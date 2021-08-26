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

package me.xizzhu.android.joshua.strongnumber

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrongNumberInteractorTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var strongNumberManager: StrongNumberManager
    private lateinit var settingsManager: SettingsManager

    private lateinit var strongNumberInteractor: StrongNumberInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        strongNumberManager = mockk()
        settingsManager = mockk()

        strongNumberInteractor = StrongNumberInteractor(bibleReadingManager, strongNumberManager, settingsManager)
    }

    @Test
    fun `test verses with empty verses`(): Unit = runBlocking {
        coEvery { strongNumberManager.readVerseIndexes("H7225") } returns emptyList()
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, emptyList()) } returns emptyMap()

        assertTrue(strongNumberInteractor.verses("H7225").isEmpty())
    }

    @Test
    fun `test verses with single verse`(): Unit = runBlocking {
        coEvery { strongNumberManager.readVerseIndexes("H7225") } returns listOf(VerseIndex(0, 0, 0))
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, listOf(VerseIndex(0, 0, 0)))
        } returns mapOf(Pair(VerseIndex(0, 0, 0), MockContents.kjvVerses[0]))

        assertEquals(listOf(MockContents.kjvVerses[0]), strongNumberInteractor.verses("H7225"))
    }

    @Test
    fun `test verses with multiple verses`(): Unit = runBlocking {
        coEvery {
            strongNumberManager.readVerseIndexes("H7225")
        } returns listOf(VerseIndex(0, 0, 0), VerseIndex(0, 0, 1))
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, listOf(VerseIndex(0, 0, 0), VerseIndex(0, 0, 1)))
        } returns mapOf(
                Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1]),
                Pair(VerseIndex(0, 0, 0), MockContents.kjvVerses[0])
        )

        assertEquals(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1]), strongNumberInteractor.verses("H7225"))
    }
}
