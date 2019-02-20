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

import org.junit.Assert
import org.junit.Test

class VerseIndexTest {
    @Test
    fun testValidIndex() {
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                Assert.assertTrue(VerseIndex(bookIndex, chapterIndex, 0).isValid())
            }
        }
    }

    @Test
    fun testInvalidBookIndex() {
        Assert.assertFalse(VerseIndex(-1, 0, 0).isValid())
        Assert.assertFalse(VerseIndex(Bible.BOOK_COUNT, 0, 0).isValid())
    }

    @Test
    fun testInvalidChapterIndex() {
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            Assert.assertFalse(VerseIndex(bookIndex, -1, 0).isValid())
            Assert.assertFalse(VerseIndex(bookIndex, Bible.getChapterCount(bookIndex), 0).isValid())
        }
    }

    @Test
    fun testInvalidVerseIndex() {
        Assert.assertFalse(VerseIndex(0, 0, -1).isValid())
    }
}
