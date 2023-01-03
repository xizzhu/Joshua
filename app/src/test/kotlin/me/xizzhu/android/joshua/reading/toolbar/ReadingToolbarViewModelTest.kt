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

package me.xizzhu.android.joshua.reading.toolbar

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents

@RunWith(RobolectricTestRunner::class)
class ReadingToolbarViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var translationManager: TranslationManager
    private lateinit var application: Application

    private lateinit var readingToolbarViewModel: ReadingToolbarViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk<BibleReadingManager>().apply {
            every { currentTranslation() } returns emptyFlow()
            every { currentVerseIndex() } returns emptyFlow()
            every { parallelTranslations() } returns emptyFlow()
        }
        translationManager = mockk<TranslationManager>().apply {
            every { downloadedTranslations() } returns emptyFlow()
        }
        application = ApplicationProvider.getApplicationContext()

        readingToolbarViewModel = ReadingToolbarViewModel(bibleReadingManager, translationManager, application)
    }

    @Test
    fun `test ViewState_title, called in constructor`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        every { bibleReadingManager.currentVerseIndex() } returns flowOf(VerseIndex(1, 2, 3))
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames

        readingToolbarViewModel = ReadingToolbarViewModel(bibleReadingManager, translationManager, application)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = "Ex., 3",
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test ViewState_translationItems, called in constructor`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        every { bibleReadingManager.parallelTranslations() } returns flowOf(listOf(MockContents.msgShortName))
        every { translationManager.downloadedTranslations() } returns flowOf(listOf(
            MockContents.kjvDownloadedTranslationInfo,
            MockContents.bbeDownloadedTranslationInfo,
            MockContents.cuvDownloadedTranslationInfo,
            MockContents.msgDownloadedTranslationInfo,
        ))

        readingToolbarViewModel = ReadingToolbarViewModel(bibleReadingManager, translationManager, application)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = listOf(
                    TranslationItem.Translation(
                        translationShortName = "BBE",
                        isCurrentTranslation = false,
                        isParallelTranslation = false,
                    ),
                    TranslationItem.Translation(
                        translationShortName = "KJV",
                        isCurrentTranslation = true,
                        isParallelTranslation = false,
                    ),
                    TranslationItem.Translation(
                        translationShortName = "MSG",
                        isCurrentTranslation = false,
                        isParallelTranslation = true,
                    ),
                    TranslationItem.Translation(
                        translationShortName = "中文和合本",
                        isCurrentTranslation = false,
                        isParallelTranslation = false,
                    ),
                    TranslationItem.More,
                ),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test requestParallelTranslation(), with exception`() = runTest {
        coEvery { bibleReadingManager.requestParallelTranslation(any()) } throws RuntimeException("random exception")

        readingToolbarViewModel.requestParallelTranslation(MockContents.kjvShortName)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRequestingError("KJV"),
            ),
            readingToolbarViewModel.viewState().first()
        )

        readingToolbarViewModel.markErrorAsShown(ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRequestingError("BBE"))
        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRequestingError("KJV"),
            ),
            readingToolbarViewModel.viewState().first()
        )

        readingToolbarViewModel.markErrorAsShown(ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRequestingError("KJV"))
        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test requestParallelTranslation()`() = runTest {
        coEvery { bibleReadingManager.requestParallelTranslation(any()) } returns Unit

        readingToolbarViewModel.requestParallelTranslation(MockContents.kjvShortName)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test removeParallelTranslation(), with exception`() = runTest {
        coEvery { bibleReadingManager.removeParallelTranslation(any()) } throws RuntimeException("random exception")

        readingToolbarViewModel.removeParallelTranslation(MockContents.kjvShortName)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRemovalError("KJV"),
            ),
            readingToolbarViewModel.viewState().first()
        )

        readingToolbarViewModel.markErrorAsShown(ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRemovalError("BBE"))
        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRemovalError("KJV"),
            ),
            readingToolbarViewModel.viewState().first()
        )

        readingToolbarViewModel.markErrorAsShown(ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRemovalError("KJV"))
        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test removeParallelTranslation()`() = runTest {
        coEvery { bibleReadingManager.removeParallelTranslation(any()) } returns Unit

        readingToolbarViewModel.removeParallelTranslation(MockContents.kjvShortName)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectTranslation(), with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentTranslation(any()) } throws RuntimeException("random exception")

        readingToolbarViewModel.selectTranslation(MockContents.kjvShortName)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = ReadingToolbarViewModel.ViewState.Error.TranslationSelectionError("KJV"),
            ),
            readingToolbarViewModel.viewState().first()
        )

        readingToolbarViewModel.markErrorAsShown(ReadingToolbarViewModel.ViewState.Error.TranslationSelectionError("BBE"))
        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = ReadingToolbarViewModel.ViewState.Error.TranslationSelectionError("KJV"),
            ),
            readingToolbarViewModel.viewState().first()
        )

        readingToolbarViewModel.markErrorAsShown(ReadingToolbarViewModel.ViewState.Error.TranslationSelectionError("KJV"))
        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectTranslation(), failed to remove selected from parallel`() = runTest {
        coEvery { bibleReadingManager.saveCurrentTranslation(any()) } returns Unit
        coEvery { bibleReadingManager.removeParallelTranslation(any()) } throws RuntimeException("random exception")

        readingToolbarViewModel.selectTranslation(MockContents.kjvShortName)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectTranslation()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentTranslation(any()) } returns Unit
        coEvery { bibleReadingManager.removeParallelTranslation(any()) } returns Unit

        readingToolbarViewModel.selectTranslation(MockContents.kjvShortName)

        assertEquals(
            ReadingToolbarViewModel.ViewState(
                title = application.getString(R.string.app_name),
                translationItems = emptyList(),
                error = null,
            ),
            readingToolbarViewModel.viewState().first()
        )
    }
}
