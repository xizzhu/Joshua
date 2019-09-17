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

package me.xizzhu.android.joshua.core

import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadingProgressTest : BaseUnitTest() {
    @Test
    fun testValidChapterReadingStatus() {
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                assertTrue(ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, 0, 0L, 0L).isValid())
            }
        }
    }

    @Test
    fun testInvalidChapterReadingStatus() {
        assertFalse(ReadingProgress.ChapterReadingStatus(-1, 0, 0, 0L, 0L).isValid())
        assertFalse(ReadingProgress.ChapterReadingStatus(Bible.BOOK_COUNT, 0, 0, 0L, 0L).isValid())
        assertFalse(ReadingProgress.ChapterReadingStatus(0, -1, 0, 0L, 0L).isValid())
        assertFalse(ReadingProgress.ChapterReadingStatus(0, Bible.getChapterCount(0), 0, 0L, 0L).isValid())
        assertFalse(ReadingProgress.ChapterReadingStatus(0, 0, -1, 0L, 0L).isValid())
        assertFalse(ReadingProgress.ChapterReadingStatus(0, 0, 0, -1L, 0L).isValid())
        assertFalse(ReadingProgress.ChapterReadingStatus(0, 0, 0, 0L, -1L).isValid())
    }

    @Test
    fun testValidReadingProgress() {
        assertTrue(ReadingProgress(0, 0L, listOf(ReadingProgress.ChapterReadingStatus(0, 0, 0, 0L, 0L))).isValid())
    }

    @Test
    fun testInvalidReadingProgress() {
        assertFalse(ReadingProgress(-1, 0L, listOf(ReadingProgress.ChapterReadingStatus(0, 0, 0, 0L, 0L))).isValid())
        assertFalse(ReadingProgress(0, -1L, listOf(ReadingProgress.ChapterReadingStatus(0, 0, 0, 0L, 0L))).isValid())
        assertFalse(ReadingProgress(0, 0L, listOf(ReadingProgress.ChapterReadingStatus(-1, 0, 0, 0L, 0L))).isValid())
        assertFalse(ReadingProgress(0, 0L, listOf(
                ReadingProgress.ChapterReadingStatus(0, 0, 0, 0L, 0L),
                ReadingProgress.ChapterReadingStatus(-1, 0, 0, 0L, 0L)
        )).isValid())
    }
}
