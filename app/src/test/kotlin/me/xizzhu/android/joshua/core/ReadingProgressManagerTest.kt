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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.utils.elapsedRealtime
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class ReadingProgressManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingRepository: BibleReadingRepository

    @Mock
    private lateinit var readingProgressRepository: ReadingProgressRepository
    private lateinit var readingProgressManager: ReadingProgressManager

    @BeforeTest
    override fun setup() {
        super.setup()

        readingProgressManager = ReadingProgressManager(bibleReadingRepository, readingProgressRepository)
    }

    @Test
    fun testTrackingNothing() = runBlocking {
        `when`(bibleReadingRepository.currentVerseIndex()).thenReturn(emptyFlow())
        `when`(bibleReadingRepository.currentTranslation()).thenReturn(emptyFlow())

        readingProgressManager.startTracking()
        readingProgressManager.stopTracking()

        verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())
    }

    @Test
    fun testTracking() = runBlocking {
        `when`(bibleReadingRepository.currentVerseIndex()).thenReturn(flowOf(VerseIndex(1, 2, 3)))
        `when`(bibleReadingRepository.currentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))

        elapsedRealtime = 1L
        readingProgressManager.startTracking()

        elapsedRealtime = ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS + 2L
        readingProgressManager.stopTracking()

        verify(readingProgressRepository, times(1))
                .trackReadingProgress(1, 2,
                        ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS + 1L,
                        ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS + 2L)
    }

    @Test
    fun testTrackingWithTooLowTimeSpent() = runBlocking {
        `when`(bibleReadingRepository.currentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
        readingProgressManager.startTracking()
        verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())

        `when`(bibleReadingRepository.currentVerseIndex()).thenReturn(flowOf(VerseIndex(1, 2, 3)))
        readingProgressManager.stopTracking()
        verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())
    }

    @Test
    fun testTrackingWithoutCurrentTranslation() = runBlocking {
        `when`(bibleReadingRepository.currentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
        readingProgressManager.startTracking()
        verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())

        `when`(bibleReadingRepository.currentVerseIndex()).thenReturn(flowOf(VerseIndex(1, 2, 3)))
        readingProgressManager.stopTracking()
        verify(readingProgressRepository, never()).trackReadingProgress(anyInt(), anyInt(), anyLong(), anyLong())
    }

    @Test
    fun testStartTrackingMultipleTimes() {
        runBlocking {
            `when`(bibleReadingRepository.currentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
            `when`(bibleReadingRepository.currentVerseIndex()).thenReturn(flowOf(VerseIndex.INVALID))

            launch(Dispatchers.Unconfined) {
                readingProgressManager.startTracking()
            }
            launch(Dispatchers.Unconfined) {
                readingProgressManager.startTracking()
            }
            readingProgressManager.stopTracking()

            verify(bibleReadingRepository, times(1)).currentVerseIndex()
        }
    }
}
