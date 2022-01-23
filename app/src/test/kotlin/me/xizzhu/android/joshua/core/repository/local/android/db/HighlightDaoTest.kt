/*
 * Copyright (C) 2022 Xizhi Zhu
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

import android.graphics.Color
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class HighlightDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.highlightDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.highlightDao.read(Constants.SORT_BY_BOOK).isEmpty())
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                assertTrue(androidDatabase.highlightDao.read(bookIndex, chapterIndex).isEmpty())
                assertFalse(androidDatabase.highlightDao.read(VerseIndex(bookIndex, chapterIndex, 0)).isValid())
            }
        }
    }

    @Test
    fun testSaveThenRead() {
        val highlight1 = Highlight(VerseIndex(1, 2, 3), Color.RED, 45678L)
        val highlight2 = Highlight(VerseIndex(1, 2, 4), Color.GREEN, 45679L)
        val highlight3 = Highlight(VerseIndex(1, 4, 3), Color.CYAN, 98765L)

        androidDatabase.highlightDao.save(highlight1)
        androidDatabase.highlightDao.save(highlight2)
        androidDatabase.highlightDao.save(highlight3)

        assertEquals(listOf(highlight3, highlight2, highlight1), androidDatabase.highlightDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(highlight1, highlight2, highlight3), androidDatabase.highlightDao.read(Constants.SORT_BY_BOOK))
        assertEquals(listOf(highlight1, highlight2), androidDatabase.highlightDao.read(1, 2))
        assertEquals(listOf(highlight3), androidDatabase.highlightDao.read(1, 4))
        assertEquals(highlight1, androidDatabase.highlightDao.read(VerseIndex(1, 2, 3)))
        assertEquals(highlight2, androidDatabase.highlightDao.read(VerseIndex(1, 2, 4)))
        assertEquals(highlight3, androidDatabase.highlightDao.read(VerseIndex(1, 4, 3)))
    }

    @Test
    fun testSaveOverrideThenRead() {
        val highlight1 = Highlight(VerseIndex(1, 2, 3), Color.RED, 45678L)
        val highlight2 = Highlight(VerseIndex(1, 2, 4), Color.GREEN, 45679L)
        val highlight3 = Highlight(VerseIndex(1, 4, 3), Color.CYAN, 98765L)

        androidDatabase.highlightDao.save(Highlight(VerseIndex(1, 2, 3), 1, 1L))
        androidDatabase.highlightDao.save(Highlight(VerseIndex(1, 2, 4), 2, 2L))
        androidDatabase.highlightDao.save(Highlight(VerseIndex(1, 4, 3), 3, 3L))
        androidDatabase.highlightDao.save(listOf(highlight1, highlight2, highlight3))

        assertEquals(listOf(highlight3, highlight2, highlight1), androidDatabase.highlightDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(highlight1, highlight2, highlight3), androidDatabase.highlightDao.read(Constants.SORT_BY_BOOK))
        assertEquals(listOf(highlight1, highlight2), androidDatabase.highlightDao.read(1, 2))
        assertEquals(listOf(highlight3), androidDatabase.highlightDao.read(1, 4))
        assertEquals(highlight1, androidDatabase.highlightDao.read(VerseIndex(1, 2, 3)))
        assertEquals(highlight2, androidDatabase.highlightDao.read(VerseIndex(1, 2, 4)))
        assertEquals(highlight3, androidDatabase.highlightDao.read(VerseIndex(1, 4, 3)))
    }

    @Test
    fun testRemoveNonExist() {
        androidDatabase.highlightDao.remove(VerseIndex(1, 2, 3))
        assertTrue(androidDatabase.highlightDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.highlightDao.read(1, 2).isEmpty())
        assertFalse(androidDatabase.highlightDao.read(VerseIndex(1, 2, 3)).isValid())
    }

    @Test
    fun testSaveThenRemove() {
        val highlight = Highlight(VerseIndex(1, 2, 3), Color.BLACK, 45678L)
        androidDatabase.highlightDao.save(highlight)
        assertEquals(listOf(highlight), androidDatabase.highlightDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(highlight), androidDatabase.highlightDao.read(1, 2))

        androidDatabase.highlightDao.remove(highlight.verseIndex)
        assertTrue(androidDatabase.highlightDao.read(1, 2).isEmpty())
        assertTrue(androidDatabase.highlightDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertFalse(androidDatabase.highlightDao.read(VerseIndex(1, 2, 3)).isValid())
    }
}
