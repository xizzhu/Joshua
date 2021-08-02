/*
 * Copyright (C) 2021 Xizhi Zhu
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

class BookNamesDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_BOOK_NAMES = "bookNames"
        private const val INDEX_BOOK_NAMES = "bookNamesIndex"
        private const val COLUMN_TRANSLATION_SHORT_NAME = "translationShortName"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_BOOK_NAME = "bookName"
        private const val COLUMN_BOOK_SHORT_NAME = "bookShortName"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_BOOK_NAMES) {
            it[COLUMN_TRANSLATION_SHORT_NAME] = TEXT + PRIMARY_KEY + NOT_NULL
            it[COLUMN_BOOK_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_BOOK_NAME] = TEXT + NOT_NULL
            it[COLUMN_BOOK_SHORT_NAME] = TEXT + NOT_NULL
        }
        db.createIndex(INDEX_BOOK_NAMES, TABLE_BOOK_NAMES, COLUMN_TRANSLATION_SHORT_NAME)
    }

    @WorkerThread
    fun read(translationShortName: String): List<String> =
            db.select(TABLE_BOOK_NAMES, COLUMN_BOOK_NAME) { COLUMN_TRANSLATION_SHORT_NAME eq translationShortName }
                    .orderBy(COLUMN_BOOK_INDEX)
                    .toList { it.getString(COLUMN_BOOK_NAME) }

    @WorkerThread
    fun readShortName(translationShortName: String): List<String> =
            db.select(TABLE_BOOK_NAMES, COLUMN_BOOK_SHORT_NAME) { COLUMN_TRANSLATION_SHORT_NAME eq translationShortName }
                    .orderBy(COLUMN_BOOK_INDEX)
                    .toList { it.getString(COLUMN_BOOK_SHORT_NAME) }

    @WorkerThread
    fun save(translationShortName: String, bookNames: List<String>, bookShortNames: List<String>) {
        db.transaction {
            val bookNameIterator = bookNames.iterator()
            val bookShortNameIterator = bookShortNames.iterator()
            var bookIndex = 0
            while (bookNameIterator.hasNext() && bookShortNameIterator.hasNext()) {
                db.insert(TABLE_BOOK_NAMES, SQLiteDatabase.CONFLICT_REPLACE) {
                    it[COLUMN_TRANSLATION_SHORT_NAME] = translationShortName
                    it[COLUMN_BOOK_INDEX] = bookIndex++
                    it[COLUMN_BOOK_NAME] = bookNameIterator.next()
                    it[COLUMN_BOOK_SHORT_NAME] = bookShortNameIterator.next()
                }
            }
        }
    }

    @WorkerThread
    fun remove(translationShortName: String) {
        db.delete(TABLE_BOOK_NAMES) { COLUMN_TRANSLATION_SHORT_NAME eq translationShortName }
    }

    @WorkerThread
    fun removeAll() {
        db.deleteAll(TABLE_BOOK_NAMES)
    }
}
