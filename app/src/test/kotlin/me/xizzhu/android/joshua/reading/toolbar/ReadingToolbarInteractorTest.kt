/*
 * Copyright (C) 2019 Xizhi Zhu
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

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingToolbarInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var translationManager: TranslationManager

    private lateinit var readingToolbarInteractor: ReadingToolbarInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        readingToolbarInteractor = ReadingToolbarInteractor(bibleReadingManager, translationManager, testDispatcher)
    }

    @Test
    fun testDownloadedTranslations() = testDispatcher.runBlockingTest {
        `when`(translationManager.downloadedTranslations()).thenReturn(
                flowOf(
                        emptyList(),
                        emptyList(),
                        listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo),
                        listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo),
                        listOf(MockContents.bbeTranslationInfo)
                )
        )

        assertEquals(
                listOf(
                        ViewData.success(emptyList()),
                        ViewData.success(listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo)),
                        ViewData.success(listOf(MockContents.bbeTranslationInfo))
                ),
                readingToolbarInteractor.downloadedTranslations().toList()
        )
    }

    @Test
    fun testCurrentTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))

        assertEquals(
                listOf(ViewData.success(MockContents.kjvShortName)),
                readingToolbarInteractor.currentTranslation().toList()
        )
    }

    @Test
    fun testCurrentVerseIndex() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.observeCurrentVerseIndex()).thenReturn(
                flowOf(VerseIndex.INVALID, VerseIndex(1, 2, 3), VerseIndex.INVALID, VerseIndex.INVALID)
        )

        assertEquals(
                listOf(ViewData.success(VerseIndex(1, 2, 3))),
                readingToolbarInteractor.currentVerseIndex().toList()
        )
    }

    @Test
    fun testBookShortNames() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

        assertEquals(
                listOf(ViewData.success(MockContents.kjvBookShortNames)),
                readingToolbarInteractor.bookShortNames().toList()
        )
    }
}
