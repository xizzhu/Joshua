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
    private val availableTranslations: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel(emptyList())
    private val downloadedTranslations: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel(emptyList())

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val available = ArrayList<TranslationInfo>()
            val downloaded = ArrayList<TranslationInfo>()
            for (t in translationRepository.readTranslationsFromLocal()) {
                if (t.downloaded) {
                    downloaded.add(t)
                } else {
                    available.add(t)
                }
            }
            availableTranslations.send(available)
            downloadedTranslations.send(downloaded)
        }
    }

    fun observeAvailableTranslations(): ReceiveChannel<List<TranslationInfo>> = availableTranslations.openSubscription()

    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> = downloadedTranslations.openSubscription()

    suspend fun reload(forceRefresh: Boolean) {
        val available = ArrayList<TranslationInfo>()
        val downloaded = ArrayList<TranslationInfo>()
        val translations = translationRepository.reload(forceRefresh)
        for (t in translations) {
            if (t.downloaded) {
                downloaded.add(t)
            } else {
                available.add(t)
            }
        }
        if (available != availableTranslations.value) {
            availableTranslations.send(available)
        }
        if (downloaded != downloadedTranslations.value) {
            downloadedTranslations.send(downloaded)
        }
    }

    suspend fun downloadTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo) {
        translationRepository.downloadTranslation(channel, translationInfo)

        val currentAvailable = ArrayList(availableTranslations.value)
        currentAvailable.remove(translationInfo)
        availableTranslations.send(currentAvailable)

        val currentDownloaded = ArrayList(downloadedTranslations.value)
        currentDownloaded.add(TranslationInfo(translationInfo.shortName, translationInfo.name,
                translationInfo.language, translationInfo.size, true))
        downloadedTranslations.send(currentDownloaded)

        channel.close()
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        translationRepository.removeTranslation(translationInfo)

        val currentDownloaded = ArrayList(downloadedTranslations.value)
        if (currentDownloaded.removeByShortName(translationInfo.shortName)) {
            downloadedTranslations.send(currentDownloaded)

            val currentAvailable = ArrayList(availableTranslations.value)
            currentAvailable.add(TranslationInfo(translationInfo.shortName, translationInfo.name,
                    translationInfo.language, translationInfo.size, false))
            availableTranslations.send(currentAvailable)
        }
    }
}

private fun ArrayList<TranslationInfo>.removeByShortName(translationShortName: String): Boolean {
    for (i in 0 until size) {
        if (this[i].shortName == translationShortName) {
            removeAt(i)
            return true
        }
    }
    return false
}
