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

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class HighlightDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                assertTrue(androidDatabase.highlightDao.read(bookIndex, chapterIndex).isEmpty())
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

        assertEquals(listOf(highlight1, highlight2), androidDatabase.highlightDao.read(1, 2))
        assertEquals(listOf(highlight3), androidDatabase.highlightDao.read(1, 4))
    }

    @Test
    fun testSaveOverrideThenRead() {
        val highlight1 = Highlight(VerseIndex(1, 2, 3), Color.RED, 45678L)
        val highlight2 = Highlight(VerseIndex(1, 2, 4), Color.GREEN, 45679L)
        val highlight3 = Highlight(VerseIndex(1, 4, 3), Color.CYAN, 98765L)

        androidDatabase.highlightDao.save(Highlight(VerseIndex(1, 2, 3), 1, 1L))
        androidDatabase.highlightDao.save(Highlight(VerseIndex(1, 2, 4), 2, 2L))
        androidDatabase.highlightDao.save(Highlight(VerseIndex(1, 4, 3), 3, 3L))
        androidDatabase.highlightDao.save(highlight1)
        androidDatabase.highlightDao.save(highlight2)
        androidDatabase.highlightDao.save(highlight3)

        assertEquals(listOf(highlight1, highlight2), androidDatabase.highlightDao.read(1, 2))
        assertEquals(listOf(highlight3), androidDatabase.highlightDao.read(1, 4))
    }

    @Test
    fun testRemoveNonExist() {
        androidDatabase.highlightDao.remove(VerseIndex(1, 2, 3))
        assertTrue(androidDatabase.highlightDao.read(1, 2).isEmpty())
    }

    @Test
    fun testSaveThenRemove() {
        val highlight = Highlight(VerseIndex(1, 2, 3), Color.BLACK, 45678L)
        androidDatabase.highlightDao.save(highlight)
        assertEquals(listOf(highlight), androidDatabase.highlightDao.read(1, 2))

        androidDatabase.highlightDao.remove(highlight.verseIndex)
        assertTrue(androidDatabase.highlightDao.read(1, 2).isEmpty())
    }
}
