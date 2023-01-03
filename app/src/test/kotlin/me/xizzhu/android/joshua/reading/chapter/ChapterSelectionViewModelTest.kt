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

package me.xizzhu.android.joshua.reading.chapter

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents

class ChapterSelectionViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager

    private lateinit var chapterSelectionViewModel: ChapterSelectionViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk<BibleReadingManager>().apply {
            every { currentTranslation() } returns emptyFlow()
            every { currentVerseIndex() } returns emptyFlow()
        }

        chapterSelectionViewModel = ChapterSelectionViewModel(bibleReadingManager)
    }

    @Test
    fun `test ViewState_currentBookIndex and currentChapterIndex, called in constructor`() = runTest {
        every { bibleReadingManager.currentVerseIndex() } returns flowOf(VerseIndex(1, 2, 3))

        chapterSelectionViewModel = ChapterSelectionViewModel(bibleReadingManager)
        assertEquals(
            ChapterSelectionViewModel.ViewState(
                currentBookIndex = 1,
                currentChapterIndex = 2,
                chapterSelectionItems = emptyList(),
                error = null,
            ),
            chapterSelectionViewModel.viewState().first()
        )
    }

    @Test
    fun `test ViewState_chapterSelectionItems, called in constructor`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames

        chapterSelectionViewModel = ChapterSelectionViewModel(bibleReadingManager)
        assertEquals(
            ChapterSelectionViewModel.ViewState(
                currentBookIndex = -1,
                currentChapterIndex = -1,
                chapterSelectionItems = MockContents.kjvBookNames.mapIndexed { index, bookName ->
                    ChapterSelectionItem(bookIndex = index, bookName = bookName, chapterCount = Bible.getChapterCount(index))
                },
                error = null,
            ),
            chapterSelectionViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectChapter(), with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(any()) } throws RuntimeException("random exception")

        chapterSelectionViewModel.selectChapter(bookToSelect = 1, chapterToSelect = 2)
        assertEquals(
            ChapterSelectionViewModel.ViewState(
                currentBookIndex = -1,
                currentChapterIndex = -1,
                chapterSelectionItems = emptyList(),
                error = ChapterSelectionViewModel.ViewState.Error.ChapterSelectionError(bookToSelect = 1, chapterToSelect = 2),
            ),
            chapterSelectionViewModel.viewState().first()
        )

        chapterSelectionViewModel.markErrorAsShown(ChapterSelectionViewModel.ViewState.Error.ChapterSelectionError(bookToSelect = 0, chapterToSelect = 0))
        assertEquals(
            ChapterSelectionViewModel.ViewState(
                currentBookIndex = -1,
                currentChapterIndex = -1,
                chapterSelectionItems = emptyList(),
                error = ChapterSelectionViewModel.ViewState.Error.ChapterSelectionError(bookToSelect = 1, chapterToSelect = 2),
            ),
            chapterSelectionViewModel.viewState().first()
        )

        chapterSelectionViewModel.markErrorAsShown(ChapterSelectionViewModel.ViewState.Error.ChapterSelectionError(bookToSelect = 1, chapterToSelect = 2))
        assertEquals(
            ChapterSelectionViewModel.ViewState(
                currentBookIndex = -1,
                currentChapterIndex = -1,
                chapterSelectionItems = emptyList(),
                error = null,
            ),
            chapterSelectionViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectChapter()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(1, 2, 0)) } returns Unit

        chapterSelectionViewModel.selectChapter(bookToSelect = 1, chapterToSelect = 2)

        assertEquals(
            ChapterSelectionViewModel.ViewState(
                currentBookIndex = -1,
                currentChapterIndex = -1,
                chapterSelectionItems = emptyList(),
                error = null,
            ),
            chapterSelectionViewModel.viewState().first()
        )
        coVerify(exactly = 1) { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(1, 2, 0)) }
    }
}
