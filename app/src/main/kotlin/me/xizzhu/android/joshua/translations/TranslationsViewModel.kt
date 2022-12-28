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
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.logger.Log

@HiltViewModel
class TranslationsViewModel @Inject constructor(
    private val bibleReadingManager: BibleReadingManager,
    private val translationManager: TranslationManager,
    private val settingsManager: SettingsManager,
    private val application: Application,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
) : BaseViewModelV2<TranslationsViewModel.ViewAction, TranslationsViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        settings = null,
        items = emptyList(),
        translationDownloadingState = ViewState.TranslationDownloadingState.Idle,
        translationRemovalState = ViewState.TranslationRemovalState.Idle,
        error = null,
    )
) {
    sealed class ViewAction {
        object GoBack : ViewAction()
    }

    data class ViewState(
        val loading: Boolean,
        val settings: Settings?,
        val items: List<BaseItem>,
        val translationDownloadingState: TranslationDownloadingState,
        val translationRemovalState: TranslationRemovalState,
        val error: Error?,
    ) {
        sealed class TranslationDownloadingState {
            object Idle : TranslationDownloadingState()
            data class Downloading(val progress: Int) : TranslationDownloadingState()
            object Installing : TranslationDownloadingState()
            data class Completed(val successful: Boolean) : TranslationDownloadingState()
        }

        sealed class TranslationRemovalState {
            object Idle : TranslationRemovalState()
            object Removing : TranslationRemovalState()
            data class Completed(val successful: Boolean) : TranslationRemovalState()
        }

        sealed class Error {
            object NoTranslationsError : Error()
            data class TranslationDownloadingError(val translationToDownload: TranslationInfo) : Error()
            object TranslationNotInstalledError : Error()
            data class TranslationRemovalError(val translationToRemove: TranslationInfo) : Error()
            data class TranslationSelectionError(val translationToSelect: TranslationInfo) : Error()
        }
    }

    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME)
    private var downloadTranslationJob: Job? = null

    init {
        settingsManager.settings().onEach { settings -> updateViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)
        loadTranslations(forceRefresh = false)
    }

    fun loadTranslations(forceRefresh: Boolean) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            updateViewState { it.copy(loading = true, items = emptyList()) }

            translationManager.reload(forceRefresh)

            // After the refresh, if no change is detected, nothing will be emitted, therefore we need to manually load here.
            val availableTranslations = translationManager.availableTranslations().first().sortedWith(translationComparator)
            val downloadedTranslations = translationManager.downloadedTranslations().first().sortedWith(translationComparator)
            if (availableTranslations.isEmpty() && downloadedTranslations.isEmpty()) {
                updateViewState { it.copy(loading = false, error = ViewState.Error.NoTranslationsError) }
                return@launch
            }

            val items: ArrayList<BaseItem> = ArrayList()
            val currentTranslation = bibleReadingManager.currentTranslation().first()
            items.addAll(downloadedTranslations.toItems(currentTranslation))
            if (availableTranslations.isNotEmpty()) {
                items.add(TitleItem(application.getString(R.string.header_available_translations), false))
                items.addAll(availableTranslations.toItems(currentTranslation))
            }
            updateViewState { it.copy(loading = false, items = items) }
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

    fun selectTranslation(translationToSelect: TranslationInfo) {
        if (!translationToSelect.downloaded) {
            updateViewState { it.copy(error = ViewState.Error.TranslationNotInstalledError) }
            return
        }

        viewModelScope.launch {
            runCatching {
                bibleReadingManager.saveCurrentTranslation(translationToSelect.shortName)
                emitViewAction(ViewAction.GoBack)
            }.onFailure { e ->
                Log.e(tag, "Failed to select translation", e)
                updateViewState { it.copy(error = ViewState.Error.TranslationSelectionError(translationToSelect)) }
            }
        }
    }

    fun downloadTranslation(translationToDownload: TranslationInfo) {
        if (translationToDownload.downloaded) {
            updateViewState { it.copy(error = ViewState.Error.TranslationDownloadingError(translationToDownload)) }
            return
        }

        updateViewState { it.copy(translationDownloadingState = ViewState.TranslationDownloadingState.Downloading(progress = 0)) }

        downloadTranslationJob = translationManager.downloadTranslation(translationToDownload)
            .onEach { progress ->
                when (progress) {
                    -1 -> updateViewState { it.copy(translationDownloadingState = ViewState.TranslationDownloadingState.Completed(successful = false)) }
                    in 0 until 99 -> updateViewState { it.copy(translationDownloadingState = ViewState.TranslationDownloadingState.Downloading(progress = progress)) }
                    100 -> updateViewState { it.copy(translationDownloadingState = ViewState.TranslationDownloadingState.Installing) }
                    101 -> {
                        loadTranslations(forceRefresh = false)
                        updateViewState { it.copy(translationDownloadingState = ViewState.TranslationDownloadingState.Completed(successful = true)) }
                    }
                }
            }
            .catch { e ->
                Log.e(tag, "Failed to download translation", e)
                updateViewState { current ->
                    current.copy(
                        translationDownloadingState = ViewState.TranslationDownloadingState.Completed(successful = false),
                        error = ViewState.Error.TranslationDownloadingError(translationToDownload),
                    )
                }
            }
            .onCompletion { downloadTranslationJob = null }
            .launchIn(viewModelScope)
    }

    fun cancelDownloadingTranslation() {
        downloadTranslationJob?.cancel()
        downloadTranslationJob = null
    }

    fun removeTranslation(translationToRemove: TranslationInfo) {
        if (!translationToRemove.downloaded) {
            updateViewState { it.copy(error = ViewState.Error.TranslationRemovalError(translationToRemove)) }
            return
        }

        updateViewState { it.copy(translationRemovalState = ViewState.TranslationRemovalState.Removing) }

        viewModelScope.launch {
            runCatching {
                translationManager.removeTranslation(translationToRemove)
                loadTranslations(forceRefresh = false)
                updateViewState { it.copy(translationRemovalState = ViewState.TranslationRemovalState.Completed(successful = true)) }
            }.onFailure { e ->
                Log.e(tag, "Failed to remove translation", e)
                updateViewState { current ->
                    current.copy(
                        translationRemovalState = ViewState.TranslationRemovalState.Completed(successful = false),
                        error = ViewState.Error.TranslationRemovalError(translationToRemove),
                    )
                }
            }
        }
    }

    fun markTranslationDownloadingStateAsIdle() {
        updateViewState { it.copy(translationDownloadingState = ViewState.TranslationDownloadingState.Idle) }
    }

    fun markTranslationRemovalStateAsIdle() {
        updateViewState { it.copy(translationRemovalState = ViewState.TranslationRemovalState.Idle) }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}
