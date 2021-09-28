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

import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class NoteDaoTest : BaseSqliteTest() {
    @Test
    fun testSearchWithEmptyQuery() {
        assertTrue(androidDatabase.noteDao.search("").isEmpty())
    }

    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.noteDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.noteDao.read(Constants.SORT_BY_BOOK).isEmpty())
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                assertTrue(androidDatabase.noteDao.read(bookIndex, chapterIndex).isEmpty())
                assertFalse(androidDatabase.noteDao.read(VerseIndex(bookIndex, chapterIndex, 0)).isValid())
            }
        }
        assertTrue(androidDatabase.noteDao.search("note").isEmpty())
    }

    @Test
    fun testSaveThenReadAndSearch() {
        val note1 = Note(VerseIndex(1, 2, 3), "note 1", 45678L)
        val note2 = Note(VerseIndex(1, 2, 4), "note 2", 45679L)
        val note3 = Note(VerseIndex(1, 4, 3), "note 3", 98765L)

        androidDatabase.noteDao.save(note1)
        androidDatabase.noteDao.save(note2)
        androidDatabase.noteDao.save(note3)

        assertEquals(listOf(note3, note2, note1), androidDatabase.noteDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(note1, note2, note3), androidDatabase.noteDao.read(Constants.SORT_BY_BOOK))
        assertEquals(listOf(note1, note2), androidDatabase.noteDao.read(1, 2))
        assertEquals(listOf(note3), androidDatabase.noteDao.read(1, 4))
        assertEquals(note1, androidDatabase.noteDao.read(VerseIndex(1, 2, 3)))
        assertEquals(note2, androidDatabase.noteDao.read(VerseIndex(1, 2, 4)))
        assertEquals(note3, androidDatabase.noteDao.read(VerseIndex(1, 4, 3)))
        assertEquals(listOf(note1, note2, note3), androidDatabase.noteDao.search("note"))
        assertEquals(listOf(note2), androidDatabase.noteDao.search("2"))
        assertEquals(listOf(note3), androidDatabase.noteDao.search("note 3"))
    }

    @Test
    fun testSaveOverrideThenReadAndSearch() {
        val note1 = Note(VerseIndex(1, 2, 3), "note 1", 45678L)
        val note2 = Note(VerseIndex(1, 2, 4), "note 2", 45679L)
        val note3 = Note(VerseIndex(1, 4, 3), "note 3", 98765L)

        androidDatabase.noteDao.save(Note(VerseIndex(1, 2, 3), "fake note 1", 1L))
        androidDatabase.noteDao.save(Note(VerseIndex(1, 2, 4), "fake note 2", 2L))
        androidDatabase.noteDao.save(Note(VerseIndex(1, 4, 3), "fake note 3", 3L))
        androidDatabase.noteDao.save(listOf(note1, note2, note3))

        assertEquals(listOf(note3, note2, note1), androidDatabase.noteDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(note1, note2, note3), androidDatabase.noteDao.read(Constants.SORT_BY_BOOK))
        assertEquals(listOf(note1, note2), androidDatabase.noteDao.read(1, 2))
        assertEquals(listOf(note3), androidDatabase.noteDao.read(1, 4))
        assertEquals(note1, androidDatabase.noteDao.read(VerseIndex(1, 2, 3)))
        assertEquals(note2, androidDatabase.noteDao.read(VerseIndex(1, 2, 4)))
        assertEquals(note3, androidDatabase.noteDao.read(VerseIndex(1, 4, 3)))
        assertEquals(listOf(note1, note2, note3), androidDatabase.noteDao.search("note"))
        assertEquals(listOf(note2), androidDatabase.noteDao.search("2"))
        assertEquals(listOf(note3), androidDatabase.noteDao.search("note 3"))
    }

    @Test
    fun testRemoveNonExist() {
        androidDatabase.noteDao.remove(VerseIndex(1, 2, 3))
        assertTrue(androidDatabase.noteDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.noteDao.read(1, 2).isEmpty())
        assertFalse(androidDatabase.noteDao.read(VerseIndex(1, 2, 3)).isValid())
        assertTrue(androidDatabase.noteDao.search("note").isEmpty())
    }

    @Test
    fun testSaveThenRemove() {
        val note = Note(VerseIndex(1, 2, 3), "note 1", 45678L)
        androidDatabase.noteDao.save(note)
        assertEquals(listOf(note), androidDatabase.noteDao.read(Constants.SORT_BY_DATE))
        assertEquals(listOf(note), androidDatabase.noteDao.read(1, 2))

        androidDatabase.noteDao.remove(note.verseIndex)
        assertTrue(androidDatabase.noteDao.read(Constants.SORT_BY_DATE).isEmpty())
        assertTrue(androidDatabase.noteDao.read(1, 2).isEmpty())
        assertTrue(androidDatabase.noteDao.search("note").isEmpty())
    }
}
