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
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.TranslationRepository

data class TranslationInfo(val shortName: String, val name: String, val language: String,
                           val size: Long, val downloaded: Boolean)

class TranslationManager(private val translationRepository: TranslationRepository) {
    private val translationsLock: Any = Any()
    private val availableTranslations: MutableMap<String, TranslationInfo> = mutableMapOf()
    private val downloadedTranslations: MutableMap<String, TranslationInfo> = mutableMapOf()
    private val availableTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel()
    private val downloadedTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            updateTranslations(translationRepository.readTranslationsFromLocal())
        }
    }

    @VisibleForTesting
    suspend fun updateTranslations(updatedTranslations: List<TranslationInfo>) {
        val (available, downloaded) = synchronized(translationsLock) {
            for (t in updatedTranslations) {
                if (t.downloaded) {
                    downloadedTranslations[t.shortName] = t
                } else {
                    availableTranslations[t.shortName] = t
                }
            }
            Pair(availableTranslations.values.toList(), downloadedTranslations.values.toList())
        }
        notifyTranslationsUpdated(available, downloaded)
    }

    @VisibleForTesting
    suspend fun notifyTranslationsUpdated(available: List<TranslationInfo>, downloaded: List<TranslationInfo>) {
        if (available != availableTranslationsChannel.valueOrNull) {
            availableTranslationsChannel.send(available)
        }

        if (downloaded != downloadedTranslationsChannel.valueOrNull) {
            downloadedTranslationsChannel.send(downloaded)
        }
    }

    fun observeAvailableTranslations(): ReceiveChannel<List<TranslationInfo>> = availableTranslationsChannel.openSubscription()

    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> = downloadedTranslationsChannel.openSubscription()

    suspend fun reload(forceRefresh: Boolean) {
        updateTranslations(translationRepository.reload(forceRefresh))
    }

    suspend fun downloadTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo) {
        translationRepository.downloadTranslation(channel, translationInfo)

        val (available, downloaded) = synchronized(translationsLock) {
            availableTranslations.remove(translationInfo.shortName)

            downloadedTranslations[translationInfo.shortName] = TranslationInfo(
                    translationInfo.shortName, translationInfo.name,
                    translationInfo.language, translationInfo.size, true)

            Pair(availableTranslations.values.toList(), downloadedTranslations.values.toList())
        }
        notifyTranslationsUpdated(available, downloaded)

        channel.close()
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        translationRepository.removeTranslation(translationInfo)

        val (available, downloaded) = synchronized(translationsLock) {
            if (downloadedTranslations.remove(translationInfo.shortName) != null) {
                availableTranslations[translationInfo.shortName] = TranslationInfo(
                        translationInfo.shortName, translationInfo.name,
                        translationInfo.language, translationInfo.size, false)
            }

            Pair(availableTranslations.values.toList(), downloadedTranslations.values.toList())
        }
        notifyTranslationsUpdated(available, downloaded)
    }
}
