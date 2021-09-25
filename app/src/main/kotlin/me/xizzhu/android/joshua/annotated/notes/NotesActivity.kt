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

package me.xizzhu.android.joshua.annotated.notes

import android.app.Application
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewModel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
        bibleReadingManager: BibleReadingManager,
        notesManager: VerseAnnotationManager<Note>,
        settingsManager: SettingsManager,
        application: Application
) : AnnotatedVersesViewModel<Note>(bibleReadingManager, notesManager, R.string.text_no_notes, settingsManager, application) {
    override fun buildBaseItem(annotatedVerse: Note, bookName: String, bookShortName: String, verseText: String, sortOrder: Int): BaseItem =
            NoteItem(annotatedVerse.verseIndex, bookShortName, verseText, annotatedVerse.note)
}

@AndroidEntryPoint
class NotesActivity : AnnotatedVersesActivity<Note>(R.string.title_notes) {
    override fun extrasForOpeningVerse(): Bundle = ReadingActivity.bundleForOpenNote()
}
