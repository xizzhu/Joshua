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

package me.xizzhu.android.joshua.core.repository.local.android

import android.database.sqlite.SQLiteDatabase
import android.text.format.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.repository.local.LocalReadingProgressStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.db.MetadataDao

class AndroidReadingProgressStorage(private val androidDatabase: AndroidDatabase) : LocalReadingProgressStorage {
    override suspend fun trackReadingProgress(bookIndex: Int, chapterIndex: Int, timeSpentInMills: Long, timestamp: Long) {
        withContext(Dispatchers.IO) {
            var db: SQLiteDatabase? = null
            try {
                db = androidDatabase.writableDatabase
                db.beginTransaction()

                val previousChapterReadingStatus = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
                if (previousChapterReadingStatus.lastReadingTimestamp < timestamp) {
                    val currentChapterReadingStatus = ReadingProgress.ChapterReadingStatus(
                            bookIndex, chapterIndex, previousChapterReadingStatus.readCount + 1,
                            previousChapterReadingStatus.timeSpentInMillis + timeSpentInMills, timestamp)
                    androidDatabase.readingProgressDao.save(currentChapterReadingStatus)
                }

                val values = androidDatabase.metadataDao.read(listOf(
                        Pair(MetadataDao.KEY_CONTINUOUS_READING_DAYS, "0"),
                        Pair(MetadataDao.KEY_LAST_READING_TIMESTAMP, "0")
                ))
                val lastReadingTimestamp = values.getValue(MetadataDao.KEY_LAST_READING_TIMESTAMP).toLong()
                if (lastReadingTimestamp < timestamp) {
                    // need to do the division first, in case e.g. lastReadingTimestamp was 10AM yesterday,
                    // and current timestamp is 4PM today, then (timestamp - lastReadingTimestamp) is
                    // bigger than 24 hours
                    val daysSinceLastReadingTimestamp = timestamp / DateUtils.DAY_IN_MILLIS -
                            lastReadingTimestamp / DateUtils.DAY_IN_MILLIS
                    val continuousReadingDays = when (daysSinceLastReadingTimestamp) {
                        0L -> {
                            values.getValue(MetadataDao.KEY_CONTINUOUS_READING_DAYS).toInt()
                        }
                        1L -> {
                            values.getValue(MetadataDao.KEY_CONTINUOUS_READING_DAYS).toInt() + 1
                        }
                        else -> {
                            1
                        }
                    }
                    androidDatabase.metadataDao.save(listOf(
                            Pair(MetadataDao.KEY_CONTINUOUS_READING_DAYS, continuousReadingDays.toString()),
                            Pair(MetadataDao.KEY_LAST_READING_TIMESTAMP, timestamp.toString())
                    ))
                }

                db.setTransactionSuccessful()
            } finally {
                if (db?.inTransaction() == true) {
                    db.endTransaction()
                }
            }
        }
    }
}
