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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ReadingProgressDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        val bookIndex = 1
        val chapterIndex = 2
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0L)
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveThenRead() {
        val bookIndex = 1
        val chapterIndex = 2
        val readCount = 3
        val lastReadingTimestamp = 987654321L
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, lastReadingTimestamp)

        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex, readCount, lastReadingTimestamp))
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveThenReadNonExist() {
        val bookIndex = 1
        val chapterIndex = 2
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0)

        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex + 1, chapterIndex, 1234, 5678L))
        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex + 1, 1234, 5678L))
        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex + 1, chapterIndex + 1, 1234, 5678L))
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveOverrideThenRead() {
        val bookIndex = 1
        val chapterIndex = 2
        val readCount = 3
        val lastReadingTimestamp = 987654321L
        val expected = ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, lastReadingTimestamp)

        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex, 12345, 54321L))
        androidDatabase.readingProgressDao.save(ReadingProgress.ChapterReadingStatus(
                bookIndex, chapterIndex, readCount, lastReadingTimestamp))
        val actual = androidDatabase.readingProgressDao.read(bookIndex, chapterIndex)
        assertEquals(expected, actual)
    }
}
