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

package me.xizzhu.android.joshua.annotated.notes.list

import android.os.Bundle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.formatDate
import me.xizzhu.android.joshua.annotated.notes.NotesActivity
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import java.util.*
import kotlin.collections.ArrayList

class NotesListPresenter(private val notesActivity: NotesActivity,
                         navigator: Navigator,
                         notesListInteractor: NotesListInteractor,
                         dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseAnnotatedVersesPresenter<Note, NotesListInteractor>(
        notesActivity, navigator, R.string.text_no_note, notesListInteractor, dispatcher) {
    override suspend fun List<Note>.toBaseItemsByDate(): List<BaseItem> {
        val currentTranslation = interactor.readCurrentTranslation()
        val bookShortNames = interactor.readBookShortNames(currentTranslation)

        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<BaseItem> = ArrayList()
        forEach { note ->
            calendar.timeInMillis = note.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(note.timestamp.formatDate(notesActivity.resources, calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(NoteItem(note.verseIndex, bookShortNames[note.verseIndex.bookIndex],
                    interactor.readVerse(currentTranslation, note.verseIndex).text.text,
                    note.note, this@NotesListPresenter::openVerse))
        }
        return items
    }

    override suspend fun List<Note>.toBaseItemsByBook(): List<BaseItem> {
        val currentTranslation = interactor.readCurrentTranslation()
        val bookNames = interactor.readBookNames(currentTranslation)
        val bookShortNames = interactor.readBookShortNames(currentTranslation)

        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        forEach { note ->
            val verse = interactor.readVerse(currentTranslation, note.verseIndex)
            if (note.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookNames[note.verseIndex.bookIndex], false))
                currentBookIndex = note.verseIndex.bookIndex
            }

            items.add(NoteItem(note.verseIndex, bookShortNames[note.verseIndex.bookIndex],
                    verse.text.text, note.note, this@NotesListPresenter::openVerse))
        }
        return items
    }

    override fun extrasForOpeningVerse(): Bundle? = ReadingActivity.bundleForOpenNote()
}
