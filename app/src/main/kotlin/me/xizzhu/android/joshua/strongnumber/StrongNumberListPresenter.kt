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
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.launchIn
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
        private val strongNumberListActivity: StrongNumberListActivity, private val navigator: Navigator,
        strongNumberListViewModel: StrongNumberListViewModel, lifecycle: Lifecycle,
        lifecycleCoroutineScope: LifecycleCoroutineScope = lifecycle.coroutineScope
) : BaseSettingsPresenter<StrongNumberListViewHolder, StrongNumberListViewModel>(strongNumberListViewModel, lifecycle, lifecycleCoroutineScope) {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun observeSettings() {
        viewModel.settings().onEachSuccess { viewHolder.strongNumberListView.setSettings(it) }.launchIn(lifecycleScope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun loadStrongNumber() {
        viewModel.strongNumber(strongNumberListActivity.strongNumber()).onEach(
                onLoading = {
                    viewHolder.loadingSpinner.fadeIn()
                    viewHolder.strongNumberListView.visibility = View.GONE
                },
                onSuccess = { viewData ->
                    viewHolder.strongNumberListView.setItems(viewData.toItems())
                    viewHolder.strongNumberListView.fadeIn()
                    viewHolder.loadingSpinner.visibility = View.GONE
                },
                onError = { _, e ->
                    Log.e(tag, "Failed to load Strong's number list", e!!)
                    viewHolder.loadingSpinner.visibility = View.GONE
                    strongNumberListActivity.dialog(false, R.string.dialog_load_strong_number_list_error,
                            DialogInterface.OnClickListener { _, _ -> loadStrongNumber() },
                            DialogInterface.OnClickListener { _, _ -> strongNumberListActivity.finish() })
                }
        ).launchIn(lifecycleScope)
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
                    verse.text.text, this@StrongNumberListPresenter::openVerse))
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
        lifecycleScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(verseToOpen)
                navigator.navigate(strongNumberListActivity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                strongNumberListActivity.dialog(true, R.string.dialog_verse_selection_error,
                        DialogInterface.OnClickListener { _, _ -> openVerse(verseToOpen) })
            }
        }
    }
}
