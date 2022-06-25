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

package me.xizzhu.android.joshua.translations

import android.app.Application
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import kotlin.test.*

class TranslationsViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var translationManager: TranslationManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application

    private lateinit var translationsViewModel: TranslationsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        translationManager = mockk()

        settingsManager = mockk()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        application = mockk()
        every { application.getString(R.string.header_available_translations) } returns "AVAILABLE TRANSLATIONS"

        translationsViewModel = spyk(TranslationsViewModel(bibleReadingManager, translationManager, settingsManager, application))
    }

    @Test
    fun `test selectTranslation() with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentTranslation(MockContents.kjvShortName) } throws RuntimeException("random exception")

        val viewActionAsync = async(Dispatchers.Default) { translationsViewModel.viewAction().first() }
        delay(100)

        translationsViewModel.selectTranslation(MockContents.kjvDownloadedTranslationInfo)

        with(viewActionAsync.await()) {
            assertTrue(this is TranslationsViewModel.ViewAction.ShowSelectTranslationFailedError)
            assertEquals(MockContents.kjvDownloadedTranslationInfo, this.translationToSelect)
        }
    }

    @Test
    fun `test selectTranslation()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentTranslation(MockContents.kjvShortName) } returns Unit

        val viewActionAsync = async(Dispatchers.Default) { translationsViewModel.viewAction().first() }
        delay(100)

        translationsViewModel.selectTranslation(MockContents.kjvDownloadedTranslationInfo)

        assertTrue(viewActionAsync.await() is TranslationsViewModel.ViewAction.GoBack)
    }

    @Test
    fun `test refreshTranslations()`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.cuvShortName)
        coEvery { translationManager.reload(false) } returns Unit
        every { translationManager.availableTranslations() } returns flowOf(listOf(MockContents.kjvTranslationInfo))
        every { translationManager.downloadedTranslations() } returns flowOf(listOf(MockContents.cuvDownloadedTranslationInfo))

        translationsViewModel.refreshTranslations(false)

        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)

            assertEquals(5, translationItems.size)
            assertEquals("Chinese", (translationItems[0] as TitleItem).title.toString())
            assertEquals(MockContents.cuvDownloadedTranslationInfo, (translationItems[1] as TranslationItem).translationInfo)
            assertEquals("AVAILABLE TRANSLATIONS", (translationItems[2] as TitleItem).title.toString())
            assertEquals("English", (translationItems[3] as TitleItem).title.toString())
            assertEquals(MockContents.kjvTranslationInfo, (translationItems[4] as TranslationItem).translationInfo)

            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }

        coVerify(exactly = 1) { translationManager.reload(false) }
    }

    @Test
    fun `test refreshTranslations() without available and downloaded`() = runTest {
        coEvery { translationManager.reload(true) } returns Unit
        every { translationManager.availableTranslations() } returns flowOf(emptyList())
        every { translationManager.downloadedTranslations() } returns flowOf(emptyList())

        val viewActionAsync = async(Dispatchers.Default) { translationsViewModel.viewAction().first() }
        delay(100)

        translationsViewModel.refreshTranslations(true)

        assertTrue(viewActionAsync.await() is TranslationsViewModel.ViewAction.ShowNoTranslationAvailableError)
        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertTrue(translationItems.isEmpty())
            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }

        coVerify(ordering = Ordering.SEQUENCE) {
            translationManager.reload(true)
            translationManager.availableTranslations()
            translationManager.downloadedTranslations()
        }
    }

    @Test
    fun `test refreshTranslations() without available`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.cuvShortName)
        coEvery { translationManager.reload(true) } returns Unit
        every { translationManager.availableTranslations() } returns flowOf(emptyList())
        every { translationManager.downloadedTranslations() } returns flowOf(listOf(MockContents.cuvDownloadedTranslationInfo, MockContents.kjvDownloadedTranslationInfo))

        translationsViewModel.refreshTranslations(true)

        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)

            assertEquals(4, translationItems.size)
            assertEquals("English", (translationItems[0] as TitleItem).title.toString())
            assertEquals(MockContents.kjvDownloadedTranslationInfo, (translationItems[1] as TranslationItem).translationInfo)
            assertEquals("Chinese", (translationItems[2] as TitleItem).title.toString())
            assertEquals(MockContents.cuvDownloadedTranslationInfo, (translationItems[3] as TranslationItem).translationInfo)

            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }

        coVerify(ordering = Ordering.SEQUENCE) {
            translationManager.reload(true)
            translationManager.availableTranslations()
            translationManager.downloadedTranslations()
            bibleReadingManager.currentTranslation()
        }
    }

    @Test
    fun `test refreshTranslations() without downloaded`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.cuvShortName)
        coEvery { translationManager.reload(true) } returns Unit
        every { translationManager.availableTranslations() } returns flowOf(listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo))
        every { translationManager.downloadedTranslations() } returns flowOf(emptyList())

        translationsViewModel.refreshTranslations(true)

        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)

            assertEquals(4, translationItems.size)
            assertEquals("AVAILABLE TRANSLATIONS", (translationItems[0] as TitleItem).title.toString())
            assertEquals("English", (translationItems[1] as TitleItem).title.toString())
            assertEquals(MockContents.kjvTranslationInfo, (translationItems[2] as TranslationItem).translationInfo)
            assertEquals(MockContents.bbeTranslationInfo, (translationItems[3] as TranslationItem).translationInfo)

            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }

        coVerify(ordering = Ordering.SEQUENCE) {
            translationManager.reload(true)
            translationManager.availableTranslations()
            translationManager.downloadedTranslations()
            bibleReadingManager.currentTranslation()
        }
    }

    @Test
    fun `test downloadTranslation() with exception`() = runTest {
        every { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow { throw RuntimeException("random exception") }

        val viewActionAsync = async(Dispatchers.Default) { translationsViewModel.viewAction().first() }
        delay(100)

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo)

        with(viewActionAsync.await()) {
            assertTrue(this is TranslationsViewModel.ViewAction.ShowDownloadTranslationFailedError)
            assertEquals(MockContents.kjvTranslationInfo, translationToDownload)
        }
        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertTrue(translationItems.isEmpty())
            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }
        verify(exactly = 0) { translationsViewModel.refreshTranslations(any()) }
    }

    @Test
    fun `test downloadTranslation()`() = runTest {
        every { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow {
            emit(1)
            emit(50)
            emit(99)
            emit(100)
        }
        every { translationsViewModel.refreshTranslations(false) } returns Unit

        val viewActionAsync = async(Dispatchers.Default) { translationsViewModel.viewAction().first() }
        val viewStatesAsync = async(Dispatchers.Default) {
            translationsViewModel
                    .viewState()
                    .buffer() // makes sure we could handle all updates
                    .drop(1) // skip the initial state
                    .take(6)
                    .toList()
        }
        delay(100)

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo)

        assertTrue(viewActionAsync.await() is TranslationsViewModel.ViewAction.ShowTranslationDownloaded)
        viewStatesAsync.await().forEachIndexed { index, viewState ->
            assertEquals(Settings.DEFAULT, viewState.settings)
            assertFalse(viewState.loading)
            assertTrue(viewState.translationItems.isEmpty())
            when (index) {
                0 -> {
                    assertTrue(viewState.downloadingTranslation)
                    assertEquals(0, viewState.downloadingProgress)
                }
                1 -> {
                    assertTrue(viewState.downloadingTranslation)
                    assertEquals(1, viewState.downloadingProgress)
                }
                2 -> {
                    assertTrue(viewState.downloadingTranslation)
                    assertEquals(50, viewState.downloadingProgress)
                }
                3 -> {
                    assertTrue(viewState.downloadingTranslation)
                    assertEquals(99, viewState.downloadingProgress)
                }
                4 -> {
                    assertTrue(viewState.downloadingTranslation)
                    assertEquals(100, viewState.downloadingProgress)
                }
                5 -> {
                    assertFalse(viewState.downloadingTranslation)
                    assertEquals(0, viewState.downloadingProgress)
                }
            }
            assertFalse(viewState.removingTranslation)
        }

        verify(exactly = 1) { translationsViewModel.refreshTranslations(false) }
    }

    @Test
    fun `test calling downloadTranslation() multiple times`() = runTest {
        every { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow { delay(1000) }
        every { translationsViewModel.refreshTranslations(false) } returns Unit

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo)

        delay(500)
        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo) // current downloading not finished yet, should do nothing

        assertTrue(translationsViewModel.viewState().first().downloadingTranslation)

        delay(200)
        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo) // current downloading not finished yet, should do nothing

        assertTrue(translationsViewModel.viewState().first().downloadingTranslation)

        delay(1000) // wait until current downloading finishes
        assertFalse(translationsViewModel.viewState().first().downloadingTranslation)
        verify(exactly = 1) { translationsViewModel.refreshTranslations(false) }

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo) // previous downloading finished, start downloading again

        delay(200)
        assertTrue(translationsViewModel.viewState().first().downloadingTranslation)

        delay(1000) // wait until current downloading finishes
        assertFalse(translationsViewModel.viewState().first().downloadingTranslation)
        verify(exactly = 2) { translationsViewModel.refreshTranslations(false) }
    }

    @Test
    fun `test cancelDownloadingTranslation() without ongoing translations`() {
        translationsViewModel.cancelDownloadingTranslation()
    }

    @Test
    fun `test cancelDownloadingTranslation()`() = runTest {
        every { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow<Int> {
            delay(1000)
            fail()
        }.flowOn(Dispatchers.Default)

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo)
        delay(100)
        translationsViewModel.cancelDownloadingTranslation()

        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertTrue(translationItems.isEmpty())
            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }
    }

    @Test
    fun `test removeTranslation() with exception`() = runTest {
        coEvery { translationManager.removeTranslation(MockContents.kjvTranslationInfo) } throws RuntimeException("Random exception")

        val viewActionAsync = async(Dispatchers.Default) { translationsViewModel.viewAction().first() }
        delay(100)

        translationsViewModel.removeTranslation(MockContents.kjvDownloadedTranslationInfo)

        with(viewActionAsync.await()) {
            assertTrue(this is TranslationsViewModel.ViewAction.ShowRemoveTranslationFailedError)
            assertEquals(MockContents.kjvDownloadedTranslationInfo, this.translationToRemove)
        }
        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertTrue(translationItems.isEmpty())
            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }
    }

    @Test
    fun `test removeTranslation()`() = runTest {
        coEvery { translationManager.removeTranslation(MockContents.kjvTranslationInfo) } returns Unit
        every { translationsViewModel.refreshTranslations(false) } returns Unit

        val viewActionAsync = async(Dispatchers.Default) { translationsViewModel.viewAction().first() }
        delay(100)

        translationsViewModel.removeTranslation(MockContents.kjvTranslationInfo)

        assertTrue(viewActionAsync.await() is TranslationsViewModel.ViewAction.ShowTranslationRemoved)
        with(translationsViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertTrue(translationItems.isEmpty())
            assertFalse(downloadingTranslation)
            assertEquals(0, downloadingProgress)
            assertFalse(removingTranslation)
        }
    }
}
