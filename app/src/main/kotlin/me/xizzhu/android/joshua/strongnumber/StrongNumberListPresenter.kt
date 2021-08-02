/*
 * Copyright (C) 2021 Xizhi Zhu
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

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log

data class StrongNumberListViewHolder(
        val loadingSpinner: ProgressBar, val strongNumberListView: CommonRecyclerView
) : ViewHolder

class StrongNumberListPresenter(
        private val navigator: Navigator, strongNumberListViewModel: StrongNumberListViewModel,
        strongNumberListActivity: StrongNumberListActivity, coroutineScope: CoroutineScope = strongNumberListActivity.lifecycleScope
) : BaseSettingsPresenter<StrongNumberListViewHolder, StrongNumberListViewModel, StrongNumberListActivity>(
        strongNumberListViewModel, strongNumberListActivity, coroutineScope
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun observeSettings() {
        viewModel.settings().onEach { viewHolder.strongNumberListView.setSettings(it) }.launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun loadStrongNumber() {
        viewModel.strongNumber(activity.strongNumber())
                .onStart {
                    viewHolder.loadingSpinner.fadeIn()
                    viewHolder.strongNumberListView.visibility = View.GONE
                }.onEach { strongNumber ->
                    viewHolder.strongNumberListView.setItems(strongNumber.toItems())
                    viewHolder.strongNumberListView.fadeIn()
                    viewHolder.loadingSpinner.visibility = View.GONE
                }.catch { e ->
                    Log.e(tag, "Failed to load Strong's number list", e)
                    viewHolder.loadingSpinner.visibility = View.GONE
                    activity.dialog(false, R.string.dialog_load_strong_number_list_error,
                            { _, _ -> loadStrongNumber() }, { _, _ -> activity.finish() })
                }.launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun StrongNumberViewData.toItems(): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()

        items.add(TextItem(formatStrongNumber(strongNumber)))

        var currentBookIndex = -1
        verses.forEach { verse ->
            val verseIndex = verse.verseIndex
            val bookName = bookNames[verseIndex.bookIndex]
            if (verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = verseIndex.bookIndex
            }

            items.add(VerseStrongNumberItem(verseIndex, bookShortNames[verseIndex.bookIndex],
                    verse.text.text, ::openVerse))
        }

        return items
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun formatStrongNumber(strongNumber: StrongNumber): CharSequence =
            SpannableStringBuilder().append(strongNumber.sn)
                    .setSpan(createTitleStyleSpan(), createTitleSizeSpan())
                    .append(' ').append(strongNumber.meaning)
                    .toCharSequence()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun openVerse(verseToOpen: VerseIndex) {
        coroutineScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(verseToOpen)
                navigator.navigate(activity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                activity.dialog(true, R.string.dialog_verse_selection_error,
                        { _, _ -> openVerse(verseToOpen) })
            }
        }
    }
}
