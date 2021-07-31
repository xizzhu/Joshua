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

package me.xizzhu.android.joshua.progress

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingProgressViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var readingProgressManager: ReadingProgressManager
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var readingProgressViewModel: ReadingProgressViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        readingProgressViewModel = ReadingProgressViewModel(bibleReadingManager, readingProgressManager, settingsManager)
    }

    @Test
    fun testReadingProgress() = runBlocking {
        val readingProgress = ReadingProgress(0, 0L, emptyList())
        val currentTranslation = MockContents.kjvShortName
        val bookNames = MockContents.kjvBookNames
        `when`(readingProgressManager.read()).thenReturn(readingProgress)
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(currentTranslation))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(bookNames)

        assertEquals(
                listOf(ReadingProgressViewData(readingProgress, bookNames)),
                readingProgressViewModel.readingProgress().toList()
        )
    }

    @Test
    fun testReadingProgressWithException() = runBlocking {
        val readingProgress = ReadingProgress(0, 0L, emptyList())
        val currentTranslation = MockContents.kjvShortName
        val exception = RuntimeException("Random exception")
        `when`(readingProgressManager.read()).thenReturn(readingProgress)
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(currentTranslation))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenThrow(exception)

        readingProgressViewModel.readingProgress()
                .onCompletion { assertEquals(exception, it) }
                .catch { }
                .collect()
    }
}
