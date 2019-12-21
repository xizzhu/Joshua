/*
 * Copyright (C) 2019 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xizzhu.android.joshua.core.repository

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.analytics.Analytics
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.utils.Clock
import me.xizzhu.android.logger.Log

class TranslationRepository(private val localTranslationStorage: LocalTranslationStorage,
                            private val remoteTranslationService: RemoteTranslationService,
                            initDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    companion object {
        private val TAG: String = TranslationRepository::class.java.simpleName

        @VisibleForTesting
        const val TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS = 7L * 24L * 3600L * 1000L // 7 day
    }

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val translationsLock: Any = Any()
    private val availableTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel()
    private val downloadedTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(initDispatcher) {
            try {
                updateTranslations(readTranslationsFromLocal())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize translations", e)
                updateTranslations(emptyList())
            }
        }
    }

    @VisibleForTesting
    fun updateTranslations(updatedTranslations: List<TranslationInfo>) {
        val (available, downloaded) = synchronized(translationsLock) {
            val available = mutableMapOf<String, TranslationInfo>()
            val downloaded = mutableMapOf<String, TranslationInfo>()
            updatedTranslations.forEach { t ->
                if (t.downloaded) {
                    downloaded[t.shortName] = t
                } else {
                    available[t.shortName] = t
                }
            }
            return@synchronized Pair(available, downloaded)
        }
        notifyTranslationsUpdated(available.values.toList(), downloaded.values.toList())
    }

    private fun notifyTranslationsUpdated(available: List<TranslationInfo>, downloaded: List<TranslationInfo>) {
        availableTranslationsChannel.offer(available)
        downloadedTranslationsChannel.offer(downloaded)
    }

    fun availableTranslations(): Flow<List<TranslationInfo>> = availableTranslationsChannel.asFlow()

    fun downloadedTranslations(): Flow<List<TranslationInfo>> = downloadedTranslationsChannel.asFlow()

    suspend fun reload(forceRefresh: Boolean) {
        val translations = if (forceRefresh || translationListTooOld()) {
            try {
                readTranslationsFromBackend()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read translation list from backend", e)
                readTranslationsFromLocal()
            }
        } else {
            readTranslationsFromLocal().ifEmpty {
                Log.w(TAG, "Have fresh but empty local translation list")
                readTranslationsFromBackend()
            }
        }
        if (translations.isEmpty()) {
            throw IllegalStateException("Empty translation list")
        }
        updateTranslations(translations)
    }

    @VisibleForTesting
    suspend fun translationListTooOld(): Boolean =
            Clock.currentTimeMillis() - localTranslationStorage.readTranslationListRefreshTimestamp() >= TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS

    @VisibleForTesting
    suspend fun readTranslationsFromBackend(): List<TranslationInfo> {
        Log.i(TAG, "Start fetching translation list")
        val fetchedTranslations = remoteTranslationService.fetchTranslations()
        Log.i(TAG, "Translation list downloaded")
        val localTranslations = readTranslationsFromLocal()

        val translations = ArrayList<TranslationInfo>(fetchedTranslations.size)
        for (fetched in fetchedTranslations) {
            var downloaded = false
            for (local in localTranslations) {
                if (fetched.shortName == local.shortName) {
                    downloaded = local.downloaded
                    break
                }
            }
            translations.add(fetched.toTranslationInfo(downloaded))
        }

        localTranslationStorage.replaceTranslations(translations)

        try {
            if (translations.isNotEmpty()) {
                localTranslationStorage.saveTranslationListRefreshTimestamp(
                        Clock.currentTimeMillis())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save translation list refresh timestamp", e)
        }

        return translations
    }

    suspend fun readTranslationsFromLocal(): List<TranslationInfo> = localTranslationStorage.readTranslations()

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<Int> = channelFlow {
        val downloadProgressChannel = Channel<Int>(Channel.CONFLATED)
        launch { downloadProgressChannel.consumeEach { offer(it) } }

        downloadTranslation(downloadProgressChannel, translationToDownload)
        val (available, downloaded) = synchronized(translationsLock) {
            val available = mutableListOf<TranslationInfo>().apply {
                availableTranslationsChannel.valueOrNull?.let { addAll(it) }
                removeAll { it.shortName == translationToDownload.shortName }
            }

            val downloaded = mutableListOf<TranslationInfo>().apply {
                downloadedTranslationsChannel.valueOrNull?.let { addAll(it) }
                add(translationToDownload.copy(downloaded = true))
            }

            return@synchronized Pair(available, downloaded)
        }
        notifyTranslationsUpdated(available, downloaded)

        downloadProgressChannel.send(101)
        downloadProgressChannel.close()
    }

    @VisibleForTesting
    suspend fun downloadTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo) {
        val start = Clock.elapsedRealtime()
        Log.i(TAG, "Start downloading translation - ${translationInfo.shortName}")
        val translation = remoteTranslationService.fetchTranslation(
                channel, RemoteTranslationInfo.fromTranslationInfo(translationInfo))
        Log.i(TAG, "Translation downloaded")
        channel.send(100)
        val downloadFinished = Clock.elapsedRealtime()

        localTranslationStorage.saveTranslation(translation.translationInfo.toTranslationInfo(true),
                translation.bookNames, translation.bookShortNames, translation.verses)
        Log.i(TAG, "Translation saved to database")
        val installFinished = Clock.elapsedRealtime()

        Analytics.track(Analytics.EVENT_DOWNLOAD_TRANSLATION, mapOf(
                Pair(Analytics.PARAM_ITEM_ID, translationInfo.shortName),
                Pair(Analytics.PARAM_DOWNLOAD_TIME, downloadFinished - start),
                Pair(Analytics.PARAM_INSTALL_TIME, installFinished - downloadFinished)
        ))
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        Log.i(TAG, "Start removing translation - ${translationInfo.shortName}")
        localTranslationStorage.removeTranslation(translationInfo)
        Log.i(TAG, "Translation removed")

        val (available, downloaded) = synchronized(translationsLock) {
            var removed = false
            val downloaded = mutableListOf<TranslationInfo>().apply {
                downloadedTranslationsChannel.valueOrNull?.let { addAll(it) }
                removed = removeAll { it.shortName == translationInfo.shortName }
            }

            val available = mutableListOf<TranslationInfo>().apply {
                availableTranslationsChannel.valueOrNull?.let { addAll(it) }
                if (removed) {
                    add(translationInfo.copy(downloaded = false))
                }
            }

            return@synchronized Pair(available, downloaded)
        }
        notifyTranslationsUpdated(available, downloaded)
    }
}
