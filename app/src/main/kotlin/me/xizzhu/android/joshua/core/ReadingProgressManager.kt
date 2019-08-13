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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.logger.Log

data class ReadingProgress(val continuousReadingDays: Int, val lastReadingTimestamp: Long,
                           val chapterReadingStatus: List<ChapterReadingStatus>) {
    data class ChapterReadingStatus(val bookIndex: Int, val chapterIndex: Int, val readCount: Int,
                                    val timeSpentInMillis: Long, val lastReadingTimestamp: Long)
}

class ReadingProgressManager(private val bibleReadingManager: BibleReadingManager,
                             private val readingProgressRepository: ReadingProgressRepository) {
    companion object {
        private val TAG: String = ReadingProgressManager::class.java.simpleName
        private const val TIME_SPENT_THRESHOLD_IN_MILLIS = 2500L
    }

    private var currentVerseIndexObserver: ReceiveChannel<VerseIndex>? = null

    private var currentVerseIndex: VerseIndex = VerseIndex.INVALID
    private var lastTimestamp: Long = 0L

    suspend fun startTracking() {
        if (currentVerseIndexObserver != null) {
            return
        }

        lastTimestamp = System.currentTimeMillis()
        currentVerseIndexObserver = bibleReadingManager.observeCurrentVerseIndex()
        GlobalScope.launch(Dispatchers.Main) {
            currentVerseIndexObserver?.filter { it.isValid() }
                    ?.consumeEach {
                        trackReadingProgress()
                        currentVerseIndex = it
                        lastTimestamp = System.currentTimeMillis()
                    }
        }
    }

    private suspend fun trackReadingProgress() {
        try {
            val verseIndex = currentVerseIndex
            if (!verseIndex.isValid()
                    || lastTimestamp == 0L
                    || bibleReadingManager.observeCurrentTranslation().first().isEmpty()) {
                return
            }

            val now = System.currentTimeMillis()
            val timeSpentInMillis = now - lastTimestamp
            if (timeSpentInMillis < timeSpentThresholdInMillis()) {
                return
            }

            readingProgressRepository.trackReadingProgress(
                    verseIndex.bookIndex, verseIndex.chapterIndex, timeSpentInMillis, now)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track reading progress", e)
        }
    }

    @VisibleForTesting
    fun timeSpentThresholdInMillis(): Long = TIME_SPENT_THRESHOLD_IN_MILLIS

    suspend fun stopTracking() {
        trackReadingProgress()
        currentVerseIndex = VerseIndex.INVALID
        lastTimestamp = 0L

        currentVerseIndexObserver?.cancel()
        currentVerseIndexObserver = null
    }

    suspend fun readReadingProgress(): ReadingProgress = readingProgressRepository.readReadingProgress()
}
