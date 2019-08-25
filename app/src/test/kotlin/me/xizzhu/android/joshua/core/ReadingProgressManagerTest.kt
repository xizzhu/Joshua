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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class ReadingProgressManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var readingProgressRepository: ReadingProgressRepository
    private lateinit var readingProgressManager: ReadingProgressManager

    @Before
    override fun setup() {
        super.setup()

        readingProgressManager = ReadingProgressManager(bibleReadingManager, readingProgressRepository)
    }

    @Test
    fun testTrackingNothing() {
        runBlocking {
            `when`(bibleReadingManager.observeCurrentVerseIndex()).thenReturn(emptyFlow())
            `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(emptyFlow())

            readingProgressManager.startTracking()
            readingProgressManager.stopTracking()

            verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())
        }
    }

    @Test
    fun testTracking() {
        runBlocking {
            `when`(bibleReadingManager.observeCurrentVerseIndex()).thenReturn(flowOf(VerseIndex(1, 2, 3)))
            `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))

            readingProgressManager = spy(readingProgressManager)
            doReturn(0L).`when`(readingProgressManager).timeSpentThresholdInMillis()
            doReturn(1L).`when`(readingProgressManager).now()

            readingProgressManager.startTracking()
            readingProgressManager.stopTracking()
            verify(readingProgressRepository, times(1)).trackReadingProgress(1, 2, 0L, 1L)
        }
    }

    @Test
    fun testTrackingWithTooLowTimeSpent() {
        runBlocking {
            `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
            readingProgressManager.startTracking()
            verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())

            `when`(bibleReadingManager.observeCurrentVerseIndex()).thenReturn(flowOf(VerseIndex(1, 2, 3)))
            readingProgressManager.stopTracking()
            verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())
        }
    }

    @Test
    fun testTrackingWithoutCurrentTranslation() {
        runBlocking {
            `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
            readingProgressManager.startTracking()
            verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())

            `when`(bibleReadingManager.observeCurrentVerseIndex()).thenReturn(flowOf(VerseIndex(1, 2, 3)))
            readingProgressManager.stopTracking()
            verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())
        }
    }

    @Test
    fun testStartTrackingMultipleTimes() {
        runBlocking {
            `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
            `when`(bibleReadingManager.observeCurrentVerseIndex()).thenReturn(flowOf(VerseIndex.INVALID))

            launch(Dispatchers.Unconfined) {
                readingProgressManager.startTracking()
            }
            launch(Dispatchers.Unconfined) {
                readingProgressManager.startTracking()
            }
            readingProgressManager.stopTracking()

            verify(bibleReadingManager, times(1)).observeCurrentVerseIndex()
        }
    }
}
