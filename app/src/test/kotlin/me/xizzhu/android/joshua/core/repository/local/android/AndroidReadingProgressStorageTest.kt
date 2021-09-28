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

package me.xizzhu.android.joshua.core.repository.local.android

import android.text.format.DateUtils
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.ReadingProgress
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AndroidReadingProgressStorageTest : BaseSqliteTest() {
    private lateinit var androidReadingProgressStorage: AndroidReadingProgressStorage

    @BeforeTest
    override fun setup() {
        super.setup()
        androidReadingProgressStorage = AndroidReadingProgressStorage(androidDatabase)
    }

    @Test
    fun testEmptyReadingProgress() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0L, 0L)
            val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
            assertEquals(expected, actual)

            assertEquals(ReadingProgress(1, 0L, emptyList()),
                    androidReadingProgressStorage.read())
        }
    }

    @Test
    fun testTrackReadingProgress() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timeSpentInMills = 3L
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, timestamp)

            val expected = ReadingProgress(1, timestamp,
                    listOf(ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 1, timeSpentInMills, timestamp)))
            val actual = androidReadingProgressStorage.read()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testTrackMultipleReadingProgress() {
        runBlocking {
            val expected = ReadingProgress(1, 7L,
                    listOf(ReadingProgress.ChapterReadingStatus(3, 4, 1, 654L, 7L),
                            ReadingProgress.ChapterReadingStatus(3, 5, 1, 987L, 2L)))

            for (chapterReadingStatus in expected.chapterReadingStatus) {
                androidReadingProgressStorage.trackReadingProgress(
                        chapterReadingStatus.bookIndex, chapterReadingStatus.chapterIndex,
                        chapterReadingStatus.timeSpentInMillis, chapterReadingStatus.lastReadingTimestamp)
            }

            val actual = androidReadingProgressStorage.read()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testTrackReadingProgressEarlierTimestamp() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timeSpentInMills = 3L
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS
            val earlierTimestamp = timestamp - 6L * DateUtils.HOUR_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, earlierTimestamp)

            val expected = ReadingProgress(1, timestamp,
                    listOf(ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 1, timeSpentInMills, timestamp)))
            val actual = androidReadingProgressStorage.read()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testTrackReadingProgressSameDay() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timeSpentInMills = 3L
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS
            val updatedTimestamp = timestamp + 6L * DateUtils.HOUR_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, updatedTimestamp)

            val expected = ReadingProgress(1, updatedTimestamp,
                    listOf(ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 2, 2L * timeSpentInMills, updatedTimestamp)))
            val actual = androidReadingProgressStorage.read()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testTrackReadingProgressNextDay() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timeSpentInMills = 3L
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS + 6L * DateUtils.HOUR_IN_MILLIS
            val updatedTimestamp = timestamp + DateUtils.DAY_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, updatedTimestamp)

            val expected = ReadingProgress(2, updatedTimestamp,
                    listOf(ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 2, 2L * timeSpentInMills, updatedTimestamp)))
            val actual = androidReadingProgressStorage.read()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testTrackReadingProgressMoreThanOneDay() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            val timeSpentInMills = 3L
            val timestamp = 2L * DateUtils.DAY_IN_MILLIS + 6L * DateUtils.HOUR_IN_MILLIS
            val updatedTimestamp = timestamp + 5L * DateUtils.DAY_IN_MILLIS
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, timestamp)
            androidReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, updatedTimestamp)

            val expected = ReadingProgress(1, updatedTimestamp,
                    listOf(ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 2, 2L * timeSpentInMills, updatedTimestamp)))
            val actual = androidReadingProgressStorage.read()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testSaveThenRead() {
        runBlocking {
            val readingProgress = ReadingProgress(1, 2L, listOf(
                    ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                    ReadingProgress.ChapterReadingStatus(8, 9, 10, 11L, 12L)
            ))
            androidReadingProgressStorage.save(readingProgress)
            assertEquals(readingProgress, androidReadingProgressStorage.read())
        }
    }
}
