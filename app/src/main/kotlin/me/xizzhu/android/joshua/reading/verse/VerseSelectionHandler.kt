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
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor

class VerseSelectionHandler(private val readingInteractor: ReadingInteractor) : ActionMode.Callback {
    private var actionMode: ActionMode? = null

    fun onVerseClicked(verseIndex: VerseIndex) {
        // TODO
    }

    fun onVerseLongClicked(verseIndex: VerseIndex) {
        if (actionMode == null) {
            actionMode = readingInteractor.startActionMode(this)
        }

        onVerseClicked(verseIndex)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.menu_verse_selection, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_copy -> true
            R.id.action_share -> true
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
    }
}
