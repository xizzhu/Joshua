/*
 * Copyright (C) 2023 Xizhi Zhu
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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import me.xizzhu.android.joshua.core.Settings

class TranslationsViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var translationManager: TranslationManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application

    private lateinit var translationsViewModel: TranslationsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk<BibleReadingManager>().apply {
            every { currentTranslation() } returns flowOf(MockContents.cuvShortName)
        }

        translationManager = mockk<TranslationManager>().apply {
            every { availableTranslations() } returns flowOf(listOf(MockContents.kjvTranslationInfo))
            every { downloadedTranslations() } returns flowOf(listOf(MockContents.cuvDownloadedTranslationInfo))
            coEvery { reload(any()) } returns Unit
        }

        settingsManager = mockk<SettingsManager>().apply {
            every { settings() } returns flowOf(Settings.DEFAULT)
        }

        application = mockk()
        every { application.getString(R.string.header_available_translations) } returns "AVAILABLE TRANSLATIONS"

        translationsViewModel = TranslationsViewModel(bibleReadingManager, translationManager, settingsManager, application, testCoroutineDispatcherProvider)
    }

    @Test
    fun `test loadTranslations(), called in init`() = runTest {
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
        coVerify(exactly = 1) { translationManager.reload(false) }
    }

    private fun createDefaultViewState(): TranslationsViewModel.ViewState = TranslationsViewModel.ViewState(
        loading = false,
        items = listOf(
            TranslationsItem.Header(Settings.DEFAULT, "Chinese", hideDivider = true),
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.cuvDownloadedTranslationInfo, isCurrentTranslation = true),
            TranslationsItem.Header(Settings.DEFAULT, "AVAILABLE TRANSLATIONS", hideDivider = false),
            TranslationsItem.Header(Settings.DEFAULT, "English", hideDivider = true),
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, isCurrentTranslation = false),
        ),
        translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
        translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
        error = null,
    )

    @Test
    fun `test loadTranslations(), without available and downloaded translations`() = runTest {
        every { translationManager.availableTranslations() } returns flowOf(emptyList())
        every { translationManager.downloadedTranslations() } returns flowOf(emptyList())

        translationsViewModel.loadTranslations(true)
        assertEquals(
            TranslationsViewModel.ViewState(
                loading = false,
                items = emptyList(),
                translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
                translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
                error = TranslationsViewModel.ViewState.Error.NoTranslationsError,
            ),
            translationsViewModel.viewState().first()
        )
        coVerify(exactly = 1) { translationManager.reload(true) }

        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.TranslationNotInstalledError)
        assertEquals(
            TranslationsViewModel.ViewState(
                loading = false,
                items = emptyList(),
                translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
                translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
                error = TranslationsViewModel.ViewState.Error.NoTranslationsError,
            ),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.NoTranslationsError)
        assertEquals(
            TranslationsViewModel.ViewState(
                loading = false,
                items = emptyList(),
                translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
                translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
                error = null,
            ),
            translationsViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadTranslations(), with downloaded but without available translations`() = runTest {
        every { translationManager.availableTranslations() } returns flowOf(emptyList())
        every { translationManager.downloadedTranslations() } returns flowOf(listOf(
            MockContents.cuvDownloadedTranslationInfo, MockContents.bbeDownloadedTranslationInfo, MockContents.msgDownloadedTranslationInfo
        ))

        translationsViewModel.loadTranslations(true)
        assertEquals(
            TranslationsViewModel.ViewState(
                loading = false,
                items = listOf(
                    TranslationsItem.Header(Settings.DEFAULT, "English", hideDivider = true),
                    TranslationsItem.Translation(Settings.DEFAULT, MockContents.bbeDownloadedTranslationInfo, isCurrentTranslation = false),
                    TranslationsItem.Translation(Settings.DEFAULT, MockContents.msgDownloadedTranslationInfo, isCurrentTranslation = false),
                    TranslationsItem.Header(Settings.DEFAULT, "Chinese", hideDivider = true),
                    TranslationsItem.Translation(Settings.DEFAULT, MockContents.cuvDownloadedTranslationInfo, isCurrentTranslation = true),
                ),
                translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
                translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
                error = null,
            ),
            translationsViewModel.viewState().first()
        )
        coVerify(exactly = 1) { translationManager.reload(true) }
    }

    @Test
    fun `test loadTranslations(), with available but without downloaded translations`() = runTest {
        every { translationManager.availableTranslations() } returns flowOf(listOf(
            MockContents.cuvTranslationInfo, MockContents.bbeTranslationInfo, MockContents.msgTranslationInfo
        ))
        every { translationManager.downloadedTranslations() } returns flowOf(emptyList())

        translationsViewModel.loadTranslations(true)
        assertEquals(
            TranslationsViewModel.ViewState(
                loading = false,
                items = listOf(
                    TranslationsItem.Header(Settings.DEFAULT, "AVAILABLE TRANSLATIONS", hideDivider = false),
                    TranslationsItem.Header(Settings.DEFAULT, "English", hideDivider = true),
                    TranslationsItem.Translation(Settings.DEFAULT, MockContents.bbeTranslationInfo, isCurrentTranslation = false),
                    TranslationsItem.Translation(Settings.DEFAULT, MockContents.msgTranslationInfo, isCurrentTranslation = false),
                    TranslationsItem.Header(Settings.DEFAULT, "Chinese", hideDivider = true),
                    TranslationsItem.Translation(Settings.DEFAULT, MockContents.cuvTranslationInfo, isCurrentTranslation = false),
                ),
                translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
                translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
                error = null,
            ),
            translationsViewModel.viewState().first()
        )
        coVerify(exactly = 1) { translationManager.reload(true) }
    }

    @Test
    fun `test selectTranslation(), with translation not downloaded yet`() = runTest {
        translationsViewModel.selectTranslation(MockContents.kjvTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(error = TranslationsViewModel.ViewState.Error.TranslationNotInstalledError),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.TranslationNotInstalledError)
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }

    @Test
    fun `test selectTranslation(), with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentTranslation("KJV") } throws RuntimeException("random exception")

        translationsViewModel.selectTranslation(MockContents.kjvDownloadedTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(error = TranslationsViewModel.ViewState.Error.TranslationSelectionError(MockContents.kjvDownloadedTranslationInfo)),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.TranslationSelectionError(MockContents.kjvDownloadedTranslationInfo))
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }

    @Test
    fun `test selectTranslation()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentTranslation("KJV") } returns Unit

        val viewAction = async(Dispatchers.Unconfined) { translationsViewModel.viewAction().first() }

        translationsViewModel.selectTranslation(MockContents.kjvDownloadedTranslationInfo)

        assertEquals(TranslationsViewModel.ViewAction.GoBack, viewAction.await())
    }

    @Test
    fun `test downloadTranslation(), with translation already downloaded`() = runTest {
        translationsViewModel.downloadTranslation(MockContents.kjvDownloadedTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(error = TranslationsViewModel.ViewState.Error.TranslationAlreadyInstalledError),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.TranslationAlreadyInstalledError)
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }

    @Test
    fun `test downloadTranslation(), with exception`() = runTest {
        every { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flowOf(-1)

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(
                translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Completed(successful = false),
                error = TranslationsViewModel.ViewState.Error.TranslationDownloadingError(MockContents.kjvTranslationInfo)
            ),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markTranslationDownloadingStateAsIdle()
        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.TranslationDownloadingError(MockContents.kjvTranslationInfo))
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }

    @Test
    fun `test downloadTranslation(), with cancellation`() = runTest {
        every { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow {
            while (true) delay(100L)
        }

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Downloading(progress = 0)),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.cancelDownloadingTranslation()
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }

    @Test
    fun `test downloadTranslation()`() = runTest {
        every { translationManager.downloadTranslation(MockContents.kjvTranslationInfo) } returns flow {
            delay(100L)
            emit(1)

            delay(100L)
            emit(99)

            delay(100L)
            emit(100)

            delay(100L)
            emit(101)
        }

        translationsViewModel.downloadTranslation(MockContents.kjvTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Downloading(progress = 0)),
            translationsViewModel.viewState().first()
        )

        delay(150L)
        assertEquals(
            createDefaultViewState().copy(translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Downloading(progress = 1)),
            translationsViewModel.viewState().first()
        )

        delay(100L)
        assertEquals(
            createDefaultViewState().copy(translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Downloading(progress = 99)),
            translationsViewModel.viewState().first()
        )

        delay(100L)
        assertEquals(
            createDefaultViewState().copy(translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Installing),
            translationsViewModel.viewState().first()
        )

        delay(100L)
        assertEquals(
            createDefaultViewState().copy(translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Completed(successful = true)),
            translationsViewModel.viewState().first()
        )
    }

    @Test
    fun `test removeTranslation(), with translation not yet downloaded`() = runTest {
        translationsViewModel.removeTranslation(MockContents.kjvTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(error = TranslationsViewModel.ViewState.Error.TranslationNotInstalledError),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.TranslationNotInstalledError)
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }

    @Test
    fun `test removeTranslation(), with exception`() = runTest {
        coEvery { translationManager.removeTranslation(MockContents.kjvDownloadedTranslationInfo) } throws RuntimeException("random exception")

        translationsViewModel.removeTranslation(MockContents.kjvDownloadedTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(
                translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Completed(successful = false),
                error = TranslationsViewModel.ViewState.Error.TranslationRemovalError(MockContents.kjvDownloadedTranslationInfo)
            ),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markTranslationRemovalStateAsIdle()
        translationsViewModel.markErrorAsShown(TranslationsViewModel.ViewState.Error.TranslationRemovalError(MockContents.kjvDownloadedTranslationInfo))
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }

    @Test
    fun `test removeTranslation()`() = runTest {
        coEvery { translationManager.removeTranslation(MockContents.kjvDownloadedTranslationInfo) } returns Unit

        translationsViewModel.removeTranslation(MockContents.kjvDownloadedTranslationInfo)
        assertEquals(
            createDefaultViewState().copy(
                translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Completed(successful = true)
            ),
            translationsViewModel.viewState().first()
        )

        translationsViewModel.markTranslationRemovalStateAsIdle()
        assertEquals(createDefaultViewState(), translationsViewModel.viewState().first())
    }
}
