/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.BaseInteractor

class TranslationsInteractor(
        private val bibleReadingManager: BibleReadingManager,
        private val translationManager: TranslationManager,
        settingsManager: SettingsManager
) : BaseInteractor(settingsManager) {
    suspend fun currentTranslation(): String = bibleReadingManager.currentTranslation().first()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    suspend fun availableTranslations(): List<TranslationInfo> = translationManager.availableTranslations().first()

    suspend fun downloadedTranslations(): List<TranslationInfo> = translationManager.downloadedTranslations().first()

    suspend fun refreshTranslationList(forceRefresh: Boolean) {
        translationManager.reload(forceRefresh)
    }

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<Int> = translationManager.downloadTranslation(translationToDownload)

    suspend fun removeTranslation(translationToRemove: TranslationInfo) {
        translationManager.removeTranslation(translationToRemove)
    }
}
