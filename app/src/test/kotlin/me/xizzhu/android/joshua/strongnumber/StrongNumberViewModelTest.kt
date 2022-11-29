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

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class StrongNumberViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var strongNumberManager: StrongNumberManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var strongNumberViewModel: StrongNumberViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        strongNumberManager = mockk()
        settingsManager = mockk<SettingsManager>().apply { every { settings() } returns flowOf(Settings.DEFAULT) }
        savedStateHandle = mockk<SavedStateHandle>().apply { every { get<String>("me.xizzhu.android.joshua.KEY_STRONG_NUMBER") } returns "" }

        strongNumberViewModel = StrongNumberViewModel(bibleReadingManager, strongNumberManager, settingsManager, testCoroutineDispatcherProvider, savedStateHandle)
    }

    @Test
    fun `test loadStrongNumber(), called in constructor, with empty sn`() = runTest {
        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError
            ),
            strongNumberViewModel.viewState().first()
        )

        strongNumberViewModel.markErrorAsShown(StrongNumberViewModel.ViewState.Error.VerseOpeningError(VerseIndex.INVALID))
        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError
            ),
            strongNumberViewModel.viewState().first()
        )

        strongNumberViewModel.markErrorAsShown(StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError)
        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = null
            ),
            strongNumberViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadStrongNumber(), called in constructor, with exception`() = runTest {
        val sn = "H7225"
        coEvery { bibleReadingManager.currentTranslation() } throws RuntimeException("random exception")
        every { savedStateHandle.get<String>("me.xizzhu.android.joshua.KEY_STRONG_NUMBER") } returns sn

        strongNumberViewModel = StrongNumberViewModel(bibleReadingManager, strongNumberManager, settingsManager, testCoroutineDispatcherProvider, savedStateHandle)

        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError
            ),
            strongNumberViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadStrongNumber(), called in constructor`() = runTest {
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

        every { savedStateHandle.get<String>("me.xizzhu.android.joshua.KEY_STRONG_NUMBER") } returns sn

        strongNumberViewModel = StrongNumberViewModel(bibleReadingManager, strongNumberManager, settingsManager, testCoroutineDispatcherProvider, savedStateHandle)

        val actual = strongNumberViewModel.viewState().first()
        assertFalse(actual.loading)
        assertEquals(Settings.DEFAULT, (actual.items[0] as StrongNumberItem.StrongNumber).settings)
        assertEquals(
            "H7225 beginning, chief(-est), first(-fruits, part, time), principal thing.",
            (actual.items[0] as StrongNumberItem.StrongNumber).text.toString()
        )
        assertEquals(
            listOf(
                StrongNumberItem.BookName(Settings.DEFAULT, "Genesis"),
                StrongNumberItem.Verse(
                    settings = Settings.DEFAULT,
                    verseIndex = VerseIndex(0, 0, 0),
                    bookShortName = "Gen.",
                    verseText = "In the beginning God created the heaven and the earth."
                ),
                StrongNumberItem.Verse(
                    settings = Settings.DEFAULT,
                    verseIndex = VerseIndex(0, 9, 9),
                    bookShortName = "Gen.",
                    verseText = "And the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar."
                ),
                StrongNumberItem.BookName(Settings.DEFAULT, "Exodus"),
                StrongNumberItem.Verse(
                    settings = Settings.DEFAULT,
                    verseIndex = VerseIndex(1, 22, 18),
                    bookShortName = "Ex.",
                    verseText = "The first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk."
                )
            ),
            actual.items.subList(1, actual.items.size)
        )
        assertNull(actual.preview)
        assertNull(actual.error)
    }

    @Test
    fun `test openVerse() with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } throws RuntimeException("random exception")

        strongNumberViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = StrongNumberViewModel.ViewState.Error.VerseOpeningError(VerseIndex(0, 0, 0))
            ),
            strongNumberViewModel.viewState().first()
        )
    }

    @Test
    fun `test openVerse()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } returns Unit

        val viewAction = async(Dispatchers.Unconfined) { strongNumberViewModel.viewAction().first() }

        strongNumberViewModel.markErrorAsShown(StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError)
        strongNumberViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = null
            ),
            strongNumberViewModel.viewState().first()
        )
        assertEquals(StrongNumberViewModel.ViewAction.OpenReadingScreen, viewAction.await())
    }

    @Test
    fun `test loadPreview() with invalid verse index`() = runTest {
        strongNumberViewModel.loadPreview(VerseIndex.INVALID)

        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = StrongNumberViewModel.ViewState.Error.PreviewLoadingError(VerseIndex.INVALID)
            ),
            strongNumberViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadPreview()`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0)
        } returns listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames

        strongNumberViewModel.markErrorAsShown(StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError)
        strongNumberViewModel.loadPreview(VerseIndex(0, 0, 1))

        val actual = strongNumberViewModel.viewState().first()
        assertFalse(actual.loading)
        assertTrue(actual.items.isEmpty())
        assertEquals(Settings.DEFAULT, actual.preview?.settings)
        assertEquals("Gen., 1", actual.preview?.title)
        assertEquals(3, actual.preview?.items?.size)
        assertEquals(1, actual.preview?.currentPosition)
        assertNull(actual.error)

        strongNumberViewModel.markPreviewAsClosed()
        assertEquals(
            StrongNumberViewModel.ViewState(
                loading = false,
                items = emptyList(),
                preview = null,
                error = null
            ),
            strongNumberViewModel.viewState().first()
        )
    }
}
