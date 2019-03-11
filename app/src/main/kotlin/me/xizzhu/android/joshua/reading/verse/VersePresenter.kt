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
import androidx.appcompat.view.ActionMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.onEach

class VersePresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<VerseView>() {
    companion object {
        private val TAG: String = VersePresenter::class.java.simpleName
    }

    private val selectedVerses: HashSet<VerseIndex> = HashSet()
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
                    mode.finish()
                    true
                }
                R.id.action_share -> {
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

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            receiveChannels.add(readingInteractor.observeCurrentTranslation()
                    .filter { it.isNotEmpty() }
                    .onEach {
                        view?.onCurrentTranslationUpdated(it)
                    })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(readingInteractor.observeCurrentVerseIndex()
                    .filter { it.isValid() }
                    .onEach {
                        actionMode?.finish()
                        view?.onCurrentVerseIndexUpdated(it)
                    })
        }
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

    fun loadVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int) {
        launch(Dispatchers.Main) {
            try {
                view?.onVersesLoaded(bookIndex, chapterIndex,
                        readingInteractor.readVerses(translationShortName, bookIndex, chapterIndex))
            } catch (e: Exception) {
                Log.e(TAG, e, "Failed to load verses")
                view?.onVersesLoadFailed(translationShortName, bookIndex, chapterIndex)
            }
        }
    }

    fun onVerseClicked(verseIndex: VerseIndex) {
        if (selectedVerses.contains(verseIndex)) {
            // de-select the verse
            selectedVerses.remove(verseIndex)
            if (selectedVerses.isEmpty()) {
                actionMode?.finish()
            }

            view?.onVerseDeselected(verseIndex)
        } else {
            // select the verse
            selectedVerses.add(verseIndex)

            view?.onVerseSelected(verseIndex)
        }
    }

    fun onVerseLongClicked(verseIndex: VerseIndex) {
        if (actionMode == null) {
            actionMode = readingInteractor.startActionMode(actionModeCallback)
        }

        onVerseClicked(verseIndex)
    }
}
