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

package me.xizzhu.android.joshua.reading.verse

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.*
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.reading.*
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.joshua.utils.copyToClipBoard
import me.xizzhu.android.joshua.utils.share
import me.xizzhu.android.logger.Log
import kotlin.math.max

data class VerseViewHolder(val versePager: ViewPager2) : ViewHolder

class VersePresenter(
        readingViewModel: ReadingViewModel, readingActivity: ReadingActivity,
        coroutineScope: CoroutineScope = readingActivity.lifecycleScope
) : BaseSettingsPresenter<VerseViewHolder, ReadingViewModel, ReadingActivity>(readingViewModel, readingActivity, coroutineScope) {
    private val selectedVerses: MutableSet<Verse> = mutableSetOf()
    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_verse_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
            R.id.action_copy -> {
                copyToClipBoard()
                true
            }
            R.id.action_share -> {
                share()
                true
            }
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectedVerses.forEach { verse -> adapter.deselectVerse(verse.verseIndex) }
            selectedVerses.clear()
            actionMode = null
        }
    }

    private fun copyToClipBoard() {
        if (selectedVerses.isEmpty()) return

        coroutineScope.launch {
            try {
                activity.copyToClipBoard(
                        "${currentTranslationViewData.currentTranslation} ${currentVerseIndexViewData.bookName}",
                        selectedVerses.toStringForSharing(
                                currentVerseIndexViewData.bookName,
                                viewModel.settings().first().consolidateVersesForSharing
                        )
                )
                activity.toast(R.string.toast_verses_copied)
            } catch (e: Exception) {
                Log.e(tag, "Failed to copy", e)
                activity.toast(R.string.toast_unknown_error)
            } finally {
                actionMode?.finish()
            }
        }
    }

    private fun share() {
        if (selectedVerses.isEmpty()) return

        coroutineScope.launch {
            try {
                activity.share(
                        activity.getString(R.string.text_share_with),
                        selectedVerses.toStringForSharing(
                                currentVerseIndexViewData.bookName,
                                viewModel.settings().first().consolidateVersesForSharing
                        )
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to share", e)
                activity.toast(R.string.toast_unknown_error)
            } finally {
                actionMode?.finish()
            }
        }
    }

    private val adapter: VersePagerAdapter = VersePagerAdapter(readingActivity, ::loadVerses, ::updateCurrentVerse)

    private var currentTranslationViewData = CurrentTranslationViewData("", emptyList())
    private var currentVerseIndexViewData = CurrentVerseIndexViewData(VerseIndex.INVALID, "", "")

    private fun loadVerses(bookIndex: Int, chapterIndex: Int) {
        viewModel.verses(bookIndex, chapterIndex)
                .onEach {
                    adapter.setVerses(bookIndex, chapterIndex, it.toItems())
                }.catch { e ->
                    Log.e(tag, "Failed to load verses", e)
                    activity.dialog(true, R.string.dialog_verse_load_error,
                            { _, _ -> loadVerses(bookIndex, chapterIndex) })
                }.launchIn(coroutineScope)
    }

    private fun VersesViewData.toItems(): List<BaseItem> = if (simpleReadingModeOn) {
        verses.toSimpleVerseItems(highlights, ::onVerseClicked, ::onVerseLongClicked)
    } else {
        verses.toVerseItems(bookmarks, highlights, notes, ::onVerseClicked, ::onVerseLongClicked,
                ::onBookmarkClicked, ::onHighlightClicked, ::onNoteClicked)
    }

    private fun onVerseClicked(verse: Verse) {
        if (actionMode == null) {
            showVerseDetail(verse.verseIndex, VerseDetailRequest.VERSES)
            return
        }

        if (selectedVerses.contains(verse)) {
            // de-select the verse
            selectedVerses.remove(verse)
            if (selectedVerses.isEmpty()) {
                actionMode?.finish()
            }

            adapter.deselectVerse(verse.verseIndex)
        } else {
            // select the verse
            selectedVerses.add(verse)
            adapter.selectVerse(verse.verseIndex)
        }
    }

    private fun showVerseDetail(verseIndex: VerseIndex, @VerseDetailRequest.Companion.Content content: Int) {
        viewModel.requestVerseDetail(VerseDetailRequest(verseIndex, content))
    }

    private fun onVerseLongClicked(verse: Verse) {
        if (actionMode == null) {
            actionMode = activity.startSupportActionMode(actionModeCallback)
        }
        onVerseClicked(verse)
    }

    private fun onBookmarkClicked(verseIndex: VerseIndex, hasBookmark: Boolean) {
        coroutineScope.launch {
            try {
                viewModel.saveBookmark(verseIndex, hasBookmark)
            } catch (e: Exception) {
                Log.e(tag, "Failed to update bookmark", e)
                // TODO
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onHighlightClicked(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor currentHighlightColor: Int) {
        coroutineScope.launch {
            when (val defaultHighlightColor = viewModel.settings().first().defaultHighlightColor) {
                Highlight.COLOR_NONE -> {
                    activity.dialog(R.string.text_pick_highlight_color,
                            activity.resources.getStringArray(R.array.text_colors),
                            max(0, Highlight.AVAILABLE_COLORS.indexOf(currentHighlightColor))) { dialog, which ->
                        updateHighlight(verseIndex, Highlight.AVAILABLE_COLORS[which])

                        dialog.dismiss()
                    }
                }
                else -> {
                    updateHighlight(
                            verseIndex,
                            if (currentHighlightColor == Highlight.COLOR_NONE) {
                                defaultHighlightColor
                            } else {
                                Highlight.COLOR_NONE
                            }
                    )
                }
            }
        }
    }

    private fun updateHighlight(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor highlightColor: Int) {
        coroutineScope.launch {
            try {
                viewModel.saveHighlight(verseIndex, highlightColor)
            } catch (e: Exception) {
                Log.e(tag, "Failed to update highlight", e)
                // TODO
            }
        }
    }

    private fun onNoteClicked(verseIndex: VerseIndex) {
        showVerseDetail(verseIndex, VerseDetailRequest.NOTE)
    }

    private fun updateCurrentVerse(verseIndex: VerseIndex) {
        if (currentVerseIndexViewData.verseIndex == verseIndex) return

        coroutineScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(verseIndex)
            } catch (e: Exception) {
                Log.e(tag, "Failed to update current verse", e)
            }
        }
    }

    @UiThread
    override fun onBind() {
        super.onBind()

        viewHolder.versePager.offscreenPageLimit = 1
        viewHolder.versePager.adapter = adapter
        viewHolder.versePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateCurrentChapter(position.toBookIndex(), position.toChapterIndex())
            }
        })
    }

    private fun updateCurrentChapter(bookIndex: Int, chapterIndex: Int) {
        if (currentVerseIndexViewData.verseIndex.bookIndex == bookIndex
                && currentVerseIndexViewData.verseIndex.chapterIndex == chapterIndex) {
            return
        }

        coroutineScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(tag, "Failed to current chapter", e)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.settings().onEach { adapter.settings = it }.launchIn(coroutineScope)

        combine(viewModel.currentVerseIndexViewData(),
                viewModel.currentTranslationViewData()) { newVerseIndexViewData, newTranslationViewData ->
            if (actionMode != null) {
                if (currentVerseIndexViewData.verseIndex.bookIndex != newVerseIndexViewData.verseIndex.bookIndex
                        || currentVerseIndexViewData.verseIndex.chapterIndex != newVerseIndexViewData.verseIndex.chapterIndex) {
                    actionMode?.finish()
                }
            }

            currentVerseIndexViewData = newVerseIndexViewData
            currentTranslationViewData = newTranslationViewData

            adapter.setCurrent(
                    newVerseIndexViewData.verseIndex,
                    newTranslationViewData.currentTranslation,
                    newTranslationViewData.parallelTranslations
            )
            viewHolder.versePager.setCurrentItem(newVerseIndexViewData.verseIndex.toPagePosition(), false)
        }.launchIn(coroutineScope)

        viewModel.verseUpdates.onEach { adapter.notifyVerseUpdate(it) }.launchIn(coroutineScope)
    }
}
