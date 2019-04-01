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

package me.xizzhu.android.joshua.core.repository.local.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.VerseIndex
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidBookmarkStorageTest : BaseSqliteTest() {
    private lateinit var androidBookmarkStorage: AndroidBookmarkStorage

    @Before
    override fun setup() {
        super.setup()
        androidBookmarkStorage = AndroidBookmarkStorage(androidDatabase)
    }

    @Test
    fun testEmpty() {
        runBlocking {
            assertTrue(androidBookmarkStorage.read().isEmpty())
            for (bookIndex in 0 until Bible.BOOK_COUNT) {
                for (chapterIndex in 0 until Bible.getChapterCount(bookIndex)) {
                    assertFalse(androidBookmarkStorage.read(VerseIndex(bookIndex, chapterIndex, 0)).isValid())
                }
            }
        }
    }

    @Test
    fun testSaveThenRead() {
        runBlocking {
            val bookmark1 = Bookmark(VerseIndex(1, 2, 3), 45678L)
            val bookmark2 = Bookmark(VerseIndex(1, 2, 4), 45679L)
            val bookmark3 = Bookmark(VerseIndex(1, 4, 3), 98765L)

            androidBookmarkStorage.save(bookmark1)
            androidBookmarkStorage.save(bookmark2)
            androidBookmarkStorage.save(bookmark3)

            assertEquals(listOf(bookmark3, bookmark2, bookmark1), androidBookmarkStorage.read())
            assertEquals(bookmark1, androidBookmarkStorage.read(VerseIndex(1, 2, 3)))
            assertEquals(bookmark2, androidBookmarkStorage.read(VerseIndex(1, 2, 4)))
            assertEquals(bookmark3, androidBookmarkStorage.read(VerseIndex(1, 4, 3)))
        }
    }

    @Test
    fun testSaveOverrideThenRead() {
        runBlocking {
            val bookmark1 = Bookmark(VerseIndex(1, 2, 3), 45678L)
            val bookmark2 = Bookmark(VerseIndex(1, 2, 4), 45679L)
            val bookmark3 = Bookmark(VerseIndex(1, 4, 3), 98765L)

            androidBookmarkStorage.save(Bookmark(VerseIndex(1, 2, 3), 1L))
            androidBookmarkStorage.save(Bookmark(VerseIndex(1, 2, 4), 2L))
            androidBookmarkStorage.save(Bookmark(VerseIndex(1, 4, 3), 3L))
            androidBookmarkStorage.save(bookmark1)
            androidBookmarkStorage.save(bookmark2)
            androidBookmarkStorage.save(bookmark3)

            assertEquals(listOf(bookmark3, bookmark2, bookmark1), androidBookmarkStorage.read())
            assertEquals(bookmark1, androidBookmarkStorage.read(VerseIndex(1, 2, 3)))
            assertEquals(bookmark2, androidBookmarkStorage.read(VerseIndex(1, 2, 4)))
            assertEquals(bookmark3, androidBookmarkStorage.read(VerseIndex(1, 4, 3)))
        }
    }

    @Test
    fun testRemoveNonExist() {
        runBlocking {
            androidBookmarkStorage.remove(VerseIndex(1, 2, 3))
            assertTrue(androidBookmarkStorage.read().isEmpty())
            assertFalse(androidBookmarkStorage.read(VerseIndex(1, 2, 3)).isValid())
        }
    }

    @Test
    fun testSaveThenRemove() {
        runBlocking {
            val bookmark = Bookmark(VerseIndex(1, 2, 3), 45678L)
            androidBookmarkStorage.save(bookmark)
            assertEquals(listOf(bookmark), androidBookmarkStorage.read())

            androidBookmarkStorage.remove(bookmark.verseIndex)
            assertTrue(androidBookmarkStorage.read().isEmpty())
        }
    }
}
