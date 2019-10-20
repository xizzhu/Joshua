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

package me.xizzhu.android.joshua.progress

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadingProgressInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var readingProgressManager: ReadingProgressManager
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var readingProgressInteractor: ReadingProgressInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        readingProgressInteractor = ReadingProgressInteractor(readingProgressManager, bibleReadingManager, settingsManager, testDispatcher)
    }

    @Test
    fun testReadReadingProgress() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
        val bookNames = MockContents.kjvBookNames
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(bookNames)
        val readingProgress = ReadingProgress(0, 0L, emptyList())
        `when`(readingProgressManager.read()).thenReturn(readingProgress)

        val loadingStateAsync = async { readingProgressInteractor.loadingState().take(2).toList() }
        assertEquals(Pair(bookNames, readingProgress), readingProgressInteractor.readReadingProgress())
        assertEquals(listOf(ViewData.loading(Unit), ViewData.success(Unit)), loadingStateAsync.await())
    }

    @Test
    fun testReadReadingProgressWithException() = testDispatcher.runBlockingTest {
        val exception = RuntimeException("random exception")
        `when`(bibleReadingManager.observeCurrentTranslation()).thenThrow(exception)

        val loadingStateAsync = async { readingProgressInteractor.loadingState().take(2).toList() }
        var exceptionThrown = false
        try {
            readingProgressInteractor.readReadingProgress()
        } catch (e: RuntimeException) {
            assertEquals(exception, e)
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
        assertEquals(listOf(ViewData.loading(Unit), ViewData.error(Unit, exception)), loadingStateAsync.await())
    }
}
