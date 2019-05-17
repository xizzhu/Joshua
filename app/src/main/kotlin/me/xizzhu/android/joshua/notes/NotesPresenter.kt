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

package me.xizzhu.android.joshua.notes

import android.content.res.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.ui.formatDate
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.NoteItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter
import java.util.*
import kotlin.collections.ArrayList

class NotesPresenter(private val notesInteractor: NotesInteractor, private val resources: Resources)
    : BaseSettingsPresenter<NotesView>(notesInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()
        loadNotes()
    }

    fun loadNotes() {
        launch(Dispatchers.Main) {
            try {
                val notes = notesInteractor.readNotes()
                if (notes.isEmpty()) {
                    view?.onNotesLoaded(listOf(TextItem(resources.getString(R.string.text_no_note))))
                } else {
                    val calendar = Calendar.getInstance()
                    var previousYear = -1
                    var previousDayOfYear = -1

                    val currentTranslation = notesInteractor.readCurrentTranslation()
                    val items: ArrayList<BaseItem> = ArrayList(notes.size)
                    for (note in notes) {
                        calendar.timeInMillis = note.timestamp
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                        if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                            items.add(TitleItem(note.timestamp.formatDate(resources)))

                            previousYear = currentYear
                            previousDayOfYear = currentDayOfYear
                        }

                        items.add(NoteItem(note.verseIndex,
                                notesInteractor.readVerse(currentTranslation, note.verseIndex).text,
                                note.note, note.timestamp))
                    }
                    view?.onNotesLoaded(items)
                }

                notesInteractor.notifyLoadingFinished()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load notes")
                view?.onNotesLoadFailed()
            }
        }
    }

    fun selectVerse(verseToSelect: VerseIndex) {
        launch(Dispatchers.Main) {
            try {
                notesInteractor.openReading(verseToSelect)
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to select verse and open reading activity")
                view?.onVerseSelectionFailed(verseToSelect)
            }
        }
    }
}
