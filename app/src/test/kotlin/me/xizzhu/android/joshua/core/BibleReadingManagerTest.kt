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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.MockLocalReadingStorage
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class BibleReadingManagerTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager

    @Before
    override fun setUp() {
        super.setUp()
        bibleReadingManager = BibleReadingManager(BibleReadingRepository(MockLocalReadingStorage()))
    }

    @Test
    fun testDefaultCurrentVerseIndex() {
        val expected = VerseIndex.INVALID
        val actual = runBlocking { bibleReadingManager.observeCurrentVerseIndex().receive() }
        assertEquals(expected, actual)
    }

    @Test
    fun testCurrentVerseIndex() {
        val expected = VerseIndex(1, 2, 3)
        val actual = runBlocking {
            bibleReadingManager.saveCurrentVerseIndex(VerseIndex(1, 2, 3))
            bibleReadingManager.observeCurrentVerseIndex().receive()
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testDefaultCurrentTranslation() {
        val expected = ""
        val actual = runBlocking { bibleReadingManager.observeCurrentTranslation().receive() }
        assertEquals(expected, actual)
    }

    @Test
    fun testCurrentTranslation() {
        val expected = "KJV"
        val actual = runBlocking {
            bibleReadingManager.saveCurrentTranslation("KJV")
            bibleReadingManager.observeCurrentTranslation().receive()
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testBookNames() {
        val expected = ArrayList<String>(MockContents.kjvBookNames)
        val actual = runBlocking { bibleReadingManager.readBookNames(MockContents.kjvShortName) }
        assertEquals(expected, actual)
    }

    @Test
    fun testVerses() {
        val expected = ArrayList<Verse>(MockContents.kjvVerses)
        val actual = runBlocking { bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0) }
        assertEquals(expected, actual)
    }

    @Test
    fun testSearch() {
        val expected = ArrayList<Verse>(MockContents.kjvVerses)
        val actual = runBlocking { bibleReadingManager.search(MockContents.kjvShortName, "God") }
        assertEquals(expected, actual)
    }
}
