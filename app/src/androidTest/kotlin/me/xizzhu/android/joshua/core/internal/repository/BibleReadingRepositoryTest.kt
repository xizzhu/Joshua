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

package me.xizzhu.android.joshua.core.internal.repository

import android.database.sqlite.SQLiteException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class BibleReadingRepositoryTest {
    private lateinit var bibleReadingRepository: BibleReadingRepository

    @Before
    fun setup() {
        bibleReadingRepository = BibleReadingRepository(createLocalStorage())
    }

    @After
    fun tearDown() {
        clearLocalStorage()
    }

    @Test
    fun testDefaultCurrentVerseIndex() {
        val expected = VerseIndex(0, 0, 0)
        val actual = runBlocking { bibleReadingRepository.readCurrentVerseIndex() }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testCurrentVerseIndex() {
        val expected = VerseIndex(42, 2, 15)
        val actual = runBlocking {
            bibleReadingRepository.saveCurrentVerseIndex(VerseIndex(42, 2, 15))
            bibleReadingRepository.readCurrentVerseIndex()
        }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testDefaultCurrentTranslation() {
        val expected = ""
        val actual = runBlocking { bibleReadingRepository.readCurrentTranslation() }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testCurrentTranslation() {
        val expected = "中文和合本"
        val actual = runBlocking {
            bibleReadingRepository.saveCurrentTranslation("中文和合本")
            bibleReadingRepository.readCurrentTranslation()
        }
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testNonExistBookNames() {
        Assert.assertTrue(runBlocking { bibleReadingRepository.readBookNames("non_exist") }.isEmpty())
    }

    @Test
    fun testBookNames() {
        val expected = kjvBookNames.clone()
        val actual = runBlocking {
            prepareBookNames(kjvTranslationShortName, kjvBookNames)
            bibleReadingRepository.readBookNames(kjvTranslationShortName)
        }
        Assert.assertEquals(expected, actual)
    }

    @Test(expected = SQLiteException::class)
    fun testReadVersesFromNonExistTranslation() {
        runBlocking { bibleReadingRepository.readVerses("non_exist", 0, 0) }
    }

    @Test
    fun testVerses() {
        val expected = cuvVerses.clone()
        val actual = runBlocking {
            prepareVerses(cuvTranslationShortName, cuvVerses)
            bibleReadingRepository.readVerses(cuvTranslationShortName, cuvVerses[0].verseIndex.bookIndex, cuvVerses[0].verseIndex.chapterIndex)
        }
        Assert.assertEquals(expected, actual)
    }
}
