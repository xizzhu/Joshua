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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
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

data class StrongNumberListViewHolder(val loadingSpinner: ProgressBar,
                                      val strongNumberListView: CommonRecyclerView) : ViewHolder

class StrongNumberListPresenter(
        private val strongNumberListActivity: StrongNumberListActivity, private val navigator: Navigator,
        strongNumberListViewModel: StrongNumberListViewModel, lifecycle: Lifecycle,
        lifecycleCoroutineScope: LifecycleCoroutineScope = lifecycle.coroutineScope
) : BaseSettingsPresenter<StrongNumberListViewHolder, StrongNumberListViewModel>(strongNumberListViewModel, lifecycle, lifecycleCoroutineScope) {
    @UiThread
    override fun onBind(viewHolder: StrongNumberListViewHolder) {
        super.onBind(viewHolder)

        viewModel.settings().onEachSuccess { viewHolder.strongNumberListView.setSettings(it) }.launchIn(lifecycleScope)
        viewModel.currentTranslation().onEachSuccess { loadStrongNumber(strongNumberListActivity.strongNumber(), it) }.launchIn(lifecycleScope)
    }

    private fun loadStrongNumber(sn: String, currentTranslation: String) {
        lifecycleScope.launch {
            try {
                with(viewHolder) {
                    loadingSpinner.fadeIn()

                    strongNumberListView.visibility = View.GONE
                    strongNumberListView.setItems(prepareItems(sn, currentTranslation))
                    strongNumberListView.fadeIn()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load Strong's number list", e)
                strongNumberListActivity.dialog(false, R.string.dialog_load_strong_number_list_error,
                        DialogInterface.OnClickListener { _, _ -> loadStrongNumber(sn, currentTranslation) },
                        DialogInterface.OnClickListener { _, _ -> strongNumberListActivity.finish() })
            } finally {
                viewHolder.loadingSpinner.visibility = View.GONE
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun prepareItems(sn: String, currentTranslation: String): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()

        val strongNumber = viewModel.strongNumber(sn)
        items.add(TextItem(formatStrongNumber(strongNumber)))

        val bookNames = viewModel.bookNames(currentTranslation)
        val bookShortNames = viewModel.bookShortNames(currentTranslation)
        val verseIndexes = viewModel.verseIndexes(sn)
                .sortedBy { it.bookIndex * 100000 + it.chapterIndex * 1000 + it.verseIndex }
        val verses = viewModel.verses(currentTranslation, verseIndexes)
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
