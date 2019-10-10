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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.VerseDetailRequest
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.ToastHelper
import me.xizzhu.android.joshua.utils.createChooserForSharing
import me.xizzhu.android.joshua.utils.supervisedAsync
import me.xizzhu.android.logger.Log
import kotlin.math.max
import kotlin.properties.Delegates

data class VerseViewHolder(val versePager: VerseViewPager) : ViewHolder

class VersePresenter(private val readingActivity: ReadingActivity,
                     verseInteractor: VerseInteractor,
                     dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<VerseViewHolder, VerseInteractor>(verseInteractor, dispatcher) {
    @VisibleForTesting
    var selectedVerse: VerseIndex = VerseIndex.INVALID
    @VisibleForTesting
    val selectedVerses: HashSet<Verse> = HashSet()

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
                selectedVerses.forEach { verse -> onVerseDeselected(verse.verseIndex) }
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
                    ToastHelper.showToast(readingActivity, R.string.toast_verses_copied)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to copy", e)
                ToastHelper.showToast(readingActivity, R.string.toast_unknown_error)
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

                    createChooserForSharing(readingActivity, readingActivity.getString(R.string.text_share_with), selectedVerses.toStringForSharing(bookName))
                            ?.let { readingActivity.startActivity(it) }
                            ?: throw RuntimeException("Failed to create chooser for sharing")
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to share", e)
                ToastHelper.showToast(readingActivity, R.string.toast_unknown_error)
            }
            actionMode?.finish()
        }
    }

    private var currentTranslation: String by Delegates.observable("") { _, _, new ->
        if (new.isNotEmpty()) {
            viewHolder?.versePager?.onCurrentTranslationUpdated(new)
        }
    }
    private var currentVerseIndex: VerseIndex by Delegates.observable(VerseIndex.INVALID) { _, old, new ->
        if (!new.isValid()) {
            return@observable
        }
        if (actionMode != null) {
            if (old.bookIndex != new.bookIndex || old.chapterIndex != new.chapterIndex) {
                actionMode?.finish()
            }
        }
        viewHolder?.versePager?.onCurrentVerseIndexUpdated(new)
    }
    private var parallelTranslations: List<String> by Delegates.observable(emptyList()) { _, _, new ->
        viewHolder?.versePager?.onParallelTranslationsUpdated(new)
    }

    @UiThread
    override fun onBind(viewHolder: VerseViewHolder) {
        super.onBind(viewHolder)

        viewHolder.versePager.setOnChapterSelectedListener { bookIndex, chapterIndex -> updateCurrentChapter(bookIndex, chapterIndex) }
        viewHolder.versePager.setOnCurrentVerseUpdatedListener { verseIndex -> updateCurrentVerse(verseIndex) }
        viewHolder.versePager.setOnChapterRequestedListener { bookIndex, chapterIndex -> loadVerses(bookIndex, chapterIndex) }
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

    private fun loadVerses(bookIndex: Int, chapterIndex: Int) {
        coroutineScope.launch {
            try {
                val versesAsync = supervisedAsync {
                    if (parallelTranslations.isEmpty()) {
                        interactor.readVerses(currentTranslation, bookIndex, chapterIndex)
                    } else {
                        interactor.readVerses(currentTranslation, parallelTranslations, bookIndex, chapterIndex)
                    }
                }
                val bookNameAsync = supervisedAsync { interactor.readBookNames(currentTranslation)[bookIndex] }
                val highlightsAsync = supervisedAsync { interactor.readHighlights(bookIndex, chapterIndex) }
                val items = if (interactor.settings().first().data.simpleReadingModeOn) {
                    versesAsync.await().toSimpleVerseItems(bookNameAsync.await(), highlightsAsync.await(),
                            this@VersePresenter::onVerseClicked, this@VersePresenter::onVerseLongClicked)
                } else {
                    val bookmarksAsync = supervisedAsync { interactor.readBookmarks(bookIndex, chapterIndex) }
                    val notesAsync = supervisedAsync { interactor.readNotes(bookIndex, chapterIndex) }
                    versesAsync.await().toVerseItems(bookNameAsync.await(), bookmarksAsync.await(),
                            highlightsAsync.await(), notesAsync.await(), this@VersePresenter::onVerseClicked,
                            this@VersePresenter::onVerseLongClicked, this@VersePresenter::onBookmarkClicked,
                            this@VersePresenter::onHighlightClicked, this@VersePresenter::onNoteClicked)
                }
                viewHolder?.versePager?.onVersesLoaded(bookIndex, chapterIndex, items)
            } catch (e: Exception) {
                Log.e(tag, "Failed to load verses", e)
                DialogHelper.showDialog(readingActivity, true, R.string.dialog_verse_load_error,
                        DialogInterface.OnClickListener { _, _ -> loadVerses(bookIndex, chapterIndex) })
            }
        }
    }

    @VisibleForTesting
    fun onVerseClicked(verse: Verse) {
        if (actionMode == null) {
            interactor.requestVerseDetail(VerseDetailRequest(verse.verseIndex, VerseDetailRequest.VERSES))
            return
        }

        if (selectedVerses.contains(verse)) {
            // de-select the verse
            selectedVerses.remove(verse)
            if (selectedVerses.isEmpty()) {
                actionMode?.finish()
            }

            viewHolder?.versePager?.onVerseDeselected(verse.verseIndex)
        } else {
            // select the verse
            selectedVerses.add(verse)

            viewHolder?.versePager?.onVerseSelected(verse.verseIndex)
        }
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
        DialogHelper.showDialog(readingActivity, R.string.text_pick_highlight_color,
                readingActivity.resources.getStringArray(R.array.text_colors),
                max(0, Highlight.AVAILABLE_COLORS.indexOf(currentHighlightColor)),
                DialogInterface.OnClickListener { dialog, which ->
                    updateHighlight(verseIndex, Highlight.AVAILABLE_COLORS[which])

                    dialog.dismiss()
                })
    }

    private fun updateHighlight(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor highlightColor: Int) {
        coroutineScope.launch(Dispatchers.Main) {
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
        interactor.requestVerseDetail(VerseDetailRequest(verseIndex, VerseDetailRequest.NOTE))
    }

    @UiThread
    override fun onStart() {
        super.onStart()

        coroutineScope.launch { interactor.settings().collect { viewHolder?.versePager?.onSettingsUpdated(it.data) } }
        coroutineScope.launch { interactor.currentTranslation().collect { currentTranslation = it } }
        coroutineScope.launch { interactor.currentVerseIndex().collect { currentVerseIndex = it } }
        coroutineScope.launch { interactor.parallelTranslations().collect { parallelTranslations = it } }
        coroutineScope.launch { interactor.verseUpdates().collect { viewHolder?.versePager?.onVerseUpdated(it) } }
        coroutineScope.launch {
            interactor.verseDetailRequest().collect {
                if (selectedVerse.isValid()) {
                    viewHolder?.versePager?.onVerseDeselected(selectedVerse)
                    selectedVerse = VerseIndex.INVALID
                }
                if (it.verseIndex.isValid()) {
                    selectedVerse = it.verseIndex
                    viewHolder?.versePager?.onVerseSelected(selectedVerse)
                }
            }
        }
    }
}
