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

package me.xizzhu.android.joshua.annotated

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log
import java.util.*
import kotlin.collections.ArrayList

data class AnnotatedVersesViewHolder(val annotatedVerseListView: CommonRecyclerView) : ViewHolder

abstract class BaseAnnotatedVersesPresenter<V : VerseAnnotation, Interactor : AnnotatedVersesInteractor<V>>
(private val activity: BaseAnnotatedVersesActivity<V>, private val navigator: Navigator,
 @StringRes private val noItemText: Int, interactor: Interactor, dispatcher: CoroutineDispatcher)
    : BaseSettingsAwarePresenter<AnnotatedVersesViewHolder, Interactor>(interactor, dispatcher) {
    @UiThread
    override fun onCreate(viewHolder: AnnotatedVersesViewHolder) {
        super.onCreate(viewHolder)

        interactor.settings().onEachSuccess { viewHolder.annotatedVerseListView.setSettings(it) }.launchIn(coroutineScope)

        interactor.sortOrder().combineOnSuccess(interactor.currentTranslation()) { sortOrder, currentTranslation ->
            load(sortOrder, currentTranslation)
        }.launchIn(coroutineScope)
    }

    private suspend fun load(@Constants.SortOrder sortOrder: Int, currentTranslation: String) {
        try {
            interactor.updateLoadingState(ViewData.loading())
            viewHolder?.annotatedVerseListView?.run {
                visibility = View.GONE
                setItems(prepareItems(sortOrder, currentTranslation))
                fadeIn()
            }
            interactor.updateLoadingState(ViewData.success(null))
        } catch (e: Exception) {
            Log.e(tag, "Failed to load annotated verses", e)
            interactor.updateLoadingState(ViewData.error(exception = e))
            activity.dialog(false, R.string.dialog_load_annotated_verses_error,
                    DialogInterface.OnClickListener { _, _ -> coroutineScope.launch { load(sortOrder, currentTranslation) } },
                    DialogInterface.OnClickListener { _, _ -> activity.finish() })
        }
    }

    private suspend fun prepareItems(@Constants.SortOrder sortOrder: Int, currentTranslation: String): List<BaseItem> {
        val verseAnnotations = interactor.verseAnnotations(sortOrder).dataOnSuccessOrThrow("Failed to load verse annotations")
        if (verseAnnotations.isEmpty()) return listOf(TextItem(activity.getString(noItemText)))

        val bookNames = interactor.bookNames(currentTranslation).dataOnSuccessOrThrow("Failed to load book names")
        val bookShortNames = interactor.bookShortNames(currentTranslation).dataOnSuccessOrThrow("Failed to load book short names")
        val verses = interactor.verses(currentTranslation, verseAnnotations.map { it.verseIndex }).dataOnSuccessOrThrow("Failed to load verse")

        return when (sortOrder) {
            Constants.SORT_BY_DATE -> verseAnnotations.toBaseItemsByDate(bookNames, bookShortNames, verses)
            Constants.SORT_BY_BOOK -> verseAnnotations.toBaseItemsByBook(bookNames, bookShortNames, verses)
            else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
        }
    }

    @VisibleForTesting
    fun List<V>.toBaseItemsByDate(bookNames: List<String>, bookShortNames: List<String>, verses: Map<VerseIndex, Verse>): List<BaseItem> {
        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<BaseItem> = ArrayList()
        forEach { verseAnnotation ->
            calendar.timeInMillis = verseAnnotation.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(verseAnnotation.timestamp.formatDate(activity.resources, calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(verseAnnotation.toBaseItem(bookNames[verseAnnotation.verseIndex.bookIndex],
                    bookShortNames[verseAnnotation.verseIndex.bookIndex],
                    verses[verseAnnotation.verseIndex]?.text?.text ?: "",
                    Constants.SORT_BY_DATE))
        }
        return items
    }

    protected abstract fun V.toBaseItem(bookName: String, bookShortName: String, verseText: String, @Constants.SortOrder sortOrder: Int): BaseItem

    @VisibleForTesting
    fun List<V>.toBaseItemsByBook(bookNames: List<String>, bookShortNames: List<String>, verses: Map<VerseIndex, Verse>): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        forEach { verseAnnotation ->
            val bookName = bookNames[verseAnnotation.verseIndex.bookIndex]
            if (verseAnnotation.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = verseAnnotation.verseIndex.bookIndex
            }

            items.add(verseAnnotation.toBaseItem(bookNames[verseAnnotation.verseIndex.bookIndex],
                    bookShortNames[verseAnnotation.verseIndex.bookIndex],
                    verses[verseAnnotation.verseIndex]?.text?.text ?: "",
                    Constants.SORT_BY_BOOK))
        }
        return items
    }

    @VisibleForTesting
    fun openVerse(verseToOpen: VerseIndex) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(verseToOpen)
                navigator.navigate(activity, Navigator.SCREEN_READING, extrasForOpeningVerse())
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                activity.dialog(true, R.string.dialog_verse_selection_error,
                        DialogInterface.OnClickListener { _, _ -> openVerse(verseToOpen) })
            }
        }
    }

    open fun extrasForOpeningVerse(): Bundle? = null
}
