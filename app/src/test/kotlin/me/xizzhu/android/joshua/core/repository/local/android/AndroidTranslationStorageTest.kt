/*
 * Copyright (C) 2022 Xizhi Zhu
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

import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.toMap
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AndroidTranslationStorageTest : BaseSqliteTest() {
    private lateinit var androidTranslationStorage: AndroidTranslationStorage

    @BeforeTest
    override fun setup() {
        super.setup()
        androidTranslationStorage = AndroidTranslationStorage(androidDatabase)
    }

    @Test
    fun testReadEmpty() = runTest {
        assertTrue(androidTranslationStorage.readTranslations().isEmpty())
        assertEquals(0L, androidTranslationStorage.readTranslationListRefreshTimestamp())
    }

    @Test
    fun testTranslationListRefreshTimestamp() = runTest {
        androidTranslationStorage.saveTranslationListRefreshTimestamp(12345678L)
        assertEquals(12345678L, androidTranslationStorage.readTranslationListRefreshTimestamp())
    }

    @Test
    fun testSaveThenRead() = runTest {
        androidTranslationStorage.replaceTranslations(listOf(MockContents.kjvTranslationInfo))

        val actual = androidTranslationStorage.readTranslations()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvTranslationInfo, actual[0])
    }

    @Test
    fun testSaveOverrideThenRead() = runTest {
        androidTranslationStorage.replaceTranslations(listOf(MockContents.kjvDownloadedTranslationInfo))
        androidTranslationStorage.replaceTranslations(listOf(MockContents.kjvTranslationInfo))

        val actual = androidTranslationStorage.readTranslations()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvTranslationInfo, actual[0])
    }

    @Test
    fun testSaveTranslationThenRead() = runTest {
        androidTranslationStorage.saveTranslation(MockContents.kjvDownloadedTranslationInfo,
                MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap())

        val actual = androidTranslationStorage.readTranslations()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvDownloadedTranslationInfo, actual[0])
    }

    @Test
    fun testSaveTranslationMultipleTimesThenRead() = runTest {
        androidTranslationStorage.saveTranslation(MockContents.kjvDownloadedTranslationInfo,
                MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap())
        androidTranslationStorage.saveTranslation(MockContents.kjvDownloadedTranslationInfo,
                MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap())

        val actual = androidTranslationStorage.readTranslations()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvDownloadedTranslationInfo, actual[0])
    }

    @Test
    fun testSaveTranslationWithDownloadedFalseThenRead() = runTest {
        androidTranslationStorage.saveTranslation(MockContents.kjvTranslationInfo,
                MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap())

        val actual = androidTranslationStorage.readTranslations()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvDownloadedTranslationInfo, actual[0])
    }

    @Test
    fun testRemoveNonExistTranslation() = runTest {
        assertFalse(androidDatabase.readableDatabase.hasTable("non_exist"))
        androidTranslationStorage.removeTranslation(TranslationInfo("non_exist", "name", "language", 12345L, true))
        assertFalse(androidDatabase.readableDatabase.hasTable("non_exist"))
    }

    @Test
    fun testRemoveTranslation() = runTest {
        androidTranslationStorage.saveTranslation(MockContents.kjvTranslationInfo,
                MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap())
        assertTrue(androidDatabase.readableDatabase.hasTable(MockContents.kjvShortName))
        assertEquals(MockContents.kjvBookNames, androidDatabase.bookNamesDao.read(MockContents.kjvShortName))
        assertEquals(MockContents.kjvVerses, androidDatabase.translationDao.read(
                MockContents.kjvShortName, MockContents.kjvVerses[0].verseIndex.bookIndex,
                MockContents.kjvVerses[0].verseIndex.chapterIndex))

        androidTranslationStorage.removeTranslation(MockContents.kjvTranslationInfo)
        assertFalse(androidDatabase.readableDatabase.hasTable(MockContents.kjvShortName))
        assertTrue(androidDatabase.bookNamesDao.read(MockContents.kjvShortName).isEmpty())
        assertTrue(androidDatabase.translationDao.read(MockContents.kjvShortName,
                MockContents.kjvVerses[0].verseIndex.bookIndex,
                MockContents.kjvVerses[0].verseIndex.chapterIndex).isEmpty())
    }
}
