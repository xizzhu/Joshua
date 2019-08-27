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
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.toMap
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TranslationDaoTest : BaseSqliteTest() {
    @Test
    fun testReadNonExistTranslation() {
        assertTrue(androidDatabase.translationDao.read("not_exist", 0, 0).isEmpty())
    }

    @Test
    fun testCreateSameTable() {
        androidDatabase.translationDao.createTable(MockContents.kjvShortName)
        androidDatabase.translationDao.createTable(MockContents.kjvShortName)
    }

    @Test
    fun testSaveThenRead() {
        saveKjv()
        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.read(MockContents.kjvShortName, 0, 0))
    }

    private fun saveKjv() {
        saveTranslation(MockContents.kjvShortName, MockContents.kjvVerses)
    }

    private fun saveCuv() {
        saveTranslation(MockContents.cuvShortName, MockContents.cuvVerses)
    }

    private fun saveTranslation(translationShortName: String, verses: List<Verse>) {
        androidDatabase.translationDao.createTable(translationShortName)
        androidDatabase.translationDao.save(translationShortName, verses.toMap())
    }

    @Test
    fun testSaveOverrideThenReadVerses() {
        androidDatabase.translationDao.createTable(MockContents.kjvShortName)
        androidDatabase.translationDao.save(MockContents.kjvShortName,
                mapOf(Pair(Pair(0, 0), listOf("verse_1", "verse_2"))))
        androidDatabase.translationDao.save(MockContents.kjvShortName, MockContents.kjvVerses.toMap())
        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.read(MockContents.kjvShortName, 0, 0))
    }

    @Test
    fun testSaveThenReadWithParallelTranslations() {
        saveKjv()
        saveCuv()

        val actual = androidDatabase.translationDao.read(listOf(MockContents.kjvShortName, MockContents.cuvShortName), 0, 0)
        for ((translation, texts) in actual) {
            when (translation) {
                MockContents.kjvShortName -> {
                    assertEquals(MockContents.kjvVerses.size, texts.size)
                    for (i in MockContents.kjvVerses.indices) {
                        assertEquals(MockContents.kjvVerses[i].text, texts[i])
                    }
                }
                MockContents.cuvShortName -> {
                    assertEquals(MockContents.cuvVerses.size, texts.size)
                    for (i in MockContents.cuvVerses.indices) {
                        assertEquals(MockContents.cuvVerses[i].text, texts[i])
                    }
                }
                else -> fail()
            }
        }
    }

    @Test
    fun testSaveThenReadByVerseIndex() {
        saveKjv()
        saveCuv()

        val actual = androidDatabase.translationDao.read(listOf(MockContents.kjvShortName, MockContents.cuvShortName),
                VerseIndex(0, 0, 0))
        for ((translation, text) in actual) {
            when (translation) {
                MockContents.kjvShortName -> {
                    assertEquals(MockContents.kjvVerses[0].text, text)
                }
                MockContents.cuvShortName -> {
                    assertEquals(MockContents.cuvVerses[0].text, text)
                }
                else -> fail()
            }
        }

        assertEquals(MockContents.kjvVerses[0], androidDatabase.translationDao.read(
                MockContents.kjvShortName, VerseIndex(0, 0, 0)))
    }

    @Test
    fun testSaveThenReadByVerseIndexWithMissingVerse() {
        saveKjv()
        saveCuv()

        assertTrue(androidDatabase.translationDao.read(
                listOf(MockContents.kjvShortName, MockContents.cuvShortName),
                VerseIndex(1, 1, 1)
        ).isEmpty())
    }

    @Test
    fun testSaveThenReadByVerseIndexWithMissingParallel() {
        saveKjv()
        saveCuv()

        val actual = androidDatabase.translationDao.read(listOf(MockContents.kjvShortName, MockContents.bbeShortName, MockContents.cuvShortName),
                VerseIndex(0, 0, 0))
        for ((translation, text) in actual) {
            when (translation) {
                MockContents.kjvShortName -> {
                    assertEquals(MockContents.kjvVerses[0].text, text)
                }
                MockContents.cuvShortName -> {
                    assertEquals(MockContents.cuvVerses[0].text, text)
                }
                else -> fail()
            }
        }
    }

    @Test
    fun testSearchNonExistTranslation() {
        assertTrue(androidDatabase.translationDao.search("not_exist", "keyword").isEmpty())
    }

    @Test
    fun testSaveThenSearch() {
        saveKjv()

        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.search(MockContents.kjvShortName, "God"))
        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.search(MockContents.kjvShortName, "god"))
        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.search(MockContents.kjvShortName, "GOD"))
    }

    @Test
    fun testSaveThenSearchMultiKeywords() {
        saveKjv()

        assertEquals(listOf(MockContents.kjvVerses[0]),
                androidDatabase.translationDao.search(MockContents.kjvShortName, "God created"))
        assertEquals(listOf(MockContents.kjvVerses[0]),
                androidDatabase.translationDao.search(MockContents.kjvShortName, "beginning created"))
    }

    @Test
    fun testRemoveNonExistTranslation() {
        assertFalse(androidDatabase.readableDatabase.hasTable("non_exist"))
        androidDatabase.translationDao.removeTable("non_exist")
        assertFalse(androidDatabase.readableDatabase.hasTable("non_exist"))
    }

    @Test
    fun testRemoveTranslation() {
        saveKjv()
        assertTrue(androidDatabase.readableDatabase.hasTable(MockContents.kjvShortName))

        androidDatabase.translationDao.removeTable(MockContents.kjvShortName)
        assertFalse(androidDatabase.readableDatabase.hasTable(MockContents.kjvShortName))
    }
}
