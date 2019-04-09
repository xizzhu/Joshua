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
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@SmallTest
class NoteDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.noteDao.read().isEmpty())
        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                assertFalse(androidDatabase.noteDao.read(VerseIndex(bookIndex, chapterIndex, 0)).isValid())
            }
        }
    }

    @Test
    fun testSaveThenRead() {
        val note1 = Note(VerseIndex(1, 2, 3), "note 1", 45678L)
        val note2 = Note(VerseIndex(1, 2, 4), "note 2", 45679L)
        val note3 = Note(VerseIndex(1, 4, 3), "note 3", 98765L)

        androidDatabase.noteDao.save(note1)
        androidDatabase.noteDao.save(note2)
        androidDatabase.noteDao.save(note3)

        assertEquals(listOf(note3, note2, note1), androidDatabase.noteDao.read())
        assertEquals(note1, androidDatabase.noteDao.read(VerseIndex(1, 2, 3)))
        assertEquals(note2, androidDatabase.noteDao.read(VerseIndex(1, 2, 4)))
        assertEquals(note3, androidDatabase.noteDao.read(VerseIndex(1, 4, 3)))
    }

    @Test
    fun testSaveOverrideThenRead() {
        val note1 = Note(VerseIndex(1, 2, 3), "note 1", 45678L)
        val note2 = Note(VerseIndex(1, 2, 4), "note 2", 45679L)
        val note3 = Note(VerseIndex(1, 4, 3), "note 3", 98765L)

        androidDatabase.noteDao.save(Note(VerseIndex(1, 2, 3), "fake note 1", 1L))
        androidDatabase.noteDao.save(Note(VerseIndex(1, 2, 4), "fake note 2", 2L))
        androidDatabase.noteDao.save(Note(VerseIndex(1, 4, 3), "fake note 3", 3L))
        androidDatabase.noteDao.save(note1)
        androidDatabase.noteDao.save(note2)
        androidDatabase.noteDao.save(note3)

        assertEquals(listOf(note3, note2, note1), androidDatabase.noteDao.read())
        assertEquals(note1, androidDatabase.noteDao.read(VerseIndex(1, 2, 3)))
        assertEquals(note2, androidDatabase.noteDao.read(VerseIndex(1, 2, 4)))
        assertEquals(note3, androidDatabase.noteDao.read(VerseIndex(1, 4, 3)))
    }

    @Test
    fun testRemoveNonExist() {
        androidDatabase.noteDao.remove(VerseIndex(1, 2, 3))
        assertTrue(androidDatabase.noteDao.read().isEmpty())
        assertFalse(androidDatabase.noteDao.read(VerseIndex(1, 2, 3)).isValid())
    }

    @Test
    fun testSaveThenRemove() {
        val note = Note(VerseIndex(1, 2, 3), "note 1", 45678L)
        androidDatabase.noteDao.save(note)
        assertEquals(listOf(note), androidDatabase.noteDao.read())

        androidDatabase.noteDao.remove(note.verseIndex)
        assertTrue(androidDatabase.noteDao.read().isEmpty())
    }
}
