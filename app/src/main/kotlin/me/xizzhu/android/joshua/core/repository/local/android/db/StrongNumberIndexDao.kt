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
import me.xizzhu.android.joshua.core.VerseIndex

class StrongNumberIndexDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_STRONG_NUMBER_INDEX = "strongNumberIndex"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_STRONG_NUMBER = "strongNumber"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_STRONG_NUMBER_INDEX) {
            it[COLUMN_BOOK_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_CHAPTER_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_VERSE_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_STRONG_NUMBER] = TEXT + NOT_NULL
        }
    }

    @WorkerThread
    fun read(verseIndex: VerseIndex): List<String> =
            db.select(TABLE_STRONG_NUMBER_INDEX, COLUMN_STRONG_NUMBER) {
                (COLUMN_BOOK_INDEX eq verseIndex.bookIndex) and
                        (COLUMN_CHAPTER_INDEX eq verseIndex.chapterIndex) and
                        (COLUMN_VERSE_INDEX eq verseIndex.verseIndex)
            }.firstOrDefault(emptyList()) { row -> row.getString(COLUMN_STRONG_NUMBER).split("-") }

    @WorkerThread
    fun replace(strongNumberIndexes: Map<VerseIndex, List<String>>) {
        db.transaction {
            deleteAll(TABLE_STRONG_NUMBER_INDEX)
            strongNumberIndexes.forEach { (verseIndex, list) ->
                insert(TABLE_STRONG_NUMBER_INDEX, SQLiteDatabase.CONFLICT_REPLACE) {
                    it[COLUMN_BOOK_INDEX] = verseIndex.bookIndex
                    it[COLUMN_CHAPTER_INDEX] = verseIndex.chapterIndex
                    it[COLUMN_VERSE_INDEX] = verseIndex.verseIndex
                    it[COLUMN_STRONG_NUMBER] = list.joinToString(separator = "-")
                }
            }
        }
    }

    @WorkerThread
    fun removeAll() {
        db.deleteAll(TABLE_STRONG_NUMBER_INDEX)
    }
}
