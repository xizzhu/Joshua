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

import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.VerseQuery
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.toMap
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class TranslationDaoTest : BaseSqliteTest() {
    @Test
    fun testReadNonExistTranslation() {
        assertTrue(androidDatabase.translationDao.read("not_exist", 0, 0).isEmpty())
        assertTrue(androidDatabase.translationDao.read("not_exist", listOf(), 0, 0).isEmpty())
        assertTrue(androidDatabase.translationDao.read("not_exist", listOf()).isEmpty())
    }

    @Test
    fun testSaveSameTranslation() {
        saveKjv()
        saveKjv()
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

    private fun saveMsg() {
        saveTranslation(MockContents.msgShortName, MockContents.msgVerses)
    }

    private fun saveTranslation(translationShortName: String, verses: List<Verse>) {
        androidDatabase.translationDao.save(translationShortName, verses.toMap())
    }

    @Test
    fun testSaveOverrideThenReadVerses() {
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

        val actual = androidDatabase.translationDao.read(
                MockContents.kjvShortName, listOf(MockContents.cuvShortName), 0, 0)
        for ((i, verse) in actual.withIndex()) {
            assertEquals(MockContents.kjvVerses[i].text, verse.text)
            assertEquals(listOf(MockContents.cuvVerses[i].text), verse.parallel)
        }
    }

    @Test
    fun testSaveThenReadWithParallelTranslationsAndMissingParallelVerses() {
        saveKjv()
        saveCuv()
        saveMsg()

        val actual = androidDatabase.translationDao.read(MockContents.kjvShortName,
                listOf(MockContents.cuvShortName, MockContents.bbeShortName, MockContents.msgShortName), 0, 0)
        assertEquals(MockContents.kjvVerses.size, actual.size)
        for ((i, verse) in actual.withIndex()) {
            assertEquals(MockContents.kjvVerses[i].text, verse.text)
            assertEquals(
                    listOf(
                            MockContents.cuvVerses[i].text,
                            Verse.Text(MockContents.bbeShortName, ""),
                            if (MockContents.msgVerses.size > i) MockContents.msgVerses[i].text else Verse.Text(MockContents.msgShortName, "")
                    ), verse.parallel)
        }
    }

    @Test
    fun testSaveThenReadWithParallelTranslationsAndMissingPrimaryVerses() {
        saveKjv()
        saveCuv()
        saveMsg()

        val actual = androidDatabase.translationDao.read(MockContents.msgShortName,
                listOf(MockContents.cuvShortName, MockContents.bbeShortName, MockContents.kjvShortName), 0, 0)
        assertEquals(MockContents.kjvVerses.size, actual.size)
        for ((i, verse) in actual.withIndex()) {
            assertEquals(if (MockContents.msgVerses.size > i) MockContents.msgVerses[i].text else Verse.Text(MockContents.msgShortName, ""), verse.text)
            assertEquals(listOf(MockContents.cuvVerses[i].text, Verse.Text(MockContents.bbeShortName, ""), MockContents.kjvVerses[i].text), verse.parallel)
        }
    }

    @Test
    fun testSaveThenReadByVerseIndexes() {
        saveKjv()
        saveCuv()

        assertTrue(
                androidDatabase.translationDao.read(MockContents.kjvShortName,
                        listOf(
                                VerseIndex(1, 1, 1),
                                VerseIndex(-1, -1, -1)
                        )
                ).isEmpty()
        )
        assertEquals(
                mapOf(Pair(VerseIndex(0, 0, 0), MockContents.kjvVerses[0])),
                androidDatabase.translationDao.read(
                        MockContents.kjvShortName,
                        listOf(
                                VerseIndex(1, 1, 1),
                                VerseIndex(0, 0, 0),
                                VerseIndex(-1, -1, -1)
                        )
                )
        )
        assertEquals(
                mapOf(
                        Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2]),
                        Pair(VerseIndex(0, 0, 0), MockContents.kjvVerses[0])
                ),
                androidDatabase.translationDao.read(
                        MockContents.kjvShortName,
                        listOf(
                                VerseIndex(1, 1, 1),
                                VerseIndex(0, 0, 0),
                                VerseIndex(-1, -1, -1),
                                VerseIndex(0, 0, 2)
                        )
                )
        )
    }

    @Test
    fun testSaveThenReadByLargeVerseIndexes() {
        saveKjv()
        saveCuv()

        val verseIndexes = arrayListOf(
                VerseIndex(0, 0, 0),
                VerseIndex(0, 0, 2)
        ).apply {
            (1..10000).forEach {
                add(VerseIndex(it, 0, 0))
            }
        }
        assertEquals(
                mapOf(
                        Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2]),
                        Pair(VerseIndex(0, 0, 0), MockContents.kjvVerses[0])
                ),
                androidDatabase.translationDao.read(MockContents.kjvShortName, verseIndexes)
        )
    }

    @Test
    fun testSearchNonExistTranslation() {
        assertTrue(androidDatabase.translationDao.search(VerseQuery("not_exist", "keyword", true, true)).isEmpty())
    }

    @Test
    fun testSaveThenSearch() {
        saveKjv()

        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "God", true, true)))
        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "God", true, false)))
        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "god", true, true)))
        assertEquals(MockContents.kjvVerses,
                androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "GOD", true, true)))
        assertTrue(androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "God", false, true)).isEmpty())
        assertTrue(androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "God", false, false)).isEmpty())
        assertTrue(androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "not_exist", true, true)).isEmpty())
        assertTrue(androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "", true, true)).isEmpty())
        assertTrue(androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "    ", true, true)).isEmpty())
    }

    @Test
    fun testSaveThenSearchMultiKeywords() {
        saveKjv()

        assertEquals(listOf(MockContents.kjvVerses[0]),
                androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "God created", true, true)))
        assertEquals(listOf(MockContents.kjvVerses[0]),
                androidDatabase.translationDao.search(VerseQuery(MockContents.kjvShortName, "beginning created", true, true)))
    }

    @Test
    fun testRemoveNonExistTranslation() {
        assertFalse(androidDatabase.readableDatabase.hasTable("non_exist"))
        androidDatabase.translationDao.remove("non_exist")
        assertFalse(androidDatabase.readableDatabase.hasTable("non_exist"))
    }

    @Test
    fun testRemoveTranslation() {
        saveKjv()
        assertTrue(androidDatabase.readableDatabase.hasTable(MockContents.kjvShortName))

        androidDatabase.translationDao.remove(MockContents.kjvShortName)
        assertFalse(androidDatabase.readableDatabase.hasTable(MockContents.kjvShortName))
    }
}
