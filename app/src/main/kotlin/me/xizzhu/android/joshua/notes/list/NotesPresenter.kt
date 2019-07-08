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

package me.xizzhu.android.joshua.notes.list

import android.content.res.Resources
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.notes.NotesInteractor
import me.xizzhu.android.joshua.ui.formatDate
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter
import me.xizzhu.android.logger.Log
import java.util.*
import kotlin.collections.ArrayList

class NotesPresenter(private val notesInteractor: NotesInteractor, private val resources: Resources)
    : BaseSettingsPresenter<NotesView>(notesInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            notesInteractor.observeNotesSortOrder().consumeEach { loadNotes(it) }
        }
    }

    fun loadNotes(@Constants.SortOrder sortOrder: Int) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                notesInteractor.notifyLoadingStarted()
                view?.onNotesLoadingStarted()

                val notes = notesInteractor.readNotes(sortOrder)
                if (notes.isEmpty()) {
                    view?.onNotesLoaded(listOf(TextItem(resources.getString(R.string.text_no_note))))
                } else {
                    val items = when (sortOrder) {
                        Constants.SORT_BY_DATE -> notes.toBaseItemsByDate()
                        Constants.SORT_BY_BOOK -> notes.toBaseItemsByBook()
                        else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
                    }
                    view?.onNotesLoaded(items)
                }

                notesInteractor.notifyLoadingFinished()
                view?.onNotesLoadingCompleted()
            } catch (e: Exception) {
                Log.e(tag, "Failed to load notes", e)
                view?.onNotesLoadFailed(sortOrder)
            }
        }
    }

    @VisibleForTesting
    suspend fun List<Note>.toBaseItemsByDate(): List<BaseItem> {
        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val currentTranslation = notesInteractor.readCurrentTranslation()
        val items: ArrayList<BaseItem> = ArrayList()
        for (note in this) {
            calendar.timeInMillis = note.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(note.timestamp.formatDate(resources, calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(NoteItem(note.verseIndex,
                    notesInteractor.readVerse(currentTranslation, note.verseIndex).text,
                    notesInteractor.readBookShortName(currentTranslation, note.verseIndex.bookIndex),
                    note.note, note.timestamp, this@NotesPresenter::selectVerse))
        }
        return items
    }

    @VisibleForTesting
    suspend fun List<Note>.toBaseItemsByBook(): List<BaseItem> {
        val currentTranslation = notesInteractor.readCurrentTranslation()
        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        for (note in this) {
            val verse = notesInteractor.readVerse(currentTranslation, note.verseIndex)
            if (note.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(verse.text.bookName, false))
                currentBookIndex = note.verseIndex.bookIndex
            }

            items.add(NoteItem(note.verseIndex, verse.text,
                    notesInteractor.readBookShortName(currentTranslation, note.verseIndex.bookIndex),
                    note.note, note.timestamp, this@NotesPresenter::selectVerse))
        }
        return items
    }

    fun selectVerse(verseToSelect: VerseIndex) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                notesInteractor.openReading(verseToSelect)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                view?.onVerseSelectionFailed(verseToSelect)
            }
        }
    }
}
