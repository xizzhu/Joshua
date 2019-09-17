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

package me.xizzhu.android.joshua.core.repository

import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalNoteStorage

class NoteRepository(private val localNoteStorage: LocalNoteStorage) {
    suspend fun readSortOrder(): Int = localNoteStorage.readSortOrder()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        localNoteStorage.saveSortOrder(sortOrder)
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<Note> = localNoteStorage.read(sortOrder)

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<Note> = localNoteStorage.read(bookIndex, chapterIndex)

    suspend fun read(verseIndex: VerseIndex): Note = localNoteStorage.read(verseIndex)

    suspend fun save(note: Note) {
        localNoteStorage.save(note)
    }

    suspend fun save(notes: List<Note>) {
        localNoteStorage.save(notes)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        localNoteStorage.remove(verseIndex)
    }
}
