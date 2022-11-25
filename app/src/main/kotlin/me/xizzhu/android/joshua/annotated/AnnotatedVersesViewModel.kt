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

package me.xizzhu.android.joshua.annotated

import android.app.Application
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.provider.TimeProvider
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.preview.PreviewViewData
import me.xizzhu.android.joshua.preview.loadPreviewV2
import me.xizzhu.android.joshua.preview.toVersePreviewItems
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import java.util.Calendar
import kotlin.collections.ArrayList

abstract class AnnotatedVersesViewModel<V : VerseAnnotation>(
    private val bibleReadingManager: BibleReadingManager,
    private val verseAnnotationManager: VerseAnnotationManager<V>,
    @StringRes private val noItemText: Int,
    private val settingsManager: SettingsManager,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val timeProvider: TimeProvider,
    private val application: Application
) : BaseViewModelV2<AnnotatedVersesViewModel.ViewAction, AnnotatedVersesViewModel.ViewState>(
    initialViewState = ViewState(
        settings = null,
        loading = false,
        sortOrder = Constants.DEFAULT_SORT_ORDER,
        items = emptyList(),
        preview = null,
        error = null,
    )
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
    }

    data class ViewState(
        val settings: Settings?,
        val loading: Boolean,
        @Constants.SortOrder val sortOrder: Int,
        val items: List<BaseItem>,
        val preview: PreviewViewData?,
        val error: Error?,
    ) {
        sealed class Error {
            object AnnotatedVersesLoadingError : Error()
            data class PreviewLoadingError(val verseToPreview: VerseIndex) : Error()
            data class SortOrderSavingError(@Constants.SortOrder val sortOrder: Int) : Error()
            data class VerseOpeningError(val verseToOpen: VerseIndex) : Error()
        }
    }

    init {
        settingsManager.settings().onEach { settings -> updateViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)

        combine(
            bibleReadingManager.currentTranslation().filter { it.isNotEmpty() },
            verseAnnotationManager.sortOrder()
        ) { currentTranslation, sortOrder ->
            runCatching {
                updateViewState { it.copy(loading = true, sortOrder = sortOrder, items = emptyList()) }

                val items = loadAnnotatedVerses(currentTranslation, sortOrder)
                updateViewState { it.copy(loading = false, items = items) }
            }.onFailure { e ->
                Log.e(tag, "Error occurred while loading annotated verses", e)
                updateViewState { it.copy(loading = false, error = ViewState.Error.AnnotatedVersesLoadingError) }
            }
        }.flowOn(coroutineDispatcherProvider.default).launchIn(viewModelScope)
    }

    fun loadAnnotatedVerses() {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                updateViewState { it.copy(loading = true, items = emptyList()) }

                val sortOrder = verseAnnotationManager.sortOrder().first()
                val items = loadAnnotatedVerses(
                    currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty(),
                    sortOrder = sortOrder
                )
                updateViewState { it.copy(loading = false, sortOrder = sortOrder, items = items) }
            }.onFailure { e ->
                Log.e(tag, "Error occurred while loading annotated verses", e)
                updateViewState { it.copy(loading = false, error = ViewState.Error.AnnotatedVersesLoadingError) }
            }
        }
    }

    private suspend fun loadAnnotatedVerses(currentTranslation: String, @Constants.SortOrder sortOrder: Int): List<BaseItem> {
        val annotations = verseAnnotationManager.read(sortOrder)
        val verses = bibleReadingManager.readVerses(currentTranslation, annotations.map { it.verseIndex })
        return buildAnnotatedVersesItems(
            sortOrder = sortOrder,
            verses = annotations.mapNotNull { annotation -> verses[annotation.verseIndex]?.let { Pair(annotation, it) } },
            bookNames = bibleReadingManager.readBookNames(currentTranslation),
            bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
        )
    }

    private fun buildAnnotatedVersesItems(
        @Constants.SortOrder sortOrder: Int, verses: List<Pair<V, Verse>>, bookNames: List<String>, bookShortNames: List<String>
    ): List<BaseItem> = if (verses.isEmpty()) {
        listOf(TextItem(application.getString(noItemText)))
    } else {
        when (sortOrder) {
            Constants.SORT_BY_DATE -> buildItemsByDate(verses, bookNames, bookShortNames)
            Constants.SORT_BY_BOOK -> buildItemsByBook(verses, bookNames, bookShortNames)
            else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
        }
    }

    private fun buildItemsByDate(verses: List<Pair<V, Verse>>, bookNames: List<String>, bookShortNames: List<String>): List<BaseItem> {
        val calendar = timeProvider.calendar
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<BaseItem> = ArrayList()
        verses.forEach { (verseAnnotation, verse) ->
            calendar.timeInMillis = verseAnnotation.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(formatDate(calendar, verseAnnotation.timestamp), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(buildBaseItem(
                verseAnnotation, bookNames[verseAnnotation.verseIndex.bookIndex],
                bookShortNames[verseAnnotation.verseIndex.bookIndex], verse.text.text, Constants.SORT_BY_DATE
            ))
        }
        return items
    }

    private fun formatDate(calendar: Calendar, timestamp: Long): String {
        calendar.timeInMillis = timeProvider.currentTimeMillis
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val year = calendar.get(Calendar.YEAR)
        return if (year == currentYear) {
            application.getString(R.string.text_date_without_year,
                application.resources.getStringArray(R.array.text_months)[calendar.get(Calendar.MONTH)],
                calendar.get(Calendar.DATE))
        } else {
            application.getString(R.string.text_date,
                application.resources.getStringArray(R.array.text_months)[calendar.get(Calendar.MONTH)],
                calendar.get(Calendar.DATE), year)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun buildBaseItem(
        annotatedVerse: V, bookName: String, bookShortName: String, verseText: String, @Constants.SortOrder sortOrder: Int
    ): BaseItem

    private fun buildItemsByBook(verses: List<Pair<V, Verse>>, bookNames: List<String>, bookShortNames: List<String>): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        verses.forEach { (verseAnnotation, verse) ->
            val bookName = bookNames[verseAnnotation.verseIndex.bookIndex]
            if (verseAnnotation.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = verseAnnotation.verseIndex.bookIndex
            }

            items.add(buildBaseItem(
                verseAnnotation, bookNames[verseAnnotation.verseIndex.bookIndex],
                bookShortNames[verseAnnotation.verseIndex.bookIndex], verse.text.text, Constants.SORT_BY_BOOK
            ))
        }
        return items
    }

    fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                verseAnnotationManager.saveSortOrder(sortOrder)
            }.onFailure { e ->
                Log.e(tag, "Failed to save sort order", e)
                updateViewState { it.copy(error = ViewState.Error.SortOrderSavingError(sortOrder)) }
            }
        }
    }

    fun openVerse(verseToOpen: VerseIndex) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
                emitViewAction(ViewAction.OpenReadingScreen)
            }.onFailure { e ->
                Log.e(tag, "Failed to save current verse", e)
                updateViewState { it.copy(error = ViewState.Error.VerseOpeningError(verseToOpen)) }
            }
        }
    }

    fun loadPreview(verseToPreview: VerseIndex) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                val preview = loadPreviewV2(bibleReadingManager, settingsManager, verseToPreview, ::toVersePreviewItems)
                updateViewState { it.copy(preview = preview) }
            }.onFailure { e ->
                Log.e(tag, "Failed to load verses for preview", e)
                updateViewState { it.copy(error = ViewState.Error.PreviewLoadingError(verseToPreview)) }
            }
        }
    }

    fun markPreviewAsClosed() {
        updateViewState { it.copy(preview = null) }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}
