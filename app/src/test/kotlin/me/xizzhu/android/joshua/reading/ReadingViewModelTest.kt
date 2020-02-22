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

package me.xizzhu.android.joshua.reading

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var readingProgressManager: ReadingProgressManager
    @Mock
    private lateinit var translationManager: TranslationManager
    @Mock
    private lateinit var bookmarkManager: VerseAnnotationManager<Bookmark>
    @Mock
    private lateinit var highlightManager: VerseAnnotationManager<Highlight>
    @Mock
    private lateinit var noteManager: VerseAnnotationManager<Note>
    @Mock
    private lateinit var strongNumberManager: StrongNumberManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var readingViewModel: ReadingViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        readingViewModel = ReadingViewModel(bibleReadingManager, readingProgressManager, translationManager,
                bookmarkManager, highlightManager, noteManager, strongNumberManager, settingsManager, testDispatcher)
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
                readingViewModel.downloadedTranslations().toList()
        )
    }

    @Test
    fun testCurrentTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))

        assertEquals(
                listOf(ViewData.success(MockContents.kjvShortName)),
                readingViewModel.currentTranslation().toList()
        )
    }

    @Test
    fun testCurrentVerseIndex() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentVerseIndex()).thenReturn(
                flowOf(VerseIndex.INVALID, VerseIndex(1, 2, 3), VerseIndex.INVALID, VerseIndex.INVALID)
        )

        assertEquals(
                listOf(ViewData.success(VerseIndex(1, 2, 3))),
                readingViewModel.currentVerseIndex().toList()
        )
    }

    @Test
    fun testBookNames() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)

        assertEquals(
                listOf(ViewData.success(MockContents.kjvBookNames)),
                readingViewModel.bookNames().toList()
        )
    }

    @Test
    fun testBookShortNames() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

        assertEquals(
                listOf(ViewData.success(MockContents.kjvBookShortNames)),
                readingViewModel.bookShortNames().toList()
        )
    }
}
