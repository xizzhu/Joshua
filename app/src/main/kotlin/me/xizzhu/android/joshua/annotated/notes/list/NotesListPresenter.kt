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

package me.xizzhu.android.joshua.annotated.notes.list

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesViewModel
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.notes.NotesActivity
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem

class NotesListPresenter(
        navigator: Navigator, annotatedVersesViewModel: BaseAnnotatedVersesViewModel<Note>,
        notesActivity: NotesActivity, coroutineScope: CoroutineScope = notesActivity.lifecycleScope
) : BaseAnnotatedVersesPresenter<Note, NotesActivity>(
        navigator, R.string.text_no_notes, annotatedVersesViewModel, notesActivity, coroutineScope
) {
    override fun Note.toBaseItem(bookName: String, bookShortName: String, verseText: String, @Constants.SortOrder sortOrder: Int): BaseItem =
            NoteItem(verseIndex, bookShortName, verseText, note, ::openVerse)

    override fun extrasForOpeningVerse(): Bundle? = ReadingActivity.bundleForOpenNote()
}
