/*
 * Copyright (C) 2022 Xizhi Zhu
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.utils.currentTimeMillis
import me.xizzhu.android.logger.Log

class TranslationRepository(
        private val localTranslationStorage: LocalTranslationStorage,
        private val remoteTranslationService: RemoteTranslationService,
        appScope: CoroutineScope
) {
    companion object {
        private val TAG: String = TranslationRepository::class.java.simpleName

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS = 7L * 24L * 3600L * 1000L // 7 day
    }

    private val translationsLock: Any = Any()
    private val _availableTranslations = MutableStateFlow<List<TranslationInfo>?>(null)
    val availableTranslations: Flow<List<TranslationInfo>> = _availableTranslations.filterNotNull()
    private val _downloadedTranslations = MutableStateFlow<List<TranslationInfo>?>(null)
    val downloadedTranslations: Flow<List<TranslationInfo>> = _downloadedTranslations.filterNotNull()

    init {
        appScope.launch {
            try {
                updateTranslations(readTranslationsFromLocal())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize translations", e)
                updateTranslations(emptyList())
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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
        _availableTranslations.value = available
        _downloadedTranslations.value = downloaded
    }

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
        updateTranslations(translations)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun translationListTooOld(): Boolean =
            currentTimeMillis() - localTranslationStorage.readTranslationListRefreshTimestamp() >= TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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
                localTranslationStorage.saveTranslationListRefreshTimestamp(currentTimeMillis())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save translation list refresh timestamp", e)
        }

        return translations
    }

    suspend fun readTranslationsFromLocal(): List<TranslationInfo> = localTranslationStorage.readTranslations()

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<Int> = channelFlow {
        val downloadProgressChannel = Channel<Int>(Channel.CONFLATED)
        launch { downloadProgressChannel.consumeEach { trySend(it) } }

        downloadTranslation(downloadProgressChannel, translationToDownload)
        downloadProgressChannel.close()

        val (available, downloaded) = synchronized(translationsLock) {
            val available = mutableListOf<TranslationInfo>().apply {
                _availableTranslations.value?.let { addAll(it) }
                removeAll { it.shortName == translationToDownload.shortName }
            }

            val downloaded = mutableListOf<TranslationInfo>().apply {
                _downloadedTranslations.value?.let { addAll(it) }
                add(translationToDownload.copy(downloaded = true))
            }

            return@synchronized Pair(available, downloaded)
        }
        notifyTranslationsUpdated(available, downloaded)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun downloadTranslation(downloadProgressChannel: SendChannel<Int>, translationInfo: TranslationInfo) {
        Log.i(TAG, "Start downloading translation - ${translationInfo.shortName}")
        val toDownload = RemoteTranslationInfo.fromTranslationInfo(translationInfo)
        val translation = remoteTranslationService.fetchTranslation(downloadProgressChannel, toDownload)
        Log.i(TAG, "Translation downloaded")

        localTranslationStorage.saveTranslation(
                translation.translationInfo.toTranslationInfo(true),
                translation.bookNames, translation.bookShortNames, translation.verses
        )
        remoteTranslationService.removeTranslationCache(toDownload)
        Log.i(TAG, "Translation saved to database")
        downloadProgressChannel.trySend(100)
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        Log.i(TAG, "Start removing translation - ${translationInfo.shortName}")
        localTranslationStorage.removeTranslation(translationInfo)
        remoteTranslationService.removeTranslationCache(RemoteTranslationInfo.fromTranslationInfo(translationInfo))
        Log.i(TAG, "Translation removed")

        val (available, downloaded) = synchronized(translationsLock) {
            var removed = false
            val downloaded = mutableListOf<TranslationInfo>().apply {
                _downloadedTranslations.value?.let { addAll(it) }
                removed = removeAll { it.shortName == translationInfo.shortName }
            }

            val available = mutableListOf<TranslationInfo>().apply {
                _availableTranslations.value?.let { addAll(it) }
                if (removed) {
                    add(translationInfo.copy(downloaded = false))
                }
            }

            return@synchronized Pair(available, downloaded)
        }
        notifyTranslationsUpdated(available, downloaded)
    }
}
