/*
 * Copyright (C) 2023 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading.toolbar

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.utils.filterIsValid
import me.xizzhu.android.joshua.utils.filterNotEmpty
import me.xizzhu.android.logger.Log

@HiltViewModel
class ReadingToolbarViewModel @Inject constructor(
    private val bibleReadingManager: BibleReadingManager,
    translationManager: TranslationManager,
    application: Application
) : BaseViewModelV2<ReadingToolbarViewModel.ViewAction, ReadingToolbarViewModel.ViewState>(
    initialViewState = ViewState(
        title = application.getString(R.string.app_name),
        translationItems = emptyList(),
        error = null
    )
) {
    sealed class ViewAction

    data class ViewState(
        val title: String,
        val translationItems: List<TranslationItem>,
        val error: Error? = null
    ) {
        sealed class Error {
            data class ParallelTranslationRemovalError(val translationToRemove: String) : Error()
            data class ParallelTranslationRequestingError(val translationToRequest: String) : Error()
            data class TranslationSelectionError(val translationToSelect: String) : Error()
        }
    }

    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)

    init {
        combine(
            bibleReadingManager.currentTranslation().filterNotEmpty(),
            bibleReadingManager.currentVerseIndex().filterIsValid(),
        ) { currentTranslation, currentVerseIndex ->
            val currentBookShortName = bibleReadingManager.readBookShortNames(currentTranslation)[currentVerseIndex.bookIndex]
            updateViewState { it.copy(title = "$currentBookShortName, ${currentVerseIndex.chapterIndex + 1}") }
        }.launchIn(viewModelScope)

        combine(
            bibleReadingManager.currentTranslation().filterNotEmpty(),
            bibleReadingManager.parallelTranslations(),
            translationManager.downloadedTranslations(),
        ) { currentTranslation, parallelTranslations, downloadedTranslations ->
            val translationItems = ArrayList<TranslationItem>(downloadedTranslations.size + 1)
            downloadedTranslations.sortedWith(translationComparator).forEach { downloaded ->
                val isCurrentTranslation = currentTranslation == downloaded.shortName
                translationItems.add(TranslationItem.Translation(
                    translationShortName = downloaded.shortName,
                    isCurrentTranslation = isCurrentTranslation,
                    isParallelTranslation = parallelTranslations.contains(downloaded.shortName),
                ))
            }
            translationItems.add(TranslationItem.More)
            updateViewState { it.copy(translationItems = translationItems) }
        }.launchIn(viewModelScope)
    }

    fun requestParallelTranslation(translationToRequest: String) {
        viewModelScope.launch {
            runCatching {
                bibleReadingManager.requestParallelTranslation(translationToRequest)
            }.onFailure { e ->
                Log.e(tag, "Failed to request parallel translation [$translationToRequest]", e)
                updateViewState { it.copy(error = ViewState.Error.ParallelTranslationRequestingError(translationToRequest)) }
            }
        }
    }

    fun removeParallelTranslation(translationToRemove: String) {
        viewModelScope.launch {
            runCatching {
                bibleReadingManager.removeParallelTranslation(translationToRemove)
            }.onFailure { e ->
                Log.e(tag, "Failed to remove parallel translation [$translationToRemove]", e)
                updateViewState { it.copy(error = ViewState.Error.ParallelTranslationRemovalError(translationToRemove)) }
            }
        }
    }

    fun selectTranslation(translationToSelect: String) {
        viewModelScope.launch {
            runCatching {
                bibleReadingManager.saveCurrentTranslation(translationToSelect)

                runCatching {
                    bibleReadingManager.removeParallelTranslation(translationToSelect)
                }.onFailure { Log.e(tag, "Failed to remove the selected translation from parallel", it) }
            }.onFailure { e ->
                Log.e(tag, "Failed to select translation [$translationToSelect]", e)
                updateViewState { it.copy(error = ViewState.Error.TranslationSelectionError(translationToSelect)) }
            }
        }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}
