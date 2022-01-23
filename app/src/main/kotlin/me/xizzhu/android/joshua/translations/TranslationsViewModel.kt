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

package me.xizzhu.android.joshua.translations

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log
import java.util.*
import javax.inject.Inject

class TranslationsViewData(val items: List<BaseItem>)

@HiltViewModel
class TranslationsViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val translationManager: TranslationManager,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModel(settingsManager, application) {
    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME)
    private val translations: MutableStateFlow<ViewData<TranslationsViewData>?> = MutableStateFlow(null)

    init {
        refreshTranslations(false)
    }

    fun selectTranslation(translationToSelect: TranslationInfo): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentTranslation(translationToSelect.shortName)
    }.onFailure { Log.e(tag, "Failed to select translation", it) }

    fun translations(): Flow<ViewData<TranslationsViewData>> = translations.filterNotNull()

    fun refreshTranslations(forceRefresh: Boolean) {
        viewModelScope.launch {
            translations.value = ViewData.Loading()
            translationManager.reload(forceRefresh)

            // After the refresh, if no change is detected, nothing will be emitted, therefore we need to manually load here.
            val items: ArrayList<BaseItem> = ArrayList()
            val availableTranslations = translationManager.availableTranslations().first().sortedWith(translationComparator)
            val downloadedTranslations = translationManager.downloadedTranslations().first().sortedWith(translationComparator)
            if (availableTranslations.isEmpty() && downloadedTranslations.isEmpty()) {
                translations.value = ViewData.Failure(IllegalStateException("No available nor downloaded translation"))
                return@launch
            }
            val currentTranslation = bibleReadingManager.currentTranslation().first()
            items.addAll(downloadedTranslations.toItems(currentTranslation))
            if (availableTranslations.isNotEmpty()) {
                items.add(TitleItem(application.getString(R.string.header_available_translations), false))
                items.addAll(availableTranslations.toItems(currentTranslation))
            }
            translations.value = ViewData.Success(TranslationsViewData(items))
        }
    }

    private fun List<TranslationInfo>.toItems(currentTranslation: String): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        var currentLanguage = ""
        for (translationInfo in this@toItems) {
            val language = translationInfo.language.split("_")[0]
            if (currentLanguage != language) {
                items.add(TitleItem(Locale(language).displayLanguage, true))
                currentLanguage = language
            }
            items.add(TranslationItem(translationInfo, translationInfo.downloaded && translationInfo.shortName == currentTranslation))
        }
        return items
    }

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<ViewData<Int>> =
            translationManager.downloadTranslation(translationToDownload)
                    .map { progress ->
                        when (progress) {
                            -1 -> ViewData.Failure(CancellationException("Translation downloading cancelled by user"))
                            in 0 until 100 -> ViewData.Loading(progress)
                            else -> ViewData.Success(100)
                        }
                    }
                    .catch { e ->
                        Log.e(tag, "Failed to download translation", e)
                        emit(ViewData.Failure(e))
                    }
                    .onSuccess { refreshTranslations(false) }

    fun removeTranslation(translationToDownload: TranslationInfo): Flow<ViewData<Unit>> = viewData {
        translationManager.removeTranslation(translationToDownload)
    }.onSuccess { refreshTranslations(false) }.onFailure { Log.e(tag, "Failed to remove translation", it) }
}
