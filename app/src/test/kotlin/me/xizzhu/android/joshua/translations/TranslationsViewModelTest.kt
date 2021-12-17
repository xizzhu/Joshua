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

package me.xizzhu.android.joshua.translations

import android.app.Application
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.cuvShortName)

        translationManager = mockk()
        coEvery { translationManager.availableTranslations() } returns flowOf(listOf(MockContents.kjvTranslationInfo))
        coEvery { translationManager.downloadedTranslations() } returns flowOf(listOf(MockContents.cuvDownloadedTranslationInfo))
        coEvery { translationManager.reload(any()) } returns Unit

        settingsManager = mockk()

        application = mockk()
        every { application.getString(R.string.header_available_translations) } returns "AVAILABLE TRANSLATIONS"

        translationsViewModel = spyk(TranslationsViewModel(bibleReadingManager, translationManager, settingsManager, application))
    }

    @Test
    fun `test loadTranslation at init`() = runTest {
        val actual = translationsViewModel.translations().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(5, actual.data.items.size)
        assertEquals("Chinese", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(MockContents.cuvDownloadedTranslationInfo, (actual.data.items[1] as TranslationItem).translationInfo)
        assertEquals("AVAILABLE TRANSLATIONS", (actual.data.items[2] as TitleItem).title.toString())
        assertEquals("English", (actual.data.items[3] as TitleItem).title.toString())
        assertEquals(MockContents.kjvTranslationInfo, (actual.data.items[4] as TranslationItem).translationInfo)
    }

    @Test
    fun `test refreshTranslations without available and downloaded`() = runTest {
        coEvery { translationManager.availableTranslations() } returns flowOf(emptyList())
        coEvery { translationManager.downloadedTranslations() } returns flowOf(emptyList())

        translationsViewModel.refreshTranslations(true)
        assertTrue(translationsViewModel.translations().first() is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test refreshTranslations without available`() = runTest {
        coEvery { translationManager.availableTranslations() } returns flowOf(emptyList())

        translationsViewModel.refreshTranslations(true)

        val actual = translationsViewModel.translations().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(2, actual.data.items.size)
        assertEquals("Chinese", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(MockContents.cuvDownloadedTranslationInfo, (actual.data.items[1] as TranslationItem).translationInfo)
    }

    @Test
    fun `test refreshTranslations without downloaded`() = runTest {
        coEvery { translationManager.downloadedTranslations() } returns flowOf(emptyList())

        translationsViewModel.refreshTranslations(true)

        val actual = translationsViewModel.translations().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(3, actual.data.items.size)
        assertEquals("AVAILABLE TRANSLATIONS", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals("English", (actual.data.items[1] as TitleItem).title.toString())
        assertEquals(MockContents.kjvTranslationInfo, (actual.data.items[2] as TranslationItem).translationInfo)
    }

    @Test
    fun `test refreshTranslations`() = runTest {
        coEvery { translationManager.availableTranslations() } returns flowOf(listOf(MockContents.kjvTranslationInfo, MockContents.msgTranslationInfo))
        coEvery { translationManager.downloadedTranslations() } returns flowOf(listOf(MockContents.bbeDownloadedTranslationInfo, MockContents.cuvDownloadedTranslationInfo))

        translationsViewModel.refreshTranslations(true)

        val actual = translationsViewModel.translations().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(8, actual.data.items.size)
        assertEquals("English", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(MockContents.bbeDownloadedTranslationInfo, (actual.data.items[1] as TranslationItem).translationInfo)
        assertEquals("Chinese", (actual.data.items[2] as TitleItem).title.toString())
        assertEquals(MockContents.cuvDownloadedTranslationInfo, (actual.data.items[3] as TranslationItem).translationInfo)
        assertEquals("AVAILABLE TRANSLATIONS", (actual.data.items[4] as TitleItem).title.toString())
        assertEquals("English", (actual.data.items[5] as TitleItem).title.toString())
        assertEquals(MockContents.kjvTranslationInfo, (actual.data.items[6] as TranslationItem).translationInfo)
        assertEquals(MockContents.msgTranslationInfo, (actual.data.items[7] as TranslationItem).translationInfo)
    }

    @Test
    fun `test downloadTranslation with error`() = runTest {
        val error = RuntimeException("Random exception")
        coEvery { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow {
            emit(1)
            emit(5)
            throw error
        }
        every { translationsViewModel.refreshTranslations(any()) } returns Unit

        val actual = translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo).toList()
        assertEquals(3, actual.size)
        assertEquals(BaseViewModel.ViewData.Loading(1), actual[0])
        assertEquals(BaseViewModel.ViewData.Loading(5), actual[1])
        assertEquals(BaseViewModel.ViewData.Failure(error), actual[2])

        verify(exactly = 0) { translationsViewModel.refreshTranslations(any()) }
    }

    @Test
    fun `test downloadTranslation`() = runTest {
        coEvery { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow {
            emit(1)
            emit(50)
            emit(100)
        }
        every { translationsViewModel.refreshTranslations(any()) } returns Unit

        val actual = translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo).toList()
        assertEquals(3, actual.size)
        assertEquals(BaseViewModel.ViewData.Loading(1), actual[0])
        assertEquals(BaseViewModel.ViewData.Loading(50), actual[1])
        assertEquals(BaseViewModel.ViewData.Success(100), actual[2])

        verify(exactly = 1) { translationsViewModel.refreshTranslations(false) }
        verify(exactly = 0) { translationsViewModel.refreshTranslations(true) }
    }

    @Test
    fun `test removeTranslation with error`() = runTest {
        val error = RuntimeException("Random exception")
        coEvery { translationManager.removeTranslation(MockContents.kjvTranslationInfo) } throws error
        every { translationsViewModel.refreshTranslations(any()) } returns Unit

        val actual = translationsViewModel.removeTranslation(MockContents.kjvTranslationInfo).toList()
        assertEquals(2, actual.size)
        assertEquals(BaseViewModel.ViewData.Loading(), actual[0])
        assertEquals(BaseViewModel.ViewData.Failure(error), actual[1])

        verify(exactly = 0) { translationsViewModel.refreshTranslations(any()) }
    }

    @Test
    fun `test removeTranslation`() = runTest {
        coEvery { translationManager.removeTranslation(MockContents.kjvTranslationInfo) } returns Unit
        every { translationsViewModel.refreshTranslations(any()) } returns Unit

        val actual = translationsViewModel.removeTranslation(MockContents.kjvTranslationInfo).toList()
        assertEquals(2, actual.size)
        assertEquals(BaseViewModel.ViewData.Loading(), actual[0])
        assertEquals(BaseViewModel.ViewData.Success(Unit), actual[1])

        verify(exactly = 1) { translationsViewModel.refreshTranslations(false) }
        verify(exactly = 0) { translationsViewModel.refreshTranslations(true) }
    }
}
