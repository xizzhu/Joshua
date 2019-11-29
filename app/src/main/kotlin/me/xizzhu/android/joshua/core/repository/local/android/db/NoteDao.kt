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

package me.xizzhu.android.joshua.core.repository.local.android.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.ask.db.*
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex

class NoteDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_NOTE = "note"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_NOTE = "note"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_NOTE) {
            it[COLUMN_BOOK_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_CHAPTER_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_VERSE_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_NOTE] = TEXT + NOT_NULL
            it[COLUMN_TIMESTAMP] = INTEGER + NOT_NULL
        }
    }

    fun read(@Constants.SortOrder sortOrder: Int): List<Note> = db.select(TABLE_NOTE)
            .apply {
                when (sortOrder) {
                    Constants.SORT_BY_DATE -> orderBy(COLUMN_TIMESTAMP, sortOrder = SortOrder.DESCENDING)
                    Constants.SORT_BY_BOOK -> orderBy(COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX, COLUMN_VERSE_INDEX)
                    else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
                }
            }.toList { row ->
                Note(
                        VerseIndex(
                                row.getInt(COLUMN_BOOK_INDEX),
                                row.getInt(COLUMN_CHAPTER_INDEX),
                                row.getInt(COLUMN_VERSE_INDEX)
                        ),
                        row.getString(COLUMN_NOTE),
                        row.getLong(COLUMN_TIMESTAMP)
                )
            }

    fun read(bookIndex: Int, chapterIndex: Int): List<Note> =
            db.select(TABLE_NOTE, COLUMN_VERSE_INDEX, COLUMN_NOTE, COLUMN_TIMESTAMP) {
                (COLUMN_BOOK_INDEX eq bookIndex) and (COLUMN_CHAPTER_INDEX eq chapterIndex)
            }
                    .orderBy(COLUMN_VERSE_INDEX)
                    .toList { row ->
                        Note(
                                VerseIndex(bookIndex, chapterIndex, row.getInt(COLUMN_VERSE_INDEX)),
                                row.getString(COLUMN_NOTE),
                                row.getLong(COLUMN_TIMESTAMP)
                        )
                    }

    fun read(verseIndex: VerseIndex): Note =
            db.select(TABLE_NOTE, COLUMN_NOTE, COLUMN_TIMESTAMP) {
                (COLUMN_BOOK_INDEX eq verseIndex.bookIndex) and (COLUMN_CHAPTER_INDEX eq verseIndex.chapterIndex) and (COLUMN_VERSE_INDEX eq verseIndex.verseIndex)
            }.firstOrDefault({ Note(verseIndex, "", -1L) }) { Note(verseIndex, it.getString(COLUMN_NOTE), it.getLong(COLUMN_TIMESTAMP)) }

    fun save(note: Note) {
        db.insert(TABLE_NOTE, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_BOOK_INDEX] = note.verseIndex.bookIndex
            it[COLUMN_CHAPTER_INDEX] = note.verseIndex.chapterIndex
            it[COLUMN_VERSE_INDEX] = note.verseIndex.verseIndex
            it[COLUMN_NOTE] = note.note
            it[COLUMN_TIMESTAMP] = note.timestamp
        }
    }

    fun save(notes: List<Note>) {
        db.transaction { notes.forEach { save(it) } }
    }

    fun remove(verseIndex: VerseIndex) {
        db.delete(TABLE_NOTE) {
            (COLUMN_BOOK_INDEX eq verseIndex.bookIndex) and (COLUMN_CHAPTER_INDEX eq verseIndex.chapterIndex) and (COLUMN_VERSE_INDEX eq verseIndex.verseIndex)
        }
    }
}
