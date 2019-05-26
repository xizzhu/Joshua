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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.NoteRepository

data class Note(val verseIndex: VerseIndex, val note: String, val timestamp: Long) {
    fun isValid(): Boolean = timestamp > 0L
}

class NoteManager(private val noteRepository: NoteRepository) {
    private val notesSortOrder: BroadcastChannel<Int> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            notesSortOrder.send(noteRepository.readSortOrder())
        }
    }

    fun observeSortOrder(): ReceiveChannel<Int> = notesSortOrder.openSubscription()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        noteRepository.saveSortOrder(sortOrder)
        notesSortOrder.send(sortOrder)
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<Note> = noteRepository.read(sortOrder)

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<Note> = noteRepository.read(bookIndex, chapterIndex)

    suspend fun read(verseIndex: VerseIndex): Note = noteRepository.read(verseIndex)

    suspend fun save(note: Note) {
        noteRepository.save(note)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        noteRepository.remove(verseIndex)
    }
}
