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

import kotlinx.coroutines.channels.SendChannel
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService

class TranslationRepository(private val localTranslationStorage: LocalTranslationStorage,
                            private val remoteTranslationService: RemoteTranslationService) {
    suspend fun reload(forceRefresh: Boolean): List<TranslationInfo> {
        return if (forceRefresh) {
            readTranslationsFromBackend()
        } else {
            val translations = readTranslationsFromLocal()
            if (translations.isNotEmpty()) {
                translations
            } else {
                readTranslationsFromBackend()
            }
        }
    }

    private suspend fun readTranslationsFromBackend(): List<TranslationInfo> {
        val fetchedTranslations = remoteTranslationService.fetchTranslations()
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

        return translations
    }

    suspend fun readTranslationsFromLocal(): List<TranslationInfo> = localTranslationStorage.readTranslations()

    suspend fun downloadTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo) {
        val translation = remoteTranslationService.fetchTranslation(
                channel, RemoteTranslationInfo.fromTranslationInfo(translationInfo))
        channel.send(100)

        localTranslationStorage.saveTranslation(translation.translationInfo.toTranslationInfo(true),
                translation.bookNames, translation.verses)
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        localTranslationStorage.removeTranslation(translationInfo)
    }
}
