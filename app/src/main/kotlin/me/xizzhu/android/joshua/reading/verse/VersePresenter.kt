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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.appcompat.view.ActionMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.onEachSuccess
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.VerseDetailRequest
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.joshua.utils.chooserForSharing
import me.xizzhu.android.logger.Log
import kotlin.math.max

data class VerseViewHolder(val versePager: VerseViewPager) : ViewHolder

class VersePresenter(private val readingActivity: ReadingActivity,
                     verseInteractor: VerseInteractor,
                     dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<VerseViewHolder, VerseInteractor>(verseInteractor, dispatcher) {
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
            viewHolder?.versePager?.run {
                selectedVerses.forEach { verse -> deselectVerse(verse.verseIndex) }
            }
            selectedVerses.clear()
            actionMode = null
        }
    }

    private fun copyToClipBoard() {
        coroutineScope.launch {
            try {
                if (selectedVerses.isNotEmpty()) {
                    val verse = selectedVerses.first()
                    val bookName = interactor.readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex]
                    // On older devices, this only works on the threads with loopers.
                    (readingActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText(verse.text.translationShortName + " " + bookName,
                                    selectedVerses.toStringForSharing(bookName)))
                    readingActivity.toast(R.string.toast_verses_copied)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to copy", e)
                readingActivity.toast(R.string.toast_unknown_error)
            }
            actionMode?.finish()
        }
    }

    private fun share() {
        coroutineScope.launch {
            try {
                if (selectedVerses.isNotEmpty()) {
                    val verse = selectedVerses.first()
                    val bookName = interactor.readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex]

                    readingActivity.chooserForSharing(readingActivity.getString(R.string.text_share_with), selectedVerses.toStringForSharing(bookName))
                            ?.let { readingActivity.startActivity(it) }
                            ?: throw RuntimeException("Failed to create chooser for sharing")
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to share", e)
                readingActivity.toast(R.string.toast_unknown_error)
            }
            actionMode?.finish()
        }
    }

    private var currentTranslation: String = ""
    private var currentVerseIndex: VerseIndex = VerseIndex.INVALID
    private var parallelTranslations: List<String> = emptyList()

    @UiThread
    override fun onCreate(viewHolder: VerseViewHolder) {
        super.onCreate(viewHolder)

        viewHolder.versePager.setListeners(
                onChapterSelected = { bookIndex, chapterIndex -> updateCurrentChapter(bookIndex, chapterIndex) },
                onChapterRequested = { bookIndex, chapterIndex -> loadVerses(bookIndex, chapterIndex) },
                onCurrentVerseUpdated = { verseIndex -> updateCurrentVerse(verseIndex) }
        )

        interactor.settings().onEachSuccess { viewHolder.versePager.setSettings(it) }.launchIn(coroutineScope)

        interactor.verseUpdates().onEach { viewHolder.versePager.notifyVerseUpdate(it) }.launchIn(coroutineScope)

        combine(interactor.currentTranslation(),
                interactor.currentVerseIndex()
                        .onEach { newVerseIndex ->
                            if (actionMode != null) {
                                if (currentVerseIndex.bookIndex != newVerseIndex.bookIndex
                                        || currentVerseIndex.chapterIndex != newVerseIndex.chapterIndex) {
                                    actionMode?.finish()
                                }
                            }
                        },
                interactor.parallelTranslations()
        ) { currentTranslation, currentVerseIndex, parallelTranslations ->
            this@VersePresenter.currentVerseIndex = currentVerseIndex
            this@VersePresenter.currentTranslation = currentTranslation
            this@VersePresenter.parallelTranslations = parallelTranslations
            viewHolder.versePager.setCurrent(currentVerseIndex, currentTranslation, parallelTranslations)
        }.launchIn(coroutineScope)
    }

    private fun updateCurrentChapter(bookIndex: Int, chapterIndex: Int) {
        if (currentVerseIndex.bookIndex == bookIndex && currentVerseIndex.chapterIndex == chapterIndex) return

        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(tag, "Failed to current chapter", e)
            }
        }
    }

    private fun updateCurrentVerse(verseIndex: VerseIndex) {
        if (currentVerseIndex == verseIndex) return

        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(verseIndex)
            } catch (e: Exception) {
                Log.e(tag, "Failed to update current verse", e)
            }
        }
    }

    @VisibleForTesting
    fun loadVerses(bookIndex: Int, chapterIndex: Int) {
        coroutineScope.launch {
            try {
                val items = coroutineScope {
                    val bookNameAsync = async { interactor.readBookNames(currentTranslation)[bookIndex] }
                    val highlightsAsync = async { interactor.readHighlights(bookIndex, chapterIndex) }
                    val versesAsync = async {
                        if (parallelTranslations.isEmpty()) {
                            interactor.readVerses(currentTranslation, bookIndex, chapterIndex)
                        } else {
                            interactor.readVerses(currentTranslation, parallelTranslations, bookIndex, chapterIndex)
                        }
                    }
                    return@coroutineScope if (interactor.settings().first().data!!.simpleReadingModeOn) {
                        versesAsync.await().toSimpleVerseItems(bookNameAsync.await(), highlightsAsync.await(),
                                this@VersePresenter::onVerseClicked, this@VersePresenter::onVerseLongClicked)
                    } else {
                        val bookmarksAsync = async { interactor.readBookmarks(bookIndex, chapterIndex) }
                        val notesAsync = async { interactor.readNotes(bookIndex, chapterIndex) }
                        versesAsync.await().toVerseItems(bookNameAsync.await(), bookmarksAsync.await(),
                                highlightsAsync.await(), notesAsync.await(), this@VersePresenter::onVerseClicked,
                                this@VersePresenter::onVerseLongClicked, this@VersePresenter::onBookmarkClicked,
                                this@VersePresenter::onHighlightClicked, this@VersePresenter::onNoteClicked)
                    }
                }
                viewHolder?.versePager?.setVerses(bookIndex, chapterIndex, items)
            } catch (e: Exception) {
                Log.e(tag, "Failed to load verses", e)
                readingActivity.dialog(true, R.string.dialog_verse_load_error,
                        DialogInterface.OnClickListener { _, _ -> loadVerses(bookIndex, chapterIndex) })
            }
        }
    }

    @VisibleForTesting
    fun onVerseClicked(verse: Verse) {
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

            viewHolder?.versePager?.deselectVerse(verse.verseIndex)
        } else {
            // select the verse
            selectedVerses.add(verse)

            viewHolder?.versePager?.selectVerse(verse.verseIndex)
        }
    }

    private fun showVerseDetail(verseIndex: VerseIndex, @VerseDetailRequest.Companion.Content content: Int) {
        interactor.requestVerseDetail(VerseDetailRequest(verseIndex, content))
        viewHolder?.versePager?.selectVerse(verseIndex)
    }

    @VisibleForTesting
    fun onVerseLongClicked(verse: Verse) {
        if (actionMode == null) {
            actionMode = readingActivity.startSupportActionMode(actionModeCallback)
        }
        onVerseClicked(verse)
    }

    @VisibleForTesting
    fun onBookmarkClicked(verseIndex: VerseIndex, hasBookmark: Boolean) {
        coroutineScope.launch {
            try {
                if (hasBookmark) {
                    interactor.removeBookmark(verseIndex)
                } else {
                    interactor.addBookmark(verseIndex)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to update bookmark", e)
                // TODO
            }
        }
    }

    @VisibleForTesting
    fun onHighlightClicked(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor currentHighlightColor: Int) {
        readingActivity.dialog(R.string.text_pick_highlight_color,
                readingActivity.resources.getStringArray(R.array.text_colors),
                max(0, Highlight.AVAILABLE_COLORS.indexOf(currentHighlightColor)),
                DialogInterface.OnClickListener { dialog, which ->
                    updateHighlight(verseIndex, Highlight.AVAILABLE_COLORS[which])

                    dialog.dismiss()
                })
    }

    @VisibleForTesting
    fun updateHighlight(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor highlightColor: Int) {
        coroutineScope.launch {
            try {
                if (highlightColor == Highlight.COLOR_NONE) {
                    interactor.removeHighlight(verseIndex)
                } else {
                    interactor.saveHighlight(verseIndex, highlightColor)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to update highlight", e)
                // TODO
            }
        }
    }

    @VisibleForTesting
    fun onNoteClicked(verseIndex: VerseIndex) {
        showVerseDetail(verseIndex, VerseDetailRequest.NOTE)
    }
}
