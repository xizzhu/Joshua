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
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.currentTimeMillis
import me.xizzhu.android.logger.Log
import java.util.*
import kotlin.collections.ArrayList

data class AnnotatedVersesViewHolder(
        val loadingSpinner: ProgressBar, val annotatedVerseListView: CommonRecyclerView
) : ViewHolder

abstract class BaseAnnotatedVersesPresenter<V : VerseAnnotation, A : BaseAnnotatedVersesActivity<V, A>>(
        private val navigator: Navigator, @StringRes private val noItemText: Int,
        annotatedVersesViewModel: BaseAnnotatedVersesViewModel<V>, activity: A, coroutineScope: CoroutineScope
) : BaseSettingsPresenter<AnnotatedVersesViewHolder, BaseAnnotatedVersesViewModel<V>, A>(annotatedVersesViewModel, activity, coroutineScope) {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun observeSettings() {
        viewModel.settings().onEachSuccess { viewHolder.annotatedVerseListView.setSettings(it) }.launchIn(coroutineScope)

        viewModel.sortOrder().combineOnSuccess(viewModel.currentTranslation()) { sortOrder, currentTranslation ->
            loadAnnotatedVerses(sortOrder, currentTranslation)
        }.launchIn(coroutineScope)
    }

    private fun loadAnnotatedVerses(@Constants.SortOrder sortOrder: Int, currentTranslation: String) {
        viewModel.annotatedVerses(sortOrder, currentTranslation).onEach(
                onLoading = {
                    viewHolder.loadingSpinner.fadeIn()
                    viewHolder.annotatedVerseListView.visibility = View.GONE
                },
                onSuccess = { viewData ->
                    viewHolder.annotatedVerseListView.setItems(viewData.toItems(sortOrder))
                    viewHolder.annotatedVerseListView.fadeIn()
                    viewHolder.loadingSpinner.visibility = View.GONE
                },
                onError = { _, e ->
                    Log.e(tag, "Failed to load annotated verses", e!!)
                    viewHolder.loadingSpinner.visibility = View.GONE
                    activity.dialog(false, R.string.dialog_load_annotated_verses_error,
                            DialogInterface.OnClickListener { _, _ -> loadAnnotatedVerses(sortOrder, currentTranslation) },
                            DialogInterface.OnClickListener { _, _ -> activity.finish() })
                }
        ).launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun AnnotatedVersesViewData<V>.toItems(@Constants.SortOrder sortOrder: Int): List<BaseItem> =
            if (verses.isEmpty()) {
                listOf(TextItem(activity.getString(noItemText)))
            } else {
                when (sortOrder) {
                    Constants.SORT_BY_DATE -> toItemsByDate()
                    Constants.SORT_BY_BOOK -> toItemsByBook()
                    else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
                }
            }

    private fun AnnotatedVersesViewData<V>.toItemsByDate(): List<BaseItem> {
        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<BaseItem> = ArrayList()
        verses.forEach { (verseAnnotation, verse) ->
            calendar.timeInMillis = verseAnnotation.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(verseAnnotation.timestamp.formatDate(calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(verseAnnotation.toBaseItem(bookNames[verseAnnotation.verseIndex.bookIndex],
                    bookShortNames[verseAnnotation.verseIndex.bookIndex], verse.text.text, Constants.SORT_BY_DATE))
        }
        return items
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun Long.formatDate(calendar: Calendar): String {
        calendar.timeInMillis = currentTimeMillis()
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = this
        val year = calendar.get(Calendar.YEAR)
        return if (year == currentYear) {
            activity.resources.getString(R.string.text_date_without_year,
                    activity.resources.getStringArray(R.array.text_months)[calendar.get(Calendar.MONTH)],
                    calendar.get(Calendar.DATE))
        } else {
            activity.resources.getString(R.string.text_date,
                    activity.resources.getStringArray(R.array.text_months)[calendar.get(Calendar.MONTH)],
                    calendar.get(Calendar.DATE), year)
        }
    }

    protected abstract fun V.toBaseItem(bookName: String, bookShortName: String, verseText: String, @Constants.SortOrder sortOrder: Int): BaseItem

    private fun AnnotatedVersesViewData<V>.toItemsByBook(): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        verses.forEach { (verseAnnotation, verse) ->
            val bookName = bookNames[verseAnnotation.verseIndex.bookIndex]
            if (verseAnnotation.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = verseAnnotation.verseIndex.bookIndex
            }

            items.add(verseAnnotation.toBaseItem(bookNames[verseAnnotation.verseIndex.bookIndex],
                    bookShortNames[verseAnnotation.verseIndex.bookIndex], verse.text.text, Constants.SORT_BY_BOOK))
        }
        return items
    }

    protected fun openVerse(verseToOpen: VerseIndex) {
        coroutineScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(verseToOpen)
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
