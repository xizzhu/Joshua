/*
 * Copyright (C) 2019 Xizhi Zhu
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
import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log

data class StrongNumberListViewHolder(val strongNumberListView: CommonRecyclerView) : ViewHolder

class StrongNumberListPresenter(private val strongNumberListActivity: StrongNumberListActivity,
                                private val navigator: Navigator,
                                strongNumberListInteractor: StrongNumberListInteractor,
                                dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<StrongNumberListViewHolder, StrongNumberListInteractor>(strongNumberListInteractor, dispatcher) {
    @UiThread
    override fun onCreate(viewHolder: StrongNumberListViewHolder) {
        super.onCreate(viewHolder)

        interactor.settings().onEachSuccess { viewHolder.strongNumberListView.setSettings(it) }.launchIn(coroutineScope)
        interactor.strongNumberRequest().onEach { loadStrongNumber(it) }.launchIn(coroutineScope)
    }

    private fun loadStrongNumber(sn: String) {
        coroutineScope.launch {
            try {
                interactor.updateLoadingState(ViewData.loading())

                viewHolder?.strongNumberListView?.run {
                    visibility = View.GONE
                    setItems(prepareItems(sn))
                    fadeIn()
                }

                interactor.updateLoadingState(ViewData.success(null))
            } catch (e: Exception) {
                Log.e(tag, "Failed to load Strong's number list", e)
                interactor.updateLoadingState(ViewData.error(exception = e))
                DialogHelper.showDialog(strongNumberListActivity, true, R.string.dialog_load_strong_number_list_error,
                        DialogInterface.OnClickListener { _, _ -> loadStrongNumber(sn) })
            }
        }
    }

    private suspend fun prepareItems(sn: String): List<BaseItem> {
        val currentTranslation = interactor.currentTranslation().dataOnSuccessOrThrow("Failed to load current translation")
        val bookNames = interactor.bookNames(currentTranslation).dataOnSuccessOrThrow("Failed to load book names")
        val bookShortNames = interactor.bookShortNames(currentTranslation).dataOnSuccessOrThrow("Failed to load book short names")
        val verseIndexes = interactor.readVerseIndexes(sn)
                .dataOnSuccessOrThrow("Failed to load verse indexes")
                .sortedBy { it.bookIndex * 100000 + it.chapterIndex * 1000 + it.verseIndex }
        val verses = interactor.verses(currentTranslation, verseIndexes).dataOnSuccessOrThrow("Failed to load verses")

        val items: ArrayList<BaseItem> = ArrayList()
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
    fun openVerse(verseToOpen: VerseIndex) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(verseToOpen)
                navigator.navigate(strongNumberListActivity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                DialogHelper.showDialog(strongNumberListActivity, true, R.string.dialog_verse_selection_error,
                        DialogInterface.OnClickListener { _, _ -> openVerse(verseToOpen) })
            }
        }
    }
}
