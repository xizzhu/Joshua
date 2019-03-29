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
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class BookNamesDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.bookNamesDao.read("not_exist").isEmpty())

        for (bookIndex in 0 until Bible.BOOK_COUNT) {
            assertTrue(androidDatabase.bookNamesDao.read(bookIndex).isEmpty())
        }
    }

    @Test
    fun testSaveThenRead() {
        val translationShortName = MockContents.kjvShortName
        androidDatabase.bookNamesDao.save(translationShortName, MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        assertEquals(MockContents.kjvBookNames, androidDatabase.bookNamesDao.read(translationShortName))
    }

    @Test
    fun testSaveOverrideThenReadBookNames() {
        val translationShortName = MockContents.kjvShortName
        androidDatabase.bookNamesDao.save(translationShortName, listOf("random_1", "whatever_2"), listOf("ok_3", "fine_4"))
        androidDatabase.bookNamesDao.save(translationShortName, MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        assertEquals(MockContents.kjvBookNames, androidDatabase.bookNamesDao.read(translationShortName))
    }

    @Test
    fun testSaveThenReadParallel() {
        androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        androidDatabase.bookNamesDao.save(MockContents.cuvShortName, MockContents.cuvBookNames, MockContents.cuvBookShortNames)
        assertEquals(mapOf(Pair(MockContents.kjvShortName, MockContents.kjvBookNames[1]),
                Pair(MockContents.cuvShortName, MockContents.cuvBookNames[1])),
                androidDatabase.bookNamesDao.read(listOf(MockContents.kjvShortName, MockContents.cuvShortName), 1))
    }

    @Test
    fun testSaveOverrideThenReadParallel() {
        androidDatabase.bookNamesDao.save(MockContents.kjvShortName, listOf("random_1", "whatever_2"), listOf("ok_3", "fine_4"))
        androidDatabase.bookNamesDao.save(MockContents.cuvShortName, listOf("random_3", "whatever_4"), listOf("ok_1", "fine_2"))
        androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        androidDatabase.bookNamesDao.save(MockContents.cuvShortName, MockContents.cuvBookNames, MockContents.cuvBookShortNames)
        assertEquals(mapOf(Pair(MockContents.kjvShortName, MockContents.kjvBookNames[1]),
                Pair(MockContents.cuvShortName, MockContents.cuvBookNames[1])),
                androidDatabase.bookNamesDao.read(listOf(MockContents.kjvShortName, MockContents.cuvShortName), 1))
    }

    @Test
    fun testSaveThenReadByBookIndex() {
        androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        androidDatabase.bookNamesDao.save(MockContents.cuvShortName, MockContents.cuvBookNames, MockContents.cuvBookShortNames)

        val expected = mapOf(Pair(MockContents.kjvShortName, MockContents.kjvBookNames[0]),
                Pair(MockContents.cuvShortName, MockContents.cuvBookNames[0]))
        val actual = androidDatabase.bookNamesDao.read(0)
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveOverrideThenReadByBookIndex() {
        androidDatabase.bookNamesDao.save(MockContents.kjvShortName, listOf("random_1", "whatever_2"), listOf("ok_3", "fine_4"))
        androidDatabase.bookNamesDao.save(MockContents.cuvShortName, listOf("random_3", "whatever_4"), listOf("ok_2", "fine_1"))
        androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        androidDatabase.bookNamesDao.save(MockContents.cuvShortName, MockContents.cuvBookNames, MockContents.cuvBookShortNames)

        val expected = mapOf(Pair(MockContents.kjvShortName, MockContents.kjvBookNames[0]),
                Pair(MockContents.cuvShortName, MockContents.cuvBookNames[0]))
        val actual = androidDatabase.bookNamesDao.read(0)
        assertEquals(expected, actual)
    }

    @Test
    fun testRemoveNonExist() {
        val translationShortName = "not_exist"
        androidDatabase.bookNamesDao.remove(translationShortName)
        assertTrue(androidDatabase.bookNamesDao.read(translationShortName).isEmpty())
    }

    @Test
    fun testSaveThenRemove() {
        val translationShortName = MockContents.kjvShortName
        androidDatabase.bookNamesDao.save(translationShortName, MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        assertEquals(MockContents.kjvBookNames, androidDatabase.bookNamesDao.read(translationShortName))

        androidDatabase.bookNamesDao.remove(translationShortName)
        assertTrue(androidDatabase.bookNamesDao.read(translationShortName).isEmpty())
    }
}
