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
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.core.Bible
import java.lang.StringBuilder

class BookNamesDao(sqliteHelper: SQLiteOpenHelper) {
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
        db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_BOOK_NAME),
                "$COLUMN_TRANSLATION_SHORT_NAME = ?", arrayOf(translationShortName), null, null,
                "$COLUMN_BOOK_INDEX ASC").use {
            val bookNames = ArrayList<String>(Bible.BOOK_COUNT)
            while (it.moveToNext()) {
                bookNames.add(it.getString(0))
            }
            return bookNames
        }
    }

    @WorkerThread
    fun readShortName(translationShortName: String): List<String> {
        db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_BOOK_SHORT_NAME),
                "$COLUMN_TRANSLATION_SHORT_NAME = ?", arrayOf(translationShortName), null, null,
                "$COLUMN_BOOK_INDEX ASC").use {
            val bookNames = ArrayList<String>(Bible.BOOK_COUNT)
            while (it.moveToNext()) {
                bookNames.add(it.getString(0))
            }
            return bookNames
        }
    }

    @WorkerThread
    fun readShortName(translations: List<String>, bookIndex: Int): Map<String, String> {
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

        db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_TRANSLATION_SHORT_NAME, COLUMN_BOOK_SHORT_NAME),
                selection.toString(), selectionArgs, null, null, null).use {
            val bookNames = HashMap<String, String>(it.count)
            if (it.count > 0) {
                val translationShortName = it.getColumnIndex(COLUMN_TRANSLATION_SHORT_NAME)
                val bookName = it.getColumnIndex(COLUMN_BOOK_SHORT_NAME)
                while (it.moveToNext()) {
                    bookNames[it.getString(translationShortName)] = it.getString(bookName)
                }
            }
            return bookNames
        }
    }

    @WorkerThread
    fun read(translationShortName: String, bookIndex: Int): String {
        db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_BOOK_NAME),
                "$COLUMN_TRANSLATION_SHORT_NAME = ? AND $COLUMN_BOOK_INDEX = ?",
                arrayOf(translationShortName, bookIndex.toString()),
                null, null, null).use {
            return if (it.moveToNext()) {
                it.getString(0)
            } else {
                ""
            }
        }
    }

    @WorkerThread
    fun readShortName(translationShortName: String, bookIndex: Int): String {
        db.query(TABLE_BOOK_NAMES, arrayOf(COLUMN_BOOK_SHORT_NAME),
                "$COLUMN_TRANSLATION_SHORT_NAME = ? AND $COLUMN_BOOK_INDEX = ?",
                arrayOf(translationShortName, bookIndex.toString()),
                null, null, null).use {
            return if (it.moveToNext()) {
                it.getString(0)
            } else {
                ""
            }
        }
    }

    @WorkerThread
    fun save(translationShortName: String, bookNames: List<String>, bookShortNames: List<String>) {
        val values = ContentValues(4)
        with(values) {
            put(COLUMN_TRANSLATION_SHORT_NAME, translationShortName)
            for ((bookIndex, bookName) in bookNames.withIndex()) {
                put(COLUMN_BOOK_INDEX, bookIndex)
                put(COLUMN_BOOK_NAME, bookName)
                put(COLUMN_BOOK_SHORT_NAME, bookShortNames[bookIndex])
                db.insertWithOnConflict(TABLE_BOOK_NAMES, null, this, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }

    @WorkerThread
    fun remove(translationShortName: String) {
        db.delete(TABLE_BOOK_NAMES, "$COLUMN_TRANSLATION_SHORT_NAME = ?", arrayOf(translationShortName))
    }
}
