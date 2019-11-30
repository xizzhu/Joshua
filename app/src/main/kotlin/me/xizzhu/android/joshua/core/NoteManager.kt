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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.VerseAnnotationRepository
import me.xizzhu.android.logger.Log

data class Note(override val verseIndex: VerseIndex, val note: String, override val timestamp: Long) : VerseAnnotation(verseIndex, timestamp)

class NoteManager(private val noteRepository: VerseAnnotationRepository<Note>) {
    companion object {
        private val TAG = NoteManager::class.java.simpleName
    }

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val notesSortOrder: BroadcastChannel<Int> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                notesSortOrder.offer(noteRepository.readSortOrder())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize note sort order", e)
                notesSortOrder.offer(Constants.DEFAULT_SORT_ORDER)
            }
        }
    }

    fun observeSortOrder(): Flow<Int> = notesSortOrder.asFlow()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        noteRepository.saveSortOrder(sortOrder)
        notesSortOrder.offer(sortOrder)
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<Note> = noteRepository.read(sortOrder)

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<Note> = noteRepository.read(bookIndex, chapterIndex)

    suspend fun read(verseIndex: VerseIndex): Note = noteRepository.read(verseIndex)

    suspend fun save(note: Note) {
        noteRepository.save(note)
    }

    suspend fun save(notes: List<Note>) {
        noteRepository.save(notes)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        noteRepository.remove(verseIndex)
    }
}
