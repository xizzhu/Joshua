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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.preview.PreviewViewData
import me.xizzhu.android.joshua.preview.loadPreviewV2
import me.xizzhu.android.joshua.preview.toVersePreviewItems
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.currentTimeMillis
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import java.util.Calendar
import kotlin.collections.ArrayList

abstract class AnnotatedVersesViewModel<V : VerseAnnotation>(
        private val bibleReadingManager: BibleReadingManager,
        private val verseAnnotationManager: VerseAnnotationManager<V>,
        @StringRes private val noItemText: Int,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModelV2<AnnotatedVersesViewModel.ViewAction, AnnotatedVersesViewModel.ViewState>(
        settingsManager = settingsManager,
        application = application,
        initialViewState = ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.DEFAULT_SORT_ORDER,
                annotatedVerseItems = emptyList(),
        ),
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
        object ShowLoadAnnotatedVersesFailedError : ViewAction()
        class ShowOpenPreviewFailedError(val verseIndex: VerseIndex) : ViewAction()
        class ShowOpenVerseFailedError(val verseToOpen: VerseIndex) : ViewAction()
        class ShowPreview(val previewViewData: PreviewViewData) : ViewAction()
        class ShowSaveSortOrderFailedError(@Constants.SortOrder val sortOrderToSave: Int) : ViewAction()
    }

    data class ViewState(
            val settings: Settings?,
            val loading: Boolean,
            @Constants.SortOrder val sortOrder: Int,
            val annotatedVerseItems: List<BaseItem>,
    )

    init {
        settings().onEach { settings -> emitViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)

        combine(
                bibleReadingManager.currentTranslation().filter { it.isNotEmpty() },
                verseAnnotationManager.sortOrder()
        ) { currentTranslation, sortOrder ->
            try {
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = true, sortOrder = sortOrder, annotatedVerseItems = emptyList())
                }

                val items = loadAnnotatedVerses(currentTranslation, sortOrder)
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, annotatedVerseItems = items)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error occurred while loading annotated verses", e)
                emitViewAction(ViewAction.ShowLoadAnnotatedVersesFailedError)
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, annotatedVerseItems = emptyList())
                }
            }
        }.launchIn(viewModelScope)
    }

    fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        viewModelScope.launch {
            try {
                verseAnnotationManager.saveSortOrder(sortOrder)
                emitViewState { currentViewState ->
                    currentViewState.copy(sortOrder = sortOrder)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to save sort order", e)
                emitViewAction(ViewAction.ShowSaveSortOrderFailedError(sortOrder))
            }
        }
    }

    fun loadAnnotatedVerses() {
        viewModelScope.launch {
            try {
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = true, annotatedVerseItems = emptyList())
                }

                val items = loadAnnotatedVerses(
                        currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty(),
                        sortOrder = verseAnnotationManager.sortOrder().first()
                )
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, annotatedVerseItems = items)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error occurred while loading annotated verses", e)
                emitViewAction(ViewAction.ShowLoadAnnotatedVersesFailedError)
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, annotatedVerseItems = emptyList())
                }
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
        val calendar = Calendar.getInstance()
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun formatDate(calendar: Calendar, timestamp: Long): String {
        calendar.timeInMillis = currentTimeMillis()
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

    fun openVerse(verseToOpen: VerseIndex) {
        viewModelScope.launch {
            try {
                bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
                emitViewAction(ViewAction.OpenReadingScreen)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save current verse", e)
                emitViewAction(ViewAction.ShowOpenVerseFailedError(verseToOpen))
            }
        }
    }

    fun showPreview(verseIndex: VerseIndex) {
        viewModelScope.launch {
            try {
                emitViewAction(ViewAction.ShowPreview(
                        previewViewData = loadPreviewV2(bibleReadingManager, settingsManager, verseIndex, ::toVersePreviewItems)
                ))
            } catch (e: Exception) {
                Log.e(tag, "Failed to load verses for preview", e)
                emitViewAction(ViewAction.ShowOpenPreviewFailedError(verseIndex))
            }
        }
    }
}
