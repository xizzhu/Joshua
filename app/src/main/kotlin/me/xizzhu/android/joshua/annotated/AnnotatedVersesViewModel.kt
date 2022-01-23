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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.ui.recyclerview.toVersePreviewItems
import me.xizzhu.android.joshua.utils.currentTimeMillis
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import java.util.*
import kotlin.collections.ArrayList

class AnnotatedVersesViewData(val items: List<BaseItem>)

class PreviewViewData(val settings: Settings, val title: String, val items: List<BaseItem>, val currentPosition: Int)

abstract class AnnotatedVersesViewModel<V : VerseAnnotation>(
        private val bibleReadingManager: BibleReadingManager,
        private val verseAnnotationManager: VerseAnnotationManager<V>,
        @StringRes private val noItemText: Int,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModel(settingsManager, application) {
    private val annotatedVerses: MutableStateFlow<ViewData<AnnotatedVersesViewData>?> = MutableStateFlow(null)

    init {
        combine(
                bibleReadingManager.currentTranslation().filter { it.isNotEmpty() },
                verseAnnotationManager.sortOrder()
        ) { currentTranslation, sortOrder ->
            try {
                annotatedVerses.value = ViewData.Loading()
                annotatedVerses.value = ViewData.Success(loadAnnotatedVerses(currentTranslation, sortOrder))
            } catch (e: Exception) {
                Log.e(tag, "Error occurred while loading annotated verses", e)
                annotatedVerses.value = ViewData.Failure(e)
            }
            loadAnnotatedVerses(currentTranslation, sortOrder)
        }.launchIn(viewModelScope)
    }

    fun sortOrder(): Flow<Int> = verseAnnotationManager.sortOrder()

    fun saveSortOrder(@Constants.SortOrder sortOrder: Int): Flow<ViewData<Unit>> = viewData {
        verseAnnotationManager.saveSortOrder(sortOrder)
    }.onFailure { Log.e(tag, "Failed to save sort order", it) }

    fun annotatedVerses(): Flow<ViewData<AnnotatedVersesViewData>> = annotatedVerses.filterNotNull()

    fun loadAnnotatedVerses() {
        viewModelScope.launch {
            try {
                annotatedVerses.value = ViewData.Loading()
                annotatedVerses.value = ViewData.Success(loadAnnotatedVerses(
                        currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty(),
                        sortOrder = verseAnnotationManager.sortOrder().first()
                ))
            } catch (e: Exception) {
                Log.e(tag, "Error occurred while loading annotated verses", e)
                annotatedVerses.value = ViewData.Failure(e)
            }
        }
    }

    private suspend fun loadAnnotatedVerses(currentTranslation: String, @Constants.SortOrder sortOrder: Int): AnnotatedVersesViewData {
        val annotations = verseAnnotationManager.read(sortOrder)
        val verses = bibleReadingManager.readVerses(currentTranslation, annotations.map { it.verseIndex })
        return AnnotatedVersesViewData(
                items = buildAnnotatedVersesItems(
                        sortOrder = sortOrder,
                        verses = annotations.mapNotNull { annotation -> verses[annotation.verseIndex]?.let { Pair(annotation, it) } },
                        bookNames = bibleReadingManager.readBookNames(currentTranslation),
                        bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
                )
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

    protected abstract fun buildBaseItem(
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

    fun saveCurrentVerseIndex(verseToOpen: VerseIndex): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
    }.onFailure { Log.e(tag, "Failed to save current verse", it) }

    fun loadVersesForPreview(verseIndex: VerseIndex): Flow<ViewData<PreviewViewData>> = viewData {
        val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
        val items = bibleReadingManager.readVerses(currentTranslation, verseIndex.bookIndex, verseIndex.chapterIndex).toVersePreviewItems()
        PreviewViewData(
                settings = settings().first(),
                title = "${bibleReadingManager.readBookShortNames(currentTranslation)[verseIndex.bookIndex]}, ${verseIndex.chapterIndex + 1}",
                items = items,
                currentPosition = verseIndex.verseIndex
        )
    }.onFailure { Log.e(tag, "Failed to load verses for preview", it) }
}
