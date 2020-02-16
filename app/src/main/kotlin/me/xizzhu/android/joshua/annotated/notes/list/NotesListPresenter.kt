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

package me.xizzhu.android.joshua.annotated.notes.list

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
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
        notesActivity: NotesActivity, navigator: Navigator, annotatedVersesViewModel: BaseAnnotatedVersesViewModel<Note>,
        lifecycle: Lifecycle, lifecycleCoroutineScope: LifecycleCoroutineScope = lifecycle.coroutineScope
) : BaseAnnotatedVersesPresenter<Note>(notesActivity, navigator, R.string.text_no_note, annotatedVersesViewModel, lifecycle, lifecycleCoroutineScope) {
    override fun Note.toBaseItem(bookName: String, bookShortName: String, verseText: String, @Constants.SortOrder sortOrder: Int): BaseItem =
            NoteItem(verseIndex, bookShortName, verseText, note, ::openVerse)

    override fun extrasForOpeningVerse(): Bundle? = ReadingActivity.bundleForOpenNote()
}
