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
import me.xizzhu.android.joshua.core.Bible
import java.lang.StringBuilder

class BookNamesDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_BOOK_NAMES = "bookNames"
        private const val INDEX_BOOK_NAMES = "bookNamesIndex"
        private const val COLUMN_TRANSLATION_SHORT_NAME = "translationShortName"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_BOOK_NAME = "bookName"
        private const val COLUMN_BOOK_SHORT_NAME = "bookShortName"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_BOOK_NAMES (" +
                    "$COLUMN_TRANSLATION_SHORT_NAME TEXT NOT NULL, " +
                    "$COLUMN_BOOK_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_BOOK_NAME TEXT NOT NULL, " +
                    "$COLUMN_BOOK_SHORT_NAME TEXT NOT NULL, " +
                    "PRIMARY KEY ($COLUMN_TRANSLATION_SHORT_NAME, $COLUMN_BOOK_INDEX));")
            db.execSQL("CREATE INDEX $INDEX_BOOK_NAMES ON $TABLE_BOOK_NAMES ($COLUMN_TRANSLATION_SHORT_NAME);")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun read(translationShortName: String): List<String> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_BOOK_NAME),
                    "$COLUMN_TRANSLATION_SHORT_NAME = ?", arrayOf(translationShortName), null, null,
                    "$COLUMN_BOOK_INDEX ASC")
            val bookNames = ArrayList<String>(Bible.BOOK_COUNT)
            while (cursor.moveToNext()) {
                bookNames.add(cursor.getString(0))
            }
            return bookNames
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun read(translations: List<String>, bookIndex: Int): Map<String, String> {
        if (translations.isEmpty() || bookIndex < 0 || bookIndex >= Bible.BOOK_COUNT) {
            return emptyMap()
        }

        val selection = StringBuilder()
        val selectionArgs = Array(translations.size + 1) { "" }
        selection.append('(')
        for ((i, translation) in translations.withIndex()) {
            if (i > 0) {
                selection.append(" OR ")
            }
            selection.append("$COLUMN_TRANSLATION_SHORT_NAME = ?")
            selectionArgs[i] = translation
        }

        selection.append(") AND ($COLUMN_BOOK_INDEX = ?)")
        selectionArgs[translations.size] = bookIndex.toString()

        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_TRANSLATION_SHORT_NAME, COLUMN_BOOK_NAME),
                    selection.toString(), selectionArgs, null, null, null)
            val bookNames = HashMap<String, String>(translations.size)
            if (cursor.count > 0) {
                val translationShortName = cursor.getColumnIndex(COLUMN_TRANSLATION_SHORT_NAME)
                val bookName = cursor.getColumnIndex(COLUMN_BOOK_NAME)
                while (cursor.moveToNext()) {
                    bookNames[cursor.getString(translationShortName)] = cursor.getString(bookName)
                }
            }
            return bookNames
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun read(bookIndex: Int): Map<String, String> {
        if (bookIndex < 0 || bookIndex >= Bible.BOOK_COUNT) {
            return emptyMap()
        }

        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_TRANSLATION_SHORT_NAME, COLUMN_BOOK_NAME),
                    "$COLUMN_BOOK_INDEX = ?", arrayOf(bookIndex.toString()), null, null, null)
            val count = cursor.count
            if (count > 0) {
                val bookNames = HashMap<String, String>(count)
                val translationShortName = cursor.getColumnIndex(COLUMN_TRANSLATION_SHORT_NAME)
                val bookName = cursor.getColumnIndex(COLUMN_BOOK_NAME)
                while (cursor.moveToNext()) {
                    bookNames[cursor.getString(translationShortName)] = cursor.getString(bookName)
                }
                return bookNames
            }
            return emptyMap()
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun save(translationShortName: String, bookNames: List<String>, bookShortNames: List<String>) {
        val values = ContentValues(3)
        values.put(COLUMN_TRANSLATION_SHORT_NAME, translationShortName)
        for ((bookIndex, bookName) in bookNames.withIndex()) {
            values.put(COLUMN_BOOK_INDEX, bookIndex)
            values.put(COLUMN_BOOK_NAME, bookName)
            values.put(COLUMN_BOOK_SHORT_NAME, bookShortNames[bookIndex])
            db.insertWithOnConflict(TABLE_BOOK_NAMES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    @WorkerThread
    fun remove(translationShortName: String) {
        db.delete(TABLE_BOOK_NAMES, "$COLUMN_TRANSLATION_SHORT_NAME = ?", arrayOf(translationShortName))
    }
}
