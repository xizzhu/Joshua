/*
 * Copyright (C) 2020 Xizhi Zhu
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

class StrongNumberReverseIndexDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_STRONG_NUMBER_REVERSE_INDEX = "strongNumberReverseIndex"
        private const val COLUMN_STRONG_NUMBER = "strongNumber"
        private const val COLUMN_VERSE_INDEXES = "verseIndexes"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_STRONG_NUMBER_REVERSE_INDEX) {
            it[COLUMN_STRONG_NUMBER] = TEXT + PRIMARY_KEY + NOT_NULL
            it[COLUMN_VERSE_INDEXES] = TEXT + NOT_NULL
        }
    }

    @WorkerThread
    fun read(strongNumber: String): List<VerseIndex> =
            db.select(TABLE_STRONG_NUMBER_REVERSE_INDEX, COLUMN_VERSE_INDEXES) { COLUMN_STRONG_NUMBER eq strongNumber }
                    .firstOrDefault(emptyList()) { row ->
                        row.getString(COLUMN_VERSE_INDEXES).split("-").map {
                            it.split(":").run { VerseIndex(get(0).toInt(), get(1).toInt(), get(2).toInt()) }
                        }
                    }

    @WorkerThread
    fun replace(strongNumberReverseIndexes: Map<String, List<VerseIndex>>) {
        db.transaction {
            deleteAll(TABLE_STRONG_NUMBER_REVERSE_INDEX)
            strongNumberReverseIndexes.forEach { (strongNumber, verseIndexes) ->
                insert(TABLE_STRONG_NUMBER_REVERSE_INDEX, SQLiteDatabase.CONFLICT_REPLACE) {
                    it[COLUMN_STRONG_NUMBER] = strongNumber
                    it[COLUMN_VERSE_INDEXES] = verseIndexes.joinToString(separator = "-") { verseIndex -> "${verseIndex.bookIndex}:${verseIndex.chapterIndex}:${verseIndex.verseIndex}" }
                }
            }
        }
    }
}
