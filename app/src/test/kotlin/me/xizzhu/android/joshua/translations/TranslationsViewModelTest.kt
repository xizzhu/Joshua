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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationsViewModelTest : BaseUnitTest() {
    private lateinit var navigator: Navigator
    private lateinit var translationsInteractor: TranslationsInteractor
    private lateinit var translationsActivity: TranslationsActivity

    private lateinit var translationsViewModel: TranslationsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        navigator = mockk()

        translationsInteractor = mockk()
        coEvery { translationsInteractor.availableTranslations() } returns listOf(MockContents.kjvTranslationInfo)
        coEvery { translationsInteractor.downloadedTranslations() } returns listOf(MockContents.cuvDownloadedTranslationInfo)
        coEvery { translationsInteractor.currentTranslation() } returns MockContents.cuvShortName

        translationsActivity = mockk()
        every { translationsActivity.getString(R.string.header_available_translations) } returns "AVAILABLE TRANSLATIONS"

        translationsViewModel = spyk(TranslationsViewModel(navigator, translationsInteractor, translationsActivity, testCoroutineScope))
    }

    @Test
    fun `test loadTranslation at init`() = runBlocking {
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
    fun `test refreshTranslations without available and downloaded`() = runBlocking {
        coEvery { translationsInteractor.availableTranslations() } returns emptyList()
        coEvery { translationsInteractor.downloadedTranslations() } returns emptyList()
        coEvery { translationsInteractor.refreshTranslationList(true) } returns Unit

        translationsViewModel.refreshTranslations(true)
        assertTrue(translationsViewModel.translations().first() is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test refreshTranslations without available`() = runBlocking {
        coEvery { translationsInteractor.availableTranslations() } returns emptyList()
        coEvery { translationsInteractor.refreshTranslationList(true) } returns Unit

        translationsViewModel.refreshTranslations(true)

        val actual = translationsViewModel.translations().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(2, actual.data.items.size)
        assertEquals("Chinese", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(MockContents.cuvDownloadedTranslationInfo, (actual.data.items[1] as TranslationItem).translationInfo)
    }

    @Test
    fun `test refreshTranslations without downloaded`() = runBlocking {
        coEvery { translationsInteractor.downloadedTranslations() } returns emptyList()
        coEvery { translationsInteractor.refreshTranslationList(true) } returns Unit

        translationsViewModel.refreshTranslations(true)

        val actual = translationsViewModel.translations().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(3, actual.data.items.size)
        assertEquals("AVAILABLE TRANSLATIONS", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals("English", (actual.data.items[1] as TitleItem).title.toString())
        assertEquals(MockContents.kjvTranslationInfo, (actual.data.items[2] as TranslationItem).translationInfo)
    }

    @Test
    fun `test refreshTranslations`() = runBlocking {
        coEvery { translationsInteractor.availableTranslations() } returns listOf(MockContents.kjvTranslationInfo, MockContents.msgTranslationInfo)
        coEvery { translationsInteractor.downloadedTranslations() } returns listOf(MockContents.bbeDownloadedTranslationInfo, MockContents.cuvDownloadedTranslationInfo)
        coEvery { translationsInteractor.refreshTranslationList(true) } returns Unit

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
    fun `test downloadTranslation with error`() = runBlocking {
        val error = RuntimeException("Random exception")
        coEvery { translationsInteractor.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow {
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
    fun `test downloadTranslation`() = runBlocking {
        coEvery { translationsInteractor.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow {
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
    fun `test removeTranslation with error`() = runBlocking {
        val error = RuntimeException("Random exception")
        coEvery { translationsInteractor.removeTranslation(MockContents.kjvTranslationInfo) } throws error
        every { translationsViewModel.refreshTranslations(any()) } returns Unit

        val actual = translationsViewModel.removeTranslation(MockContents.kjvTranslationInfo).toList()
        assertEquals(2, actual.size)
        assertEquals(BaseViewModel.ViewData.Loading(), actual[0])
        assertEquals(BaseViewModel.ViewData.Failure(error), actual[1])

        verify(exactly = 0) { translationsViewModel.refreshTranslations(any()) }
    }

    @Test
    fun `test removeTranslation`() = runBlocking {
        coEvery { translationsInteractor.removeTranslation(MockContents.kjvTranslationInfo) } returns Unit
        every { translationsViewModel.refreshTranslations(any()) } returns Unit

        val actual = translationsViewModel.removeTranslation(MockContents.kjvTranslationInfo).toList()
        assertEquals(2, actual.size)
        assertEquals(BaseViewModel.ViewData.Loading(), actual[0])
        assertEquals(BaseViewModel.ViewData.Success(Unit), actual[1])

        verify(exactly = 1) { translationsViewModel.refreshTranslations(false) }
        verify(exactly = 0) { translationsViewModel.refreshTranslations(true) }
    }
}
