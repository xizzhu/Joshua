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
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.toMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidReadingStorageTest : BaseSqliteTest() {
    private lateinit var androidReadingStorage: AndroidReadingStorage

    @Before
    override fun setup() {
        super.setup()
        androidReadingStorage = AndroidReadingStorage(androidDatabase)
    }

    @Test
    fun testReadDefaultCurrentVerseIndex() {
        runBlocking {
            assertEquals(VerseIndex(0, 0, 0), androidReadingStorage.readCurrentVerseIndex())
        }
    }

    @Test
    fun testSaveThenReadCurrentVerseIndex() {
        runBlocking {
            val expected = VerseIndex(1, 2, 3)
            androidReadingStorage.saveCurrentVerseIndex(expected)
            assertEquals(expected, androidReadingStorage.readCurrentVerseIndex())
        }
    }

    @Test
    fun testSaveOverrideThenReadCurrentVerseIndex() {
        runBlocking {
            val expected = VerseIndex(1, 2, 3)
            androidReadingStorage.saveCurrentVerseIndex(VerseIndex(9, 8, 7))
            androidReadingStorage.saveCurrentVerseIndex(expected)
            assertEquals(expected, androidReadingStorage.readCurrentVerseIndex())
        }
    }

    @Test
    fun testReadDefaultCurrentTranslation() {
        runBlocking {
            assertEquals("", androidReadingStorage.readCurrentTranslation())
        }
    }

    @Test
    fun testSaveThenReadCurrentTranslation() {
        runBlocking {
            val expected = "KJV"
            androidReadingStorage.saveCurrentTranslation(expected)
            assertEquals(expected, androidReadingStorage.readCurrentTranslation())
        }
    }

    @Test
    fun testSaveOverrideThenReadCurrentTranslation() {
        runBlocking {
            val expected = "KJV"
            androidReadingStorage.saveCurrentTranslation("random")
            androidReadingStorage.saveCurrentTranslation(expected)
            assertEquals(expected, androidReadingStorage.readCurrentTranslation())
        }
    }

    @Test
    fun testReadBookNamesFromNonExistTranslation() {
        runBlocking {
            assertTrue(androidReadingStorage.readBookNames("not_exist").isEmpty())
        }
    }

    @Test
    fun testSaveThenReadBookNames() {
        runBlocking {
            androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames)
            assertEquals(MockContents.kjvBookNames, androidReadingStorage.readBookNames(MockContents.kjvShortName))
        }
    }

    @Test
    fun testSaveOverrideThenReadBookNames() {
        runBlocking {
            androidDatabase.bookNamesDao.save(MockContents.kjvShortName, listOf("random_1", "whatever_2"))
            androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames)
            assertEquals(MockContents.kjvBookNames, androidReadingStorage.readBookNames(MockContents.kjvShortName))
        }
    }

    @Test
    fun testReadVersesFromNonExistTranslation() {
        runBlocking {
            assertTrue(androidReadingStorage.readVerses("not_exist", 0, 0, "").isEmpty())
        }
    }

    @Test
    fun testSaveThenReadVerses() {
        runBlocking {
            saveTranslation()
            assertEquals(MockContents.kjvVerses, androidReadingStorage.readVerses(
                    MockContents.kjvShortName, 0, 0, MockContents.kjvBookNames[0]))
        }
    }

    private fun saveTranslation() {
        androidDatabase.translationDao.createTable(MockContents.kjvShortName)
        androidDatabase.translationDao.save(MockContents.kjvShortName, MockContents.kjvVerses.toMap())
    }

    @Test
    fun testSaveOverrideThenReadVerses() {
        runBlocking {
            androidDatabase.translationDao.createTable(MockContents.kjvShortName)
            androidDatabase.translationDao.save(MockContents.kjvShortName,
                    mapOf(Pair(Pair(0, 0), listOf("verse_1", "verse_2"))))
            androidDatabase.translationDao.save(MockContents.kjvShortName, MockContents.kjvVerses.toMap())
            assertEquals(MockContents.kjvVerses, androidReadingStorage.readVerses(
                    MockContents.kjvShortName, 0, 0, MockContents.kjvBookNames[0]))
        }
    }

    @Test
    fun testSaveThenReadWithParallelTranslations() {
        runBlocking {
            androidDatabase.bookNamesDao.save(MockContents.kjvShortName, MockContents.kjvBookNames)
            androidDatabase.translationDao.createTable(MockContents.kjvShortName)
            androidDatabase.translationDao.save(MockContents.kjvShortName, MockContents.kjvVerses.toMap())

            androidDatabase.bookNamesDao.save(MockContents.cuvShortName, MockContents.cuvBookNames)
            androidDatabase.translationDao.createTable(MockContents.cuvShortName)
            androidDatabase.translationDao.save(MockContents.cuvShortName, MockContents.cuvVerses.toMap())

            val actual = androidReadingStorage.readVerses(MockContents.kjvShortName,
                    listOf(MockContents.cuvShortName), 0, 0)
            for ((i, verse) in actual.withIndex()) {
                assertEquals(MockContents.kjvVerses[i].text, verse.text)
                assertEquals(listOf(MockContents.cuvVerses[i].text), verse.parallel)
            }
        }
    }

    @Test
    fun testSearchNonExistTranslation() {
        runBlocking {
            assertTrue(androidReadingStorage.search("not_exist", emptyList(), "keyword").isEmpty())
        }
    }

    @Test
    fun testSaveThenSearch() {
        runBlocking {
            saveTranslation()

            assertEquals(MockContents.kjvVerses, androidReadingStorage.search(
                    MockContents.kjvShortName, MockContents.kjvBookNames, "God"))
            assertEquals(MockContents.kjvVerses, androidReadingStorage.search(
                    MockContents.kjvShortName, MockContents.kjvBookNames, "god"))
            assertEquals(MockContents.kjvVerses, androidReadingStorage.search(
                    MockContents.kjvShortName, MockContents.kjvBookNames, "GOD"))
        }
    }

    @Test
    fun testSaveThenSearchMultiKeywords() {
        runBlocking {
            saveTranslation()

            assertEquals(listOf(MockContents.kjvVerses[0]), androidReadingStorage.search(
                    MockContents.kjvShortName, MockContents.kjvBookNames, "God created"))
            assertEquals(listOf(MockContents.kjvVerses[0]), androidReadingStorage.search(
                    MockContents.kjvShortName, MockContents.kjvBookNames, "beginning created"))
        }
    }
}
