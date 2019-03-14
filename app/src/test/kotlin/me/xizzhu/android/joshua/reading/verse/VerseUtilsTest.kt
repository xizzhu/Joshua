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

package me.xizzhu.android.joshua.reading.verse

import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VerseUtilsTest : BaseUnitTest() {
    @Test
    fun testPositionToBookIndex() {
        assertEquals(0, 0.toBookIndex())
        assertEquals(0, 49.toBookIndex())

        assertEquals(1, 50.toBookIndex())
        assertEquals(1, 55.toBookIndex())

        assertEquals(65, 1188.toBookIndex())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativePositionToBookIndex() {
        (-1).toBookIndex()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTooBigPositionToBookIndex() {
        Bible.TOTAL_CHAPTER_COUNT.toBookIndex()
    }

    @Test
    fun testPositionToChapterIndex() {
        assertEquals(0, 0.toChapterIndex())
        assertEquals(49, 49.toChapterIndex())

        assertEquals(0, 50.toChapterIndex())
        assertEquals(5, 55.toChapterIndex())

        assertEquals(21, 1188.toChapterIndex())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativePositionToChapterIndex() {
        (-1).toChapterIndex()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTooBigPositionToChapterIndex() {
        Bible.TOTAL_CHAPTER_COUNT.toChapterIndex()
    }

    @Test
    fun testVerseIndexToPosition() {
        assertEquals(0, VerseIndex(0, 0, 0).toPagePosition())
        assertEquals(49, VerseIndex(0, 49, 0).toPagePosition())

        assertEquals(50, VerseIndex(1, 0, 0).toPagePosition())
        assertEquals(55, VerseIndex(1, 5, 0).toPagePosition())

        assertEquals(1188, VerseIndex(65, 21, 0).toPagePosition())
    }

    @Test
    fun testIndexToPosition() {
        assertEquals(0, indexToPagePosition(0, 0))
        assertEquals(49, indexToPagePosition(0, 49))

        assertEquals(50, indexToPagePosition(1, 0))
        assertEquals(55, indexToPagePosition(1, 5))

        assertEquals(1188, indexToPagePosition(65, 21))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeBookIndexToPosition() {
        indexToPagePosition(-1, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTooBigBookIndexToPosition() {
        indexToPagePosition(Bible.BOOK_COUNT, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeChapterIndexToPosition() {
        indexToPagePosition(0, -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTooBigChapterIndexToPosition() {
        indexToPagePosition(0, Bible.getChapterCount(0))
    }

    @Test
    fun testEmptyVerseToStringForSharing() {
        assertTrue(emptyList<Verse>().toStringForSharing().isEmpty())
    }

    @Test
    fun testSingleVerseToStringForSharing() {
        val expected = "Genesis 1:1 In the beginning God created the heaven and the earth."
        val actual = listOf(MockContents.kjvVerses[0]).toStringForSharing()
        assertEquals(expected, actual)
    }

    @Test
    fun testMultipleVersesToStringForSharing() {
        val expected = "Genesis 1:1 In the beginning God created the heaven and the earth.\nGenesis 1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."
        val actual = listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1]).toStringForSharing()
        assertEquals(expected, actual)
    }

    @Test
    fun testMultipleVersesRandomOrderToStringForSharing() {
        val expected = "Genesis 1:1 In the beginning God created the heaven and the earth.\nGenesis 1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.\nGenesis 1:10 And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good."
        val actual = listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[9], MockContents.kjvVerses[1]).toStringForSharing()
        assertEquals(expected, actual)
    }
}
