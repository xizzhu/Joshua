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

package me.xizzhu.android.joshua.strongnumber

import android.content.DialogInterface
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log

data class StrongNumberListViewHolder(val loadingSpinner: ProgressBar,
                                      val strongNumberListView: CommonRecyclerView) : ViewHolder

class StrongNumberListPresenter(private val strongNumberListActivity: StrongNumberListActivity,
                                private val navigator: Navigator,
                                strongNumberListInteractor: StrongNumberListInteractor,
                                dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<StrongNumberListViewHolder, StrongNumberListInteractor>(strongNumberListInteractor, dispatcher) {
    @UiThread
    override fun onCreate(viewHolder: StrongNumberListViewHolder) {
        super.onCreate(viewHolder)

        interactor.settings().onEachSuccess { viewHolder.strongNumberListView.setSettings(it) }.launchIn(coroutineScope)
        interactor.currentTranslation().onEachSuccess {
            loadStrongNumber(strongNumberListActivity.strongNumber(), it)
        }.launchIn(coroutineScope)
    }

    private fun loadStrongNumber(sn: String, currentTranslation: String) {
        coroutineScope.launch {
            try {
                viewHolder?.loadingSpinner?.fadeIn()

                viewHolder?.strongNumberListView?.run {
                    visibility = View.GONE
                    setItems(prepareItems(sn, currentTranslation))
                    fadeIn()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load Strong's number list", e)
                strongNumberListActivity.dialog(false, R.string.dialog_load_strong_number_list_error,
                        DialogInterface.OnClickListener { _, _ -> loadStrongNumber(sn, currentTranslation) },
                        DialogInterface.OnClickListener { _, _ -> strongNumberListActivity.finish() })
            } finally {
                viewHolder?.loadingSpinner?.visibility = View.GONE
            }
        }
    }

    private suspend fun prepareItems(sn: String, currentTranslation: String): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()

        val strongNumber = interactor.strongNumber(sn)
        items.add(TextItem(formatStrongNumber(strongNumber)))

        val bookNames = interactor.bookNames(currentTranslation)
        val bookShortNames = interactor.bookShortNames(currentTranslation)
        val verseIndexes = interactor.verseIndexes(sn)
                .sortedBy { it.bookIndex * 100000 + it.chapterIndex * 1000 + it.verseIndex }
        val verses = interactor.verses(currentTranslation, verseIndexes)
        var currentBookIndex = -1
        verseIndexes.forEach { verseIndex ->
            val bookName = bookNames[verseIndex.bookIndex]
            if (verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = verseIndex.bookIndex
            }

            items.add(VerseStrongNumberItem(verseIndex, bookShortNames[verseIndex.bookIndex],
                    verses[verseIndex]?.text?.text ?: "", this::openVerse))
        }

        return items
    }

    @VisibleForTesting
    fun formatStrongNumber(strongNumber: StrongNumber): CharSequence =
            SpannableStringBuilder().append(strongNumber.sn)
                    .setSpan(createTitleStyleSpan(), createTitleSizeSpan())
                    .append(' ').append(strongNumber.meaning)
                    .toCharSequence()

    @VisibleForTesting
    fun openVerse(verseToOpen: VerseIndex) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(verseToOpen)
                navigator.navigate(strongNumberListActivity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                strongNumberListActivity.dialog(true, R.string.dialog_verse_selection_error,
                        DialogInterface.OnClickListener { _, _ -> openVerse(verseToOpen) })
            }
        }
    }
}
