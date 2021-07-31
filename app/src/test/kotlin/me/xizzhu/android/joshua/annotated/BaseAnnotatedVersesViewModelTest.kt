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

package me.xizzhu.android.joshua.annotated

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseAnnotatedVersesViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var verseAnnotationManager: VerseAnnotationManager<TestVerseAnnotation>
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var verseAnnotationViewModel: TestVerseAnnotationViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        verseAnnotationViewModel = TestVerseAnnotationViewModel(bibleReadingManager, verseAnnotationManager, settingsManager)
    }

    @Test
    fun testLoadingRequest() = runBlocking {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName))
        `when`(verseAnnotationManager.sortOrder()).thenReturn(flowOf(Constants.SORT_BY_BOOK))

        assertEquals(
                listOf(LoadingRequest(MockContents.kjvShortName, Constants.SORT_BY_BOOK)),
                verseAnnotationViewModel.loadingRequest().toList()
        )
    }

    @Test
    fun testAnnotationVerses() = runBlocking {
        val sortOrder = Constants.SORT_BY_BOOK
        val currentTranslation = MockContents.kjvShortName
        `when`(verseAnnotationManager.read(sortOrder)).thenReturn(listOf(
                TestVerseAnnotation(VerseIndex(0, 0, 0), 0L),
                TestVerseAnnotation(VerseIndex(0, 0, 1), 1L)
        ))
        `when`(bibleReadingManager.readVerses(
                currentTranslation,
                listOf(VerseIndex(0, 0, 0), VerseIndex(0, 0, 1)))
        ).thenReturn(mapOf(
                VerseIndex(0, 0, 0) to MockContents.kjvVerses[0],
                VerseIndex(0, 0, 1) to MockContents.kjvVerses[1]
        ))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)

        assertEquals(
                listOf(
                        AnnotatedVerses(
                                sortOrder,
                                listOf(
                                        TestVerseAnnotation(VerseIndex(0, 0, 0), 0L) to MockContents.kjvVerses[0],
                                        TestVerseAnnotation(VerseIndex(0, 0, 1), 1L) to MockContents.kjvVerses[1]
                                ),
                                MockContents.kjvBookNames,
                                MockContents.kjvBookShortNames
                        )
                ),
                verseAnnotationViewModel.annotatedVerses(LoadingRequest(currentTranslation, sortOrder)).toList()
        )
    }

    @Test
    fun testAnnotationVersesWithException() = runBlocking {
        val sortOrder = Constants.SORT_BY_BOOK
        val e = RuntimeException("random exception")
        `when`(verseAnnotationManager.read(sortOrder)).thenThrow(e)

        verseAnnotationViewModel.annotatedVerses(LoadingRequest("", sortOrder))
                .onCompletion { assertEquals(e, it) }
                .catch { }
                .collect()
    }
}
