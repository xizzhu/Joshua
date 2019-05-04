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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.ui.recyclerview.NoteItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter

class NotesPresenter(private val notesInteractor: NotesInteractor)
    : BaseSettingsPresenter<NotesView>(notesInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()
        loadNotes()
    }

    fun loadNotes() {
        launch(Dispatchers.Main) {
            try {
                val currentTranslation = notesInteractor.readCurrentTranslation()
                val notes: ArrayList<NoteItem> = ArrayList()
                for (note in notesInteractor.readNotes()) {
                    notes.add(NoteItem(note.verseIndex,
                            notesInteractor.readVerse(currentTranslation, note.verseIndex).text,
                            note.note, note.timestamp))
                }
                view?.onNotesLoaded(notes)

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
                notesInteractor.selectVerse(verseToSelect)
                notesInteractor.openReading()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to select verse and open reading activity")
                view?.onVerseSelectionFailed(verseToSelect)
            }
        }
    }
}
