/*
 * Copyright (C) 2022 Xizhi Zhu
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
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex

abstract class VerseAnnotationDao<T : VerseAnnotation>(sqliteHelper: SQLiteOpenHelper, private val tableName: String) {
    companion object {
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    protected val db: SQLiteDatabase by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(tableName) {
            it[COLUMN_BOOK_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_CHAPTER_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_VERSE_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_TIMESTAMP] = INTEGER + NOT_NULL
            it.putCustomColumnModifiers()
        }
    }

    protected open fun MutableMap<String, ColumnModifiers>.putCustomColumnModifiers() {}

    @WorkerThread
    fun read(@Constants.SortOrder sortOrder: Int): List<T> = db.select(tableName)
            .apply {
                when (sortOrder) {
                    Constants.SORT_BY_DATE -> orderBy(COLUMN_TIMESTAMP, sortOrder = SortOrder.DESCENDING)
                    Constants.SORT_BY_BOOK -> orderBy(COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX, COLUMN_VERSE_INDEX)
                    else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
                }
            }.toList { row ->
                row.toVerseAnnotation(
                        VerseIndex(row.getInt(COLUMN_BOOK_INDEX), row.getInt(COLUMN_CHAPTER_INDEX), row.getInt(COLUMN_VERSE_INDEX)),
                        row.getLong(COLUMN_TIMESTAMP)
                )
            }

    protected abstract fun Map<String, Any?>.toVerseAnnotation(verseIndex: VerseIndex, timestamp: Long): T

    @WorkerThread
    fun read(bookIndex: Int, chapterIndex: Int): List<T> =
            db.select(tableName, COLUMN_VERSE_INDEX, COLUMN_TIMESTAMP, *getCustomColumns()) {
                (COLUMN_BOOK_INDEX eq bookIndex) and (COLUMN_CHAPTER_INDEX eq chapterIndex)
            }.orderBy(COLUMN_VERSE_INDEX).toList { row ->
                row.toVerseAnnotation(VerseIndex(bookIndex, chapterIndex, row.getInt(COLUMN_VERSE_INDEX)), row.getLong(COLUMN_TIMESTAMP))
            }

    protected open fun getCustomColumns(): Array<String> = emptyArray()

    @WorkerThread
    fun read(verseIndex: VerseIndex): T =
            db.select(tableName, COLUMN_TIMESTAMP, *getCustomColumns()) { eq(verseIndex) }
                    .firstOrDefault({ defaultVerseAnnotation(verseIndex) }) { row ->
                        row.toVerseAnnotation(verseIndex, row.getLong(COLUMN_TIMESTAMP))
                    }

    private fun ConditionBuilder.eq(verseIndex: VerseIndex): Condition =
            (COLUMN_BOOK_INDEX eq verseIndex.bookIndex) and (COLUMN_CHAPTER_INDEX eq verseIndex.chapterIndex) and (COLUMN_VERSE_INDEX eq verseIndex.verseIndex)

    protected abstract fun defaultVerseAnnotation(verseIndex: VerseIndex): T

    @WorkerThread
    fun search(query: String): List<T> {
        if (query.isBlank()) return emptyList()

        return buildQueryToSearchVerseAnnotations(query)
                .orderBy(COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX, COLUMN_VERSE_INDEX).toList { row ->
                    row.toVerseAnnotation(
                            VerseIndex(row.getInt(COLUMN_BOOK_INDEX), row.getInt(COLUMN_CHAPTER_INDEX), row.getInt(COLUMN_VERSE_INDEX)),
                            row.getLong(COLUMN_TIMESTAMP)
                    )
                }
    }

    protected abstract fun buildQueryToSearchVerseAnnotations(query: String): Query

    @WorkerThread
    fun save(verseAnnotation: T) {
        db.insert(tableName, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_BOOK_INDEX] = verseAnnotation.verseIndex.bookIndex
            it[COLUMN_CHAPTER_INDEX] = verseAnnotation.verseIndex.chapterIndex
            it[COLUMN_VERSE_INDEX] = verseAnnotation.verseIndex.verseIndex
            it[COLUMN_TIMESTAMP] = verseAnnotation.timestamp
            it.putCustomColumnValues(verseAnnotation)
        }
    }

    protected open fun MutableMap<String, Any?>.putCustomColumnValues(verseAnnotation: T) {}

    @WorkerThread
    fun save(verseAnnotations: List<T>) {
        db.transaction { verseAnnotations.forEach { save(it) } }
    }

    @WorkerThread
    fun remove(verseIndex: VerseIndex) {
        db.delete(tableName) { eq(verseIndex) }
    }
}
