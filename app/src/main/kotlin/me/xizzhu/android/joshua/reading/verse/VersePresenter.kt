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
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.utils.MVPPresenter

class VersePresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<VerseView>() {
    companion object {
        private val TAG: String = VersePresenter::class.java.simpleName
    }

    @VisibleForTesting
    val selectedVerses: HashSet<Verse> = HashSet()
    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_verse_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_copy -> {
                    readingInteractor.copyToClipBoard(selectedVerses)
                    view?.onVersesCopied()
                    mode.finish()
                    true
                }
                R.id.action_share -> {
                    if (!readingInteractor.share(selectedVerses)) {
                        view?.onVersesSharedFailed()
                    }
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            for (verse in selectedVerses) {
                view?.onVerseDeselected(verse)
            }
            selectedVerses.clear()

            actionMode = null
        }
    }

    private var currentTranslation = ""
    private var parallelTranslations = emptyList<String>()

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            val currentTranslation = readingInteractor.observeCurrentTranslation()
            receiveChannels.add(currentTranslation)
            currentTranslation.filter { it.isNotEmpty() }
                    .consumeEach {
                        this@VersePresenter.currentTranslation = it
                        view?.onCurrentTranslationUpdated(it)
                    }
        }
        launch(Dispatchers.Main) {
            val currentVerseIndex = readingInteractor.observeCurrentVerseIndex()
            receiveChannels.add(currentVerseIndex)
            currentVerseIndex.filter { it.isValid() }
                    .consumeEach {
                        actionMode?.finish()
                        view?.onCurrentVerseIndexUpdated(it)
                    }
        }
        launch(Dispatchers.Main) {
            val parallelTranslations = readingInteractor.observeParallelTranslations()
            receiveChannels.add(parallelTranslations)
            parallelTranslations.consumeEach {
                this@VersePresenter.parallelTranslations = it
                view?.onParallelTranslationsUpdated(it)
            }
        }
        launch(Dispatchers.Main) {
            readingInteractor.startTrackingReadingProgress()
        }
    }

    override fun onViewDetached() {
        launch(Dispatchers.Unconfined) {
            readingInteractor.stopTrackingReadingProgress()
        }
        super.onViewDetached()
    }

    fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(TAG, e, "Failed to update chapter selection")
                view?.onChapterSelectionFailed(bookIndex, chapterIndex)
            }
        }
    }

    fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentVerseIndex(verseIndex)
            } catch (e: Exception) {
                Log.e(TAG, e, "Failed to save current verse")
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
                view?.onVersesLoaded(bookIndex, chapterIndex, verses)
            } catch (e: Exception) {
                Log.e(TAG, e, "Failed to load verses")
                view?.onVersesLoadFailed(bookIndex, chapterIndex)
            }
        }
    }

    fun onVerseClicked(verse: Verse) {
        if (actionMode == null) {
            return
        }

        if (selectedVerses.contains(verse)) {
            // de-select the verse
            selectedVerses.remove(verse)
            if (selectedVerses.isEmpty()) {
                actionMode?.finish()
            }

            view?.onVerseDeselected(verse)
        } else {
            // select the verse
            selectedVerses.add(verse)

            view?.onVerseSelected(verse)
        }
    }

    fun onVerseLongClicked(verse: Verse) {
        if (actionMode == null) {
            actionMode = readingInteractor.startActionMode(actionModeCallback)
        }

        onVerseClicked(verse)
    }
}
