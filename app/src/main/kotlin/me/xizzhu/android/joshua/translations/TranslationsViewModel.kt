/*
 * Copyright (C) 2020 Xizhi Zhu
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
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.flowFrom

data class TranslationList(val currentTranslation: String,
                           val availableTranslations: List<TranslationInfo>,
                           val downloadedTranslations: List<TranslationInfo>)

class TranslationsViewModel(private val bibleReadingManager: BibleReadingManager,
                            private val translationManager: TranslationManager,
                            settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    fun translationList(forceRefresh: Boolean): Flow<ViewData<TranslationList>> = flowFrom {
        translationManager.reload(forceRefresh)
        TranslationList(
                bibleReadingManager.currentTranslation().first(),
                translationManager.availableTranslations().first(),
                translationManager.downloadedTranslations().first()
        )
    }

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<ViewData<Int>> =
            translationManager.downloadTranslation(translationToDownload)
                    .map { progress ->
                        if (progress <= 100) {
                            ViewData.loading(progress)
                        } else {
                            // Ideally, we should use onCompletion() to handle this. However, it doesn't
                            // distinguish between a successful completion and a cancellation.
                            // See https://github.com/Kotlin/kotlinx.coroutines/issues/1693
                            ViewData.success(-1)
                        }
                    }.catch { cause -> emit(ViewData.error(exception = cause)) }

    fun removeTranslation(translationToRemove: TranslationInfo): Flow<ViewData<Unit>> = flowFrom {
        translationManager.removeTranslation(translationToRemove)
    }
}
