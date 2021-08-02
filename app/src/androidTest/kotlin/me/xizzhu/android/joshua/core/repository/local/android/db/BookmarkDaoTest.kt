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

package me.xizzhu.android.joshua.core.repository.local.android.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@SmallTest
class BookmarkDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.bookmarkDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.bookmarkDao.read(Constants.SORT_BY_BOOK).isEmpty())
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                assertTrue(androidDatabase.bookmarkDao.read(bookIndex, chapterIndex).isEmpty())
                assertFalse(androidDatabase.bookmarkDao.read(VerseIndex(bookIndex, chapterIndex, 0)).isValid())
            }
        }
    }

    @Test
    fun testSaveThenRead() {
        val bookmark1 = Bookmark(VerseIndex(1, 2, 3), 45678L)
        val bookmark2 = Bookmark(VerseIndex(1, 2, 4), 45679L)
        val bookmark3 = Bookmark(VerseIndex(1, 4, 3), 98765L)

        androidDatabase.bookmarkDao.save(bookmark1)
        androidDatabase.bookmarkDao.save(bookmark2)
        androidDatabase.bookmarkDao.save(bookmark3)

        assertEquals(listOf(bookmark3, bookmark2, bookmark1), androidDatabase.bookmarkDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(bookmark1, bookmark2, bookmark3), androidDatabase.bookmarkDao.read(Constants.SORT_BY_BOOK))
        assertEquals(listOf(bookmark1, bookmark2), androidDatabase.bookmarkDao.read(1, 2))
        assertEquals(listOf(bookmark3), androidDatabase.bookmarkDao.read(1, 4))
        assertEquals(bookmark1, androidDatabase.bookmarkDao.read(VerseIndex(1, 2, 3)))
        assertEquals(bookmark2, androidDatabase.bookmarkDao.read(VerseIndex(1, 2, 4)))
        assertEquals(bookmark3, androidDatabase.bookmarkDao.read(VerseIndex(1, 4, 3)))
    }

    @Test
    fun testSaveOverrideThenRead() {
        val bookmark1 = Bookmark(VerseIndex(1, 2, 3), 45678L)
        val bookmark2 = Bookmark(VerseIndex(1, 2, 4), 45679L)
        val bookmark3 = Bookmark(VerseIndex(1, 4, 3), 98765L)

        androidDatabase.bookmarkDao.save(Bookmark(VerseIndex(1, 2, 3), 1L))
        androidDatabase.bookmarkDao.save(Bookmark(VerseIndex(1, 2, 4), 2L))
        androidDatabase.bookmarkDao.save(Bookmark(VerseIndex(1, 4, 3), 3L))
        androidDatabase.bookmarkDao.save(listOf(bookmark1, bookmark2, bookmark3))

        assertEquals(listOf(bookmark3, bookmark2, bookmark1), androidDatabase.bookmarkDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(bookmark1, bookmark2, bookmark3), androidDatabase.bookmarkDao.read(Constants.SORT_BY_BOOK))
        assertEquals(listOf(bookmark1, bookmark2), androidDatabase.bookmarkDao.read(1, 2))
        assertEquals(listOf(bookmark3), androidDatabase.bookmarkDao.read(1, 4))
        assertEquals(bookmark1, androidDatabase.bookmarkDao.read(VerseIndex(1, 2, 3)))
        assertEquals(bookmark2, androidDatabase.bookmarkDao.read(VerseIndex(1, 2, 4)))
        assertEquals(bookmark3, androidDatabase.bookmarkDao.read(VerseIndex(1, 4, 3)))
    }

    @Test
    fun testRemoveNonExist() {
        androidDatabase.bookmarkDao.remove(VerseIndex(1, 2, 3))
        assertTrue(androidDatabase.bookmarkDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.bookmarkDao.read(1, 2).isEmpty())
        assertFalse(androidDatabase.bookmarkDao.read(VerseIndex(1, 2, 3)).isValid())
    }

    @Test
    fun testSaveThenRemove() {
        val bookmark = Bookmark(VerseIndex(1, 2, 3), 45678L)
        androidDatabase.bookmarkDao.save(bookmark)
        assertEquals(listOf(bookmark), androidDatabase.bookmarkDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(bookmark), androidDatabase.bookmarkDao.read(1, 2))

        androidDatabase.bookmarkDao.remove(bookmark.verseIndex)
        assertTrue(androidDatabase.bookmarkDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.bookmarkDao.read(1, 2).isEmpty())
    }
}
