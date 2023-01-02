/*
 * Copyright (C) 2023 Xizhi Zhu
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
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVerseItem
import me.xizzhu.android.joshua.annotated.AnnotatedVerseActivity
import me.xizzhu.android.joshua.annotated.AnnotatedVerseViewModel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.provider.TimeProvider
import me.xizzhu.android.joshua.reading.ReadingActivity
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    bibleReadingManager: BibleReadingManager,
    notesManager: VerseAnnotationManager<Note>,
    settingsManager: SettingsManager,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    timeProvider: TimeProvider,
    application: Application
) : AnnotatedVerseViewModel<Note>(
    bibleReadingManager = bibleReadingManager,
    verseAnnotationManager = notesManager,
    noItemText = R.string.text_no_notes,
    settingsManager = settingsManager,
    coroutineDispatcherProvider = coroutineDispatcherProvider,
    timeProvider = timeProvider,
    application = application
) {
    override fun buildAnnotatedVerseItem(
        settings: Settings,
        verseAnnotation: Note,
        bookName: String,
        bookShortName: String,
        verseText: String,
        sortOrder: Int
    ): AnnotatedVerseItem = AnnotatedVerseItem.Note(
        settings = settings,
        verseIndex = verseAnnotation.verseIndex,
        bookShortName = bookShortName,
        verseText = verseText,
        note = verseAnnotation.note
    )
}

@AndroidEntryPoint
class NotesActivity : AnnotatedVerseActivity<Note, NotesViewModel>(R.string.title_notes) {
    override val viewModel: NotesViewModel by viewModels()

    override fun extrasForOpeningVerse(): Bundle = ReadingActivity.bundleForOpenNote()
}
