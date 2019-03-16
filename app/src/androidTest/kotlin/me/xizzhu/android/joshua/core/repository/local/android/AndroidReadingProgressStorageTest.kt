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

import android.text.format.DateUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.ReadingProgress
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidReadingProgressStorageTest : BaseSqliteTest() {
    private lateinit var androidReadingProgressStorage: AndroidReadingProgressStorage

    @Before
    override fun setup() {
        super.setup()
        androidReadingProgressStorage = AndroidReadingProgressStorage(androidDatabase)
    }

    @Test
    fun testEmptyReadingProgress() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0L)
            val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
            assertEquals(expected, actual)

            assertEquals(0L, androidDatabase.metadataDao.read(MetadataDao.KEY_LAST_READING_TIMESTAMP, "0").toLong())
            assertEquals(0, androidDatabase.metadataDao.read(MetadataDao.KEY_CONTINUOUS_READING_DAYS, "0").toInt())
        }
    }

    @Test
    fun testTrackReadingProgress() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timestamp)

            val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 1, timestamp)
            val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
            assertEquals(expected, actual)

            assertEquals(timestamp, androidDatabase.metadataDao.read(MetadataDao.KEY_LAST_READING_TIMESTAMP, "0").toLong())
            assertEquals(1, androidDatabase.metadataDao.read(MetadataDao.KEY_CONTINUOUS_READING_DAYS, "0").toInt())
        }
    }

    @Test
    fun testTrackReadingProgressEarlierTimestamp() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS
            val updatedTimestamp = timestamp - 6L * DateUtils.HOUR_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, updatedTimestamp)

            val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 1, timestamp)
            val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
            assertEquals(expected, actual)

            assertEquals(timestamp, androidDatabase.metadataDao.read(MetadataDao.KEY_LAST_READING_TIMESTAMP, "0").toLong())
            assertEquals(1, androidDatabase.metadataDao.read(MetadataDao.KEY_CONTINUOUS_READING_DAYS, "0").toInt())
        }
    }

    @Test
    fun testTrackReadingProgressSameDay() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS
            val updatedTimestamp = timestamp + 6L * DateUtils.HOUR_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, updatedTimestamp)

            val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 2, updatedTimestamp)
            val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
            assertEquals(expected, actual)

            assertEquals(updatedTimestamp, androidDatabase.metadataDao.read(MetadataDao.KEY_LAST_READING_TIMESTAMP, "0").toLong())
            assertEquals(1, androidDatabase.metadataDao.read(MetadataDao.KEY_CONTINUOUS_READING_DAYS, "0").toInt())
        }
    }

    @Test
    fun testTrackReadingProgressNextDay() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS + 6L * DateUtils.HOUR_IN_MILLIS
            val updatedTimestamp = timestamp + DateUtils.DAY_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, updatedTimestamp)

            val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 2, updatedTimestamp)
            val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
            assertEquals(expected, actual)

            assertEquals(updatedTimestamp, androidDatabase.metadataDao.read(MetadataDao.KEY_LAST_READING_TIMESTAMP, "0").toLong())
            assertEquals(2, androidDatabase.metadataDao.read(MetadataDao.KEY_CONTINUOUS_READING_DAYS, "0").toInt())
        }
    }

    @Test
    fun testTrackReadingProgressMoreThanOneDay() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS + 6L * DateUtils.HOUR_IN_MILLIS
            val updatedTimestamp = timestamp + 5L * DateUtils.DAY_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, updatedTimestamp)

            val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 2, updatedTimestamp)
            val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
            assertEquals(expected, actual)

            assertEquals(updatedTimestamp, androidDatabase.metadataDao.read(MetadataDao.KEY_LAST_READING_TIMESTAMP, "0").toLong())
            assertEquals(1, androidDatabase.metadataDao.read(MetadataDao.KEY_CONTINUOUS_READING_DAYS, "0").toInt())
        }
    }
}
