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
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.provider.TimeProvider
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import java.util.Calendar
import kotlin.collections.ArrayList
import me.xizzhu.android.joshua.preview.Preview
import me.xizzhu.android.joshua.preview.buildPreviewVerseItems

abstract class AnnotatedVerseViewModel<V : VerseAnnotation>(
    private val bibleReadingManager: BibleReadingManager,
    private val verseAnnotationManager: VerseAnnotationManager<V>,
    @StringRes private val noItemText: Int,
    private val settingsManager: SettingsManager,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val timeProvider: TimeProvider,
    private val application: Application
) : BaseViewModelV2<AnnotatedVerseViewModel.ViewAction, AnnotatedVerseViewModel.ViewState>(
    initialViewState = ViewState(
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
        val loading: Boolean,
        @Constants.SortOrder val sortOrder: Int,
        val items: List<AnnotatedVerseItem>,
        val preview: Preview?,
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
        combine(
            settingsManager.settings(),
            bibleReadingManager.currentTranslation().filter { it.isNotEmpty() },
            verseAnnotationManager.sortOrder()
        ) { settings, currentTranslation, sortOrder ->
            runCatching {
                updateViewState { it.copy(loading = true, sortOrder = sortOrder, items = emptyList()) }

                val items = loadAnnotatedVerses(settings, currentTranslation, sortOrder)
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
                    settings = settingsManager.settings().first(),
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

    private suspend fun loadAnnotatedVerses(settings: Settings, currentTranslation: String, @Constants.SortOrder sortOrder: Int): List<AnnotatedVerseItem> {
        val annotations = verseAnnotationManager.read(sortOrder)
        val verses = bibleReadingManager.readVerses(currentTranslation, annotations.map { it.verseIndex })
        val verseWithAnnotations = annotations.mapNotNull { annotation -> verses[annotation.verseIndex]?.let { Pair(annotation, it) } }
        if (verseWithAnnotations.isEmpty()) {
            return listOf(AnnotatedVerseItem.Header(settings = settings, text = application.getString(noItemText), hideDivider = true))
        }

        val bookNames = bibleReadingManager.readBookNames(currentTranslation)
        val bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
        return when (sortOrder) {
            Constants.SORT_BY_DATE -> buildItemsByDate(settings, verseWithAnnotations, bookNames, bookShortNames)
            Constants.SORT_BY_BOOK -> buildItemsByBook(settings, verseWithAnnotations, bookNames, bookShortNames)
            else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
        }
    }

    private fun buildItemsByDate(
        settings: Settings,
        verses: List<Pair<V, Verse>>,
        bookNames: List<String>,
        bookShortNames: List<String>
    ): List<AnnotatedVerseItem> {
        val calendar = timeProvider.calendar
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<AnnotatedVerseItem> = ArrayList()
        verses.forEach { (verseAnnotation, verse) ->
            calendar.timeInMillis = verseAnnotation.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(AnnotatedVerseItem.Header(settings, formatDate(calendar, verseAnnotation.timestamp), hideDivider = false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(buildAnnotatedVerseItem(
                settings = settings,
                verseAnnotation = verseAnnotation,
                bookName = bookNames[verseAnnotation.verseIndex.bookIndex],
                bookShortName = bookShortNames[verseAnnotation.verseIndex.bookIndex],
                verseText = verse.text.text,
                sortOrder = Constants.SORT_BY_DATE
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
    internal abstract fun buildAnnotatedVerseItem(
        settings: Settings,
        verseAnnotation: V,
        bookName: String,
        bookShortName: String,
        verseText: String,
        @Constants.SortOrder sortOrder: Int
    ): AnnotatedVerseItem

    private fun buildItemsByBook(
        settings: Settings,
        verses: List<Pair<V, Verse>>,
        bookNames: List<String>,
        bookShortNames: List<String>
    ): List<AnnotatedVerseItem> {
        val items: ArrayList<AnnotatedVerseItem> = ArrayList()
        var currentBookIndex = -1
        verses.forEach { (verseAnnotation, verse) ->
            val bookName = bookNames[verseAnnotation.verseIndex.bookIndex]
            if (verseAnnotation.verseIndex.bookIndex != currentBookIndex) {
                items.add(AnnotatedVerseItem.Header(settings, bookName, hideDivider = false))
                currentBookIndex = verseAnnotation.verseIndex.bookIndex
            }

            items.add(buildAnnotatedVerseItem(
                settings = settings,
                verseAnnotation = verseAnnotation,
                bookName = bookNames[verseAnnotation.verseIndex.bookIndex],
                bookShortName = bookShortNames[verseAnnotation.verseIndex.bookIndex],
                verseText = verse.text.text,
                sortOrder = Constants.SORT_BY_BOOK
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
                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val preview = Preview(
                    title = "${bibleReadingManager.readBookShortNames(currentTranslation)[verseToPreview.bookIndex]}, ${verseToPreview.chapterIndex + 1}",
                    items = buildPreviewVerseItems(
                        settings = settingsManager.settings().first(),
                        verses = bibleReadingManager.readVerses(currentTranslation, verseToPreview.bookIndex, verseToPreview.chapterIndex)
                    ),
                    currentPosition = verseToPreview.verseIndex
                )
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
