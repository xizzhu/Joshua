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

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex

class NoteDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_NOTE = "note"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_NOTE = "note"
        private const val COLUMN_TIMESTAMP = "timestamp"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_NOTE (" +
                    "$COLUMN_BOOK_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_CHAPTER_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_VERSE_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_NOTE TEXT NOT NULL, " +
                    "$COLUMN_TIMESTAMP INTEGER NOT NULL, " +
                    "PRIMARY KEY ($COLUMN_BOOK_INDEX, $COLUMN_CHAPTER_INDEX, $COLUMN_VERSE_INDEX));")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    fun read(): List<Note> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_NOTE, null, null, null, null, null, "$COLUMN_TIMESTAMP DESC")
            return with(cursor) {
                val notes = ArrayList<Note>(count)
                val bookIndex = getColumnIndex(COLUMN_BOOK_INDEX)
                val chapterIndex = getColumnIndex(COLUMN_CHAPTER_INDEX)
                val verseIndex = getColumnIndex(COLUMN_VERSE_INDEX)
                val note = getColumnIndex(COLUMN_NOTE)
                val timestamp = getColumnIndex(COLUMN_TIMESTAMP)
                while (moveToNext()) {
                    notes.add(Note(VerseIndex(getInt(bookIndex), getInt(chapterIndex), getInt(verseIndex)),
                            getString(note), getLong(timestamp)))
                }
                return@with notes
            }
        } finally {
            cursor?.close()
        }
    }

    fun read(bookIndex: Int, chapterIndex: Int): List<Note> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_NOTE, arrayOf(COLUMN_NOTE, COLUMN_VERSE_INDEX, COLUMN_TIMESTAMP),
                    "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ?",
                    arrayOf(bookIndex.toString(), chapterIndex.toString()),
                    null, null, "$COLUMN_VERSE_INDEX ASC")
            return with(cursor) {
                val notes = ArrayList<Note>(count)
                val verseIndex = getColumnIndex(COLUMN_VERSE_INDEX)
                val note = getColumnIndex(COLUMN_NOTE)
                val timestamp = getColumnIndex(COLUMN_TIMESTAMP)
                while (moveToNext()) {
                    notes.add(Note(VerseIndex(bookIndex, chapterIndex, getInt(verseIndex)),
                            getString(note), getLong(timestamp)))
                }
                return@with notes
            }
        } finally {
            cursor?.close()
        }
    }

    fun read(verseIndex: VerseIndex): Note {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_NOTE, arrayOf(COLUMN_NOTE, COLUMN_TIMESTAMP),
                    "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ? AND $COLUMN_VERSE_INDEX = ?",
                    arrayOf(verseIndex.bookIndex.toString(), verseIndex.chapterIndex.toString(), verseIndex.verseIndex.toString()),
                    null, null, null)
            return with(cursor) {
                return@with if (moveToNext()) {
                    Note(verseIndex, getString(getColumnIndex(COLUMN_NOTE)), getLong(getColumnIndex(COLUMN_TIMESTAMP)))
                } else {
                    Note(verseIndex, "", -1L)
                }
            }
        } finally {
            cursor?.close()
        }
    }

    fun save(note: Note) {
        val values = ContentValues(5)
        with(values) {
            put(COLUMN_BOOK_INDEX, note.verseIndex.bookIndex)
            put(COLUMN_CHAPTER_INDEX, note.verseIndex.chapterIndex)
            put(COLUMN_VERSE_INDEX, note.verseIndex.verseIndex)
            put(COLUMN_NOTE, note.note)
            put(COLUMN_TIMESTAMP, note.timestamp)
        }
        db.insertWithOnConflict(TABLE_NOTE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun remove(verseIndex: VerseIndex) {
        db.delete(TABLE_NOTE, "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ? AND $COLUMN_VERSE_INDEX = ?",
                arrayOf(verseIndex.bookIndex.toString(), verseIndex.chapterIndex.toString(), verseIndex.verseIndex.toString()))
    }
}
