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

package me.xizzhu.android.joshua.strongnumber

import android.app.Application
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
class StrongNumberViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var strongNumberManager: StrongNumberManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application

    private lateinit var strongNumberViewModel: StrongNumberViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        strongNumberManager = mockk()
        settingsManager = mockk()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        application = mockk()

        strongNumberViewModel = StrongNumberViewModel(bibleReadingManager, strongNumberManager, settingsManager, application)
    }

    @Test
    fun `test loadStrongNumber() with empty sn`() = runTest {
        val viewActionAsync = async(Dispatchers.Default) { strongNumberViewModel.viewAction().first() }
        delay(100)

        strongNumberViewModel.loadStrongNumber("")

        assertTrue(viewActionAsync.await() is StrongNumberViewModel.ViewAction.ShowLoadStrongNumberFailedError)
    }

    @Test
    fun `test loadStrongNumber() with exception`() = runTest {
        every { bibleReadingManager.currentTranslation() } throws RuntimeException("random exception")

        val viewActionAsync = async(Dispatchers.Default) { strongNumberViewModel.viewAction().first() }
        delay(100)

        strongNumberViewModel.loadStrongNumber("H7225")

        assertTrue(viewActionAsync.await() is StrongNumberViewModel.ViewAction.ShowLoadStrongNumberFailedError)
        with(strongNumberViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertTrue(strongNumberItems.isEmpty())
        }
    }

    @Test
    fun `test loadStrongNumber()`() = runTest {
        val sn = "H7225"
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { strongNumberManager.readStrongNumber(sn) } returns StrongNumber(sn, MockContents.strongNumberWords.getValue(sn))
        coEvery { strongNumberManager.readVerseIndexes(sn) } returns MockContents.strongNumberReverseIndex.getValue(sn)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, MockContents.strongNumberReverseIndex.getValue(sn))
        } returns mapOf(
                Pair(MockContents.kjvExtraVerses[0].verseIndex, MockContents.kjvExtraVerses[0]),
                Pair(MockContents.kjvExtraVerses[1].verseIndex, MockContents.kjvExtraVerses[1]),
                Pair(MockContents.kjvVerses[0].verseIndex, MockContents.kjvVerses[0])
        )
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames

        strongNumberViewModel.loadStrongNumber(sn)

        val viewState = strongNumberViewModel.viewState().first()
        assertFalse(viewState.loading)
        assertEquals(Settings.DEFAULT, viewState.settings)
        assertEquals(6, viewState.strongNumberItems.size)
        assertEquals(
                "H7225 beginning, chief(-est), first(-fruits, part, time), principal thing.",
                (viewState.strongNumberItems[0] as TextItem).title.toString()
        )
        assertEquals("Genesis", (viewState.strongNumberItems[1] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1 In the beginning God created the heaven and the earth.",
                (viewState.strongNumberItems[2] as StrongNumberItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 10:10 And the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar.",
                (viewState.strongNumberItems[3] as StrongNumberItem).textForDisplay.toString()
        )
        assertEquals("Exodus", (viewState.strongNumberItems[4] as TitleItem).title.toString())
        assertEquals(
                "Ex. 23:19 The first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk.",
                (viewState.strongNumberItems[5] as StrongNumberItem).textForDisplay.toString()
        )
    }

    @Test
    fun `test openVerse() with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } throws RuntimeException("random exception")

        val viewActionAsync = async(Dispatchers.Default) { strongNumberViewModel.viewAction().first() }

        strongNumberViewModel.openVerse(VerseIndex(0, 0, 0))

        with(viewActionAsync.await()) {
            assertTrue(this is StrongNumberViewModel.ViewAction.ShowOpenVerseFailedError)
            assertEquals(VerseIndex(0, 0, 0), verseToOpen)
        }
    }

    @Test
    fun `test openVerse()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } returns Unit

        val viewActionAsync = async(Dispatchers.Default) { strongNumberViewModel.viewAction().first() }

        strongNumberViewModel.openVerse(VerseIndex(0, 0, 0))

        assertTrue(viewActionAsync.await() is StrongNumberViewModel.ViewAction.OpenReadingScreen)
    }

    @Test
    fun `test showPreview() with invalid verse index`() = runTest {
        val viewActionAsync = async(Dispatchers.Default) { strongNumberViewModel.viewAction().first() }

        strongNumberViewModel.showPreview(VerseIndex.INVALID)

        assertTrue(viewActionAsync.await() is StrongNumberViewModel.ViewAction.ShowOpenPreviewFailedError)
    }

    @Test
    fun `test showPreview()`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0)
        } returns listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        val viewActionAsync = async(Dispatchers.Default) { strongNumberViewModel.viewAction().first() }
        delay(100)

        strongNumberViewModel.showPreview(VerseIndex(0, 0, 1))

        val actual = viewActionAsync.await()
        assertTrue(actual is StrongNumberViewModel.ViewAction.ShowPreview)
        assertEquals(Settings.DEFAULT, actual.previewViewData.settings)
        assertEquals("Gen., 1", actual.previewViewData.title)
        assertEquals(3, actual.previewViewData.items.size)
        assertEquals(1, actual.previewViewData.currentPosition)
    }
}
