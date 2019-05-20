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

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import androidx.appcompat.view.ActionMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.reading.detail.VerseDetailPagerAdapter
import me.xizzhu.android.joshua.ui.recyclerview.SimpleVerseItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter
import kotlin.properties.Delegates

class VersePresenter(private val readingInteractor: ReadingInteractor)
    : BaseSettingsPresenter<VerseView>(readingInteractor) {
    @VisibleForTesting
    var selectedVerse: VerseIndex = VerseIndex.INVALID
    @VisibleForTesting
    val selectedVerses: HashSet<Verse> = HashSet()
    private var actionMode: ActionMode? = null
    @VisibleForTesting
    val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_verse_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_copy -> {
                    launch(Dispatchers.Main) {
                        if (readingInteractor.copyToClipBoard(selectedVerses)) {
                            view?.onVersesCopied()
                        } else {
                            view?.onVersesCopyShareFailed()
                        }
                        mode.finish()
                    }
                    true
                }
                R.id.action_share -> {
                    if (!readingInteractor.share(selectedVerses)) {
                        view?.onVersesCopyShareFailed()
                    }
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            for (verse in selectedVerses) {
                view?.onVerseDeselected(verse.verseIndex)
            }
            selectedVerses.clear()

            actionMode = null
        }
    }

    private var currentTranslation: String by Delegates.observable("") { _, _, new ->
        if (new.isNotEmpty()) {
            view?.onCurrentTranslationUpdated(new)
        }
    }
    private var currentVerseIndex: VerseIndex by Delegates.observable(VerseIndex.INVALID) { _, old, new ->
        if (!new.isValid()) {
            return@observable
        }
        actionMode?.let {
            if (old.bookIndex != new.bookIndex || old.chapterIndex != new.chapterIndex) {
                it.finish()
            }
        }
        view?.onCurrentVerseIndexUpdated(new)
    }
    private var parallelTranslations: List<String> by Delegates.observable(emptyList()) { _, _, new ->
        view?.onParallelTranslationsUpdated(new)
    }

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            readingInteractor.observeCurrentTranslation().consumeEach { currentTranslation = it }
        }
        launch(Dispatchers.Main) {
            readingInteractor.observeCurrentVerseIndex().consumeEach { currentVerseIndex = it }
        }
        launch(Dispatchers.Main) {
            readingInteractor.observeParallelTranslations().consumeEach { parallelTranslations = it }
        }
        launch(Dispatchers.Main) {
            readingInteractor.observeVerseDetailOpenState().consumeEach {
                if (selectedVerse.isValid()) {
                    view?.onVerseDeselected(selectedVerse)
                    selectedVerse = VerseIndex.INVALID
                }
                if (it.first.isValid()) {
                    selectedVerse = it.first
                    view?.onVerseSelected(selectedVerse)
                }
            }
        }
    }

    fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to update chapter selection")
                view?.onChapterSelectionFailed(bookIndex, chapterIndex)
            }
        }
    }

    fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentVerseIndex(verseIndex)
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to save current verse")
            }
        }
    }

    fun loadVerses(bookIndex: Int, chapterIndex: Int) {
        launch(Dispatchers.Main) {
            try {
                val verses = if (parallelTranslations.isEmpty()) {
                    readingInteractor.readVerses(currentTranslation, bookIndex, chapterIndex)
                } else {
                    readingInteractor.readVerses(currentTranslation, parallelTranslations, bookIndex, chapterIndex)
                }
                val totalVerseCount = verses.size
                view?.onVersesLoaded(bookIndex, chapterIndex, verses.map { SimpleVerseItem(it, totalVerseCount) })
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load verses")
                view?.onVersesLoadFailed(bookIndex, chapterIndex)
            }
        }
    }

    fun onVerseClicked(verse: Verse) {
        if (actionMode == null) {
            launch(Dispatchers.Main) { readingInteractor.openVerseDetail(verse.verseIndex, VerseDetailPagerAdapter.PAGE_VERSES) }
            return
        }

        if (selectedVerses.contains(verse)) {
            // de-select the verse
            selectedVerses.remove(verse)
            if (selectedVerses.isEmpty()) {
                actionMode?.finish()
            }

            view?.onVerseDeselected(verse.verseIndex)
        } else {
            // select the verse
            selectedVerses.add(verse)

            view?.onVerseSelected(verse.verseIndex)
        }
    }

    fun onVerseLongClicked(verse: Verse) {
        if (actionMode == null) {
            actionMode = readingInteractor.startActionMode(actionModeCallback)
        }

        onVerseClicked(verse)
    }
}
