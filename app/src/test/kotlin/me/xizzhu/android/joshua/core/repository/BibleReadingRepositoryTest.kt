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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.MockLocalReadingStorage
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class BibleReadingRepositoryTest {
    private lateinit var bibleReadingRepository: BibleReadingRepository

    @Before
    fun setup() {
        bibleReadingRepository = BibleReadingRepository(MockLocalReadingStorage())
    }

    @Test
    fun testDefaultCurrentTranslation() {
        val expected = ""
        val actual = runBlocking { bibleReadingRepository.readCurrentTranslation() }
        assertEquals(expected, actual)
    }

    @Test
    fun testCurrentTranslation() {
        val expected = "KJV"
        val actual = runBlocking {
            bibleReadingRepository.saveCurrentTranslation("KJV")
            bibleReadingRepository.readCurrentTranslation()
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testDefaultCurrentVerseIndex() {
        val expected = VerseIndex.INVALID
        val actual = runBlocking { bibleReadingRepository.readCurrentVerseIndex() }
        assertEquals(expected, actual)
    }

    @Test
    fun testCurrentVerseIndex() {
        val expected = VerseIndex(1, 2, 3)
        val actual = runBlocking {
            bibleReadingRepository.saveCurrentVerseIndex(VerseIndex(1, 2, 3))
            bibleReadingRepository.readCurrentVerseIndex()
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testBookNames() {
        val expected = ArrayList<String>(MockContents.bookNames)
        val actual = runBlocking { bibleReadingRepository.readBookNames(MockContents.translationShortName) }
        assertEquals(expected, actual)
    }

    @Test
    fun testVerses() {
        val expected = ArrayList<Verse>(MockContents.verses)
        val actual = runBlocking { bibleReadingRepository.readVerses(MockContents.translationShortName, 0, 0) }
        assertEquals(expected, actual)
    }

    @Test
    fun testSearch() {
        val expected = ArrayList<Verse>(MockContents.verses)
        val actual = runBlocking { bibleReadingRepository.search(MockContents.translationShortName, "God") }
        assertEquals(expected, actual)
    }
}
