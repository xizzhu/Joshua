/*
 * Copyright (C) 2020 Xizhi Zhu
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

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ChapterListInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager

    private lateinit var chapterListInteractor: ChapterListInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        chapterListInteractor = ChapterListInteractor(bibleReadingManager, testDispatcher)
    }

    @Test
    fun testBookNames() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)

        assertEquals(
                listOf(ViewData.success(MockContents.kjvBookNames)),
                chapterListInteractor.bookNames().toList()
        )
    }

    @Test
    fun testCurrentVerseIndex() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentVerseIndex()).thenReturn(
                flowOf(VerseIndex.INVALID, VerseIndex(1, 2, 3), VerseIndex.INVALID, VerseIndex.INVALID)
        )

        assertEquals(
                listOf(ViewData.success(VerseIndex(1, 2, 3))),
                chapterListInteractor.currentVerseIndex().toList()
        )
    }
}
