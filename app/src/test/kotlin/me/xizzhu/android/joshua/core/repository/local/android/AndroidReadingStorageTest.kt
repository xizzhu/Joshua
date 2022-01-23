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
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AndroidReadingStorageTest : BaseSqliteTest() {
    private lateinit var androidReadingStorage: AndroidReadingStorage

    @BeforeTest
    override fun setup() {
        super.setup()
        androidReadingStorage = AndroidReadingStorage(androidDatabase)
    }

    @Test
    fun testReadDefaultCurrentVerseIndex() = runTest {
        assertEquals(VerseIndex(0, 0, 0), androidReadingStorage.readCurrentVerseIndex())
    }

    @Test
    fun testSaveThenReadCurrentVerseIndex() = runTest {
        val expected = VerseIndex(1, 2, 3)
        androidReadingStorage.saveCurrentVerseIndex(expected)
        assertEquals(expected, androidReadingStorage.readCurrentVerseIndex())
    }

    @Test
    fun testSaveOverrideThenReadCurrentVerseIndex() = runTest {
        val expected = VerseIndex(1, 2, 3)
        androidReadingStorage.saveCurrentVerseIndex(VerseIndex(9, 8, 7))
        androidReadingStorage.saveCurrentVerseIndex(expected)
        assertEquals(expected, androidReadingStorage.readCurrentVerseIndex())
    }

    @Test
    fun testReadDefaultCurrentTranslation() = runTest {
        assertEquals("", androidReadingStorage.readCurrentTranslation())
    }

    @Test
    fun testSaveThenReadCurrentTranslation() = runTest {
        val expected = MockContents.kjvShortName
        androidReadingStorage.saveCurrentTranslation(expected)
        assertEquals(expected, androidReadingStorage.readCurrentTranslation())
    }

    @Test
    fun testSaveOverrideThenReadCurrentTranslation() = runTest {
        val expected = MockContents.kjvShortName
        androidReadingStorage.saveCurrentTranslation("random")
        androidReadingStorage.saveCurrentTranslation(expected)
        assertEquals(expected, androidReadingStorage.readCurrentTranslation())
    }

    @Test
    fun testReadDefaultParallelTranslations() = runTest {
        assertTrue(androidReadingStorage.readParallelTranslations().isEmpty())
    }

    @Test
    fun testSaveThenReadParallelTranslations() = runTest {
        val expected = listOf(MockContents.kjvShortName, MockContents.cuvShortName)
        androidReadingStorage.saveParallelTranslations(expected)
        assertEquals(expected, androidReadingStorage.readParallelTranslations())
    }

    @Test
    fun testSaveOverrideThenReadParallelTranslations() = runTest {
        val expected = listOf(MockContents.kjvShortName, MockContents.cuvShortName)
        androidReadingStorage.saveParallelTranslations(listOf("random"))
        androidReadingStorage.saveParallelTranslations(expected)
        assertEquals(expected, androidReadingStorage.readParallelTranslations())
    }
}
