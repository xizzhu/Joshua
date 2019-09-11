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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ReadingProgressDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        val bookIndex = 1
        val chapterIndex = 2
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0L, 0L)
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
        assertTrue(androidDatabase.readingProgressDao.read().isEmpty())
    }

    @Test
    fun testSaveThenRead() {
        val bookIndex = 1
        val chapterIndex = 2
        val readCount = 3
        val timeSpentInMills = 4L
        val lastReadingTimestamp = 987654321L
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, timeSpentInMills, lastReadingTimestamp)

        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex, readCount, timeSpentInMills, lastReadingTimestamp))
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveThenReadNonExist() {
        val bookIndex = 1
        val chapterIndex = 2
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0L, 0L)

        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex + 1, chapterIndex, 1234, 4321L, 5678L))
        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex + 1, 1234, 4321L, 5678L))
        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex + 1, chapterIndex + 1, 1234, 4321L, 5678L))
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveOverrideThenRead() {
        val bookIndex = 1
        val chapterIndex = 2
        val readCount = 3
        val timeSpentInMills = 4L
        val lastReadingTimestamp = 987654321L
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, timeSpentInMills, lastReadingTimestamp)

        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex, 12345, 67890L, 54321L))
        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex, readCount, timeSpentInMills, lastReadingTimestamp))
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveThenReadAll() {
        val expected = listOf(
                ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
        )
        // saves it in a "random" order to make sure sorting works when reading
        expected.asReversed().forEach { androidDatabase.readingProgressDao.save(it) }

        val actual = androidDatabase.readingProgressDao.read()
        assertEquals(expected, actual)
    }
}
