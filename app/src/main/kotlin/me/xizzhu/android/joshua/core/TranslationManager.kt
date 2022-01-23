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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.flow.Flow
import me.xizzhu.android.joshua.core.repository.TranslationRepository

data class TranslationInfo(val shortName: String, val name: String, val language: String,
                           val size: Long, val downloaded: Boolean)

class TranslationManager(private val translationRepository: TranslationRepository) {
    fun availableTranslations(): Flow<List<TranslationInfo>> = translationRepository.availableTranslations

    fun downloadedTranslations(): Flow<List<TranslationInfo>> = translationRepository.downloadedTranslations

    suspend fun reload(forceRefresh: Boolean) {
        translationRepository.reload(forceRefresh)
    }

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<Int> =
            translationRepository.downloadTranslation(translationToDownload)

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        translationRepository.removeTranslation(translationInfo)
    }
}
