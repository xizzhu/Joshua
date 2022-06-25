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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.*
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TranslationsViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val translationManager: TranslationManager,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModelV2<TranslationsViewModel.ViewAction, TranslationsViewModel.ViewState>(
        settingsManager = settingsManager,
        application = application,
        initialViewState = ViewState(
                settings = null,
                loading = false,
                translationItems = emptyList(),
                downloadingTranslation = false,
                downloadingProgress = 0,
                removingTranslation = false,
        ),
) {
    sealed class ViewAction {
        object GoBack : ViewAction()
        class ShowDownloadTranslationFailedError(val translationToDownload: TranslationInfo) : ViewAction()
        object ShowNoTranslationAvailableError : ViewAction()
        class ShowRemoveTranslationFailedError(val translationToRemove: TranslationInfo) : ViewAction()
        class ShowSelectTranslationFailedError(val translationToSelect: TranslationInfo) : ViewAction()
        object ShowTranslationDownloaded : ViewAction()
        object ShowTranslationRemoved : ViewAction()
    }

    data class ViewState(
            val settings: Settings?,
            val loading: Boolean,
            val translationItems: List<BaseItem>,
            val downloadingTranslation: Boolean,
            val downloadingProgress: Int,
            val removingTranslation: Boolean,
    )

    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME)

    private var downloadTranslationJob: Job? = null

    init {
        settings().onEach { settings -> emitViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)
    }

    fun selectTranslation(translationToSelect: TranslationInfo) {
        viewModelScope.launch {
            try {
                bibleReadingManager.saveCurrentTranslation(translationToSelect.shortName)
                emitViewAction(ViewAction.GoBack)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select translation", e)
                emitViewAction(ViewAction.ShowSelectTranslationFailedError(translationToSelect))
            }
        }
    }

    fun refreshTranslations(forceRefresh: Boolean) {
        viewModelScope.launch {
            emitViewState { currentViewState ->
                currentViewState.copy(loading = true, translationItems = emptyList())
            }

            translationManager.reload(forceRefresh)

            // After the refresh, if no change is detected, nothing will be emitted, therefore we need to manually load here.
            val items: ArrayList<BaseItem> = ArrayList()
            val availableTranslations = translationManager.availableTranslations().first().sortedWith(translationComparator)
            val downloadedTranslations = translationManager.downloadedTranslations().first().sortedWith(translationComparator)
            if (availableTranslations.isEmpty() && downloadedTranslations.isEmpty()) {
                emitViewAction(ViewAction.ShowNoTranslationAvailableError)
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, translationItems = emptyList())
                }
                return@launch
            }

            val currentTranslation = bibleReadingManager.currentTranslation().first()
            items.addAll(downloadedTranslations.toItems(currentTranslation))
            if (availableTranslations.isNotEmpty()) {
                items.add(TitleItem(application.getString(R.string.header_available_translations), false))
                items.addAll(availableTranslations.toItems(currentTranslation))
            }
            emitViewState { currentViewState ->
                currentViewState.copy(loading = false, translationItems = items)
            }
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

    fun downloadTranslation(translationToDownload: TranslationInfo) {
        if (downloadTranslationJob != null) return

        downloadTranslationJob = translationManager.downloadTranslation(translationToDownload)
                .onStart {
                    emitViewState { currentViewState ->
                        currentViewState.copy(downloadingTranslation = true, downloadingProgress = 0)
                    }
                }
                .onEach { progress ->
                    when (progress) {
                        in Integer.MIN_VALUE until 0 -> throw CancellationException("Translation downloading cancelled by user")
                        else -> {
                            emitViewState { currentViewState ->
                                currentViewState.copy(downloadingTranslation = true, downloadingProgress = progress)
                            }
                        }
                    }
                }
                .onCompletion { e ->
                    if (e == null) {
                        emitViewAction(ViewAction.ShowTranslationDownloaded)
                        refreshTranslations(false)
                    }
                    emitViewState { currentViewState -> currentViewState.copy(downloadingTranslation = false, downloadingProgress = 0) }

                    downloadTranslationJob = null
                }
                .catch { e ->
                    Log.e(tag, "Failed to download translation", e)
                    emitViewAction(ViewAction.ShowDownloadTranslationFailedError(translationToDownload))
                }
                .launchIn(viewModelScope)
    }

    fun cancelDownloadingTranslation() {
        downloadTranslationJob?.cancel()
        downloadTranslationJob = null
    }

    fun removeTranslation(translationToRemove: TranslationInfo) {
        viewModelScope.launch {
            try {
                emitViewState { currentViewState -> currentViewState.copy(removingTranslation = true) }
                translationManager.removeTranslation(translationToRemove)
                refreshTranslations(false)
                emitViewAction(ViewAction.ShowTranslationRemoved)
            } catch (e: Exception) {
                Log.e(tag, "Failed to remove translation", e)
                emitViewAction(ViewAction.ShowRemoveTranslationFailedError(translationToRemove))
            } finally {
                emitViewState { currentViewState -> currentViewState.copy(removingTranslation = false) }
            }
        }
    }
}
