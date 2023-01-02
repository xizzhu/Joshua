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
import me.xizzhu.android.joshua.core.ReadingProgress

class ReadingProgressDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_READING_PROGRESS = "readingProgress"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_READ_COUNT = "readCount"
        private const val COLUMN_TIME_SPENT_IN_MILLS = "timeSpentInMills"
        private const val COLUMN_LAST_READING_TIMESTAMP = "lastReadingTimestamp"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_READING_PROGRESS) {
            it[COLUMN_BOOK_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_CHAPTER_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_READ_COUNT] = INTEGER + NOT_NULL
            it[COLUMN_TIME_SPENT_IN_MILLS] = INTEGER + NOT_NULL
            it[COLUMN_LAST_READING_TIMESTAMP] = INTEGER + NOT_NULL
        }
    }

    @WorkerThread
    fun save(chapterReadingStatus: ReadingProgress.ChapterReadingStatus) {
        db.insert(TABLE_READING_PROGRESS, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_BOOK_INDEX] = chapterReadingStatus.bookIndex
            it[COLUMN_CHAPTER_INDEX] = chapterReadingStatus.chapterIndex
            it[COLUMN_READ_COUNT] = chapterReadingStatus.readCount
            it[COLUMN_TIME_SPENT_IN_MILLS] = chapterReadingStatus.timeSpentInMillis
            it[COLUMN_LAST_READING_TIMESTAMP] = chapterReadingStatus.lastReadingTimestamp
        }
    }

    @WorkerThread
    fun read(): List<ReadingProgress.ChapterReadingStatus> =
            db.select(TABLE_READING_PROGRESS, COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX,
                    COLUMN_READ_COUNT, COLUMN_TIME_SPENT_IN_MILLS, COLUMN_LAST_READING_TIMESTAMP)
                    .orderBy(COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX)
                    .toList { row ->
                        ReadingProgress.ChapterReadingStatus(row.getInt(COLUMN_BOOK_INDEX), row.getInt(COLUMN_CHAPTER_INDEX),
                                row.getInt(COLUMN_READ_COUNT), row.getLong(COLUMN_TIME_SPENT_IN_MILLS), row.getLong(COLUMN_LAST_READING_TIMESTAMP))
                    }

    @WorkerThread
    fun read(bookIndex: Int, chapterIndex: Int): ReadingProgress.ChapterReadingStatus =
            db.select(TABLE_READING_PROGRESS, COLUMN_READ_COUNT, COLUMN_TIME_SPENT_IN_MILLS, COLUMN_LAST_READING_TIMESTAMP) {
                (COLUMN_BOOK_INDEX eq bookIndex) and (COLUMN_CHAPTER_INDEX eq chapterIndex)
            }.firstOrDefault({ ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0L, 0L) }) {
                ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, it.getInt(COLUMN_READ_COUNT),
                        it.getLong(COLUMN_TIME_SPENT_IN_MILLS), it.getLong(COLUMN_LAST_READING_TIMESTAMP))
            }
}
