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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.ui.BaseSwipeRefresherInteractor

class TranslationInteractor(private val translationManagementActivity: TranslationManagementActivity,
                            private val bibleReadingManager: BibleReadingManager,
                            private val translationManager: TranslationManager,
                            settingsManager: SettingsManager) : BaseSwipeRefresherInteractor(settingsManager, IS_LOADING) {
    fun observeAvailableTranslations(): Flow<List<TranslationInfo>> = translationManager.observeAvailableTranslations()

    fun observeDownloadedTranslations(): Flow<List<TranslationInfo>> = translationManager.observeDownloadedTranslations()

    fun observeCurrentTranslation(): Flow<String> = bibleReadingManager.observeCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    suspend fun reload(forceRefresh: Boolean) {
        translationManager.reload(forceRefresh)
    }

    suspend fun downloadTranslation(progressChannel: SendChannel<Int>, translationInfo: TranslationInfo) {
        translationManager.downloadTranslation(progressChannel, translationInfo)
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        translationManager.removeTranslation(translationInfo)
    }

    fun finish() {
        translationManagementActivity.finish()
    }
}
