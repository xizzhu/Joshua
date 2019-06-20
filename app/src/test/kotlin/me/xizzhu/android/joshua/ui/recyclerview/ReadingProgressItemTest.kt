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

package me.xizzhu.android.joshua.ui.recyclerview

import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Test
import kotlin.test.assertEquals

class ReadingProgressItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(BaseItem.READING_PROGRESS_SUMMARY_ITEM,
                ReadingProgressSummaryItem(0, 0, 0, 0, 0).getItemViewType())
        assertEquals(BaseItem.READING_PROGRESS_DETAIL_ITEM, ReadingProgressDetailItem("", 0, emptyArray(), 0) { _, _ -> }.getItemViewType())
    }

    @Test
    fun testToReadingProgressItems() {
        val readingProgress = ReadingProgress(1, 23456L,
                listOf(ReadingProgress.ChapterReadingStatus(0, 1, 1, 500L, 23456L),
                        ReadingProgress.ChapterReadingStatus(0, 2, 2, 600L, 23457L),
                        ReadingProgress.ChapterReadingStatus(2, 0, 3, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 0, 1, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 1, 2, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 2, 3, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(7, 3, 4, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(63, 0, 1, 700L, 23458L),
                        ReadingProgress.ChapterReadingStatus(64, 0, 1, 700L, 23458L)))
        val actual = readingProgress.toReadingProgressItems(Array(Bible.BOOK_COUNT) { "" }.toList()) { _, _ -> }

        val actualSummaryItem = actual[0] as ReadingProgressSummaryItem
        assertEquals(1, actualSummaryItem.continuousReadingDays)
        assertEquals(9, actualSummaryItem.chaptersRead)
        assertEquals(3, actualSummaryItem.finishedBooks)
        assertEquals(1, actualSummaryItem.finishedOldTestament)
        assertEquals(2, actualSummaryItem.finishedNewTestament)

        assertEquals(Bible.BOOK_COUNT, actual.size - 1)
        for ((i, item) in actual.withIndex()) {
            if (i == 0) continue

            when (i - 1) {
                0 -> {
                    assertEquals(2, (item as ReadingProgressDetailItem).chaptersReadCount)
                }
                2 -> {
                    assertEquals(1, (item as ReadingProgressDetailItem).chaptersReadCount)
                }
                7 -> {
                    assertEquals(4, (item as ReadingProgressDetailItem).chaptersReadCount)
                }
                63 -> {
                    assertEquals(1, (item as ReadingProgressDetailItem).chaptersReadCount)
                }
                64 -> {
                    assertEquals(1, (item as ReadingProgressDetailItem).chaptersReadCount)
                }
                else -> {
                    assertEquals(0, (item as ReadingProgressDetailItem).chaptersReadCount)
                }
            }
        }
    }
}
