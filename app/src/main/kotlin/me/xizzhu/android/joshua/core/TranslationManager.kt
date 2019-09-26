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

package me.xizzhu.android.joshua.core

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.logger.Log

data class TranslationInfo(val shortName: String, val name: String, val language: String,
                           val size: Long, val downloaded: Boolean)

class TranslationManager(private val translationRepository: TranslationRepository) {
    companion object {
        private val TAG = TranslationManager::class.java.simpleName
    }

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val translationsLock: Any = Any()
    private val availableTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel()
    private val downloadedTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                updateTranslations(translationRepository.readTranslationsFromLocal())
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

    @VisibleForTesting
    fun notifyTranslationsUpdated(available: List<TranslationInfo>, downloaded: List<TranslationInfo>) {
        availableTranslationsChannel.offer(available)
        downloadedTranslationsChannel.offer(downloaded)
    }

    fun observeAvailableTranslations(): Flow<List<TranslationInfo>> = availableTranslationsChannel.asFlow()

    fun observeDownloadedTranslations(): Flow<List<TranslationInfo>> = downloadedTranslationsChannel.asFlow()

    suspend fun reload(forceRefresh: Boolean) {
        updateTranslations(translationRepository.reload(forceRefresh))
    }

    suspend fun downloadTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo) {
        translationRepository.downloadTranslation(channel, translationInfo)

        val (available, downloaded) = synchronized(translationsLock) {
            val available = mutableListOf<TranslationInfo>().apply {
                availableTranslationsChannel.valueOrNull?.let { addAll(it) }
                removeAll { it.shortName == translationInfo.shortName }
            }

            val downloaded = mutableListOf<TranslationInfo>().apply {
                downloadedTranslationsChannel.valueOrNull?.let { addAll(it) }
                add(translationInfo.copy(downloaded = true))
            }

            return@synchronized Pair(available, downloaded)
        }
        notifyTranslationsUpdated(available, downloaded)

        channel.close()
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        translationRepository.removeTranslation(translationInfo)

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
