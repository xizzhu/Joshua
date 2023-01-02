/*
 * Copyright (C) 2023 Xizhi Zhu
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
import me.xizzhu.android.joshua.core.CrossReferences
import me.xizzhu.android.joshua.core.VerseIndex

class CrossReferencesDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_CROSS_REFERENCES = "crossReferences"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_REFERENCED_VERSES = "referencedVerses"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_CROSS_REFERENCES) {
            it[COLUMN_BOOK_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_CHAPTER_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_VERSE_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_REFERENCED_VERSES] = TEXT + NOT_NULL
        }
    }

    @WorkerThread
    fun read(verseIndex: VerseIndex): CrossReferences =
            CrossReferences(
                    verseIndex = verseIndex,
                    referenced = db.select(TABLE_CROSS_REFERENCES, COLUMN_REFERENCED_VERSES) {
                        (COLUMN_BOOK_INDEX eq verseIndex.bookIndex) and
                                (COLUMN_CHAPTER_INDEX eq verseIndex.chapterIndex) and
                                (COLUMN_VERSE_INDEX eq verseIndex.verseIndex)
                    }.firstOrDefault(emptyList()) { row ->
                        row.getString(COLUMN_REFERENCED_VERSES).split("-").map {
                            it.split(":").run { VerseIndex(get(0).toInt(), get(1).toInt(), get(2).toInt()) }
                        }
                    }
            )

    @WorkerThread
    fun replace(references: Map<VerseIndex, List<VerseIndex>>) {
        db.transaction {
            deleteAll(TABLE_CROSS_REFERENCES)
            references.forEach { (verseIndex, referenced) ->
                insert(TABLE_CROSS_REFERENCES, SQLiteDatabase.CONFLICT_REPLACE) {
                    it[COLUMN_BOOK_INDEX] = verseIndex.bookIndex
                    it[COLUMN_CHAPTER_INDEX] = verseIndex.chapterIndex
                    it[COLUMN_VERSE_INDEX] = verseIndex.verseIndex
                    it[COLUMN_REFERENCED_VERSES] = referenced.joinToString(separator = "-") { verseIndex ->
                        "${verseIndex.bookIndex}:${verseIndex.chapterIndex}:${verseIndex.verseIndex}"
                    }
                }
            }
        }
    }
}
