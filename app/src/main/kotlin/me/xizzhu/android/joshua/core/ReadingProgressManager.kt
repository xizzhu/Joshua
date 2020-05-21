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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.joshua.utils.elapsedRealtime
import me.xizzhu.android.joshua.utils.filterIsValid
import me.xizzhu.android.logger.Log

data class ReadingProgress(val continuousReadingDays: Int, val lastReadingTimestamp: Long,
                           val chapterReadingStatus: List<ChapterReadingStatus>) {
    fun isValid(): Boolean {
        if (continuousReadingDays < 0 || lastReadingTimestamp < 0L) return false
        chapterReadingStatus.forEach { if (!it.isValid()) return false }
        return true
    }

    data class ChapterReadingStatus(val bookIndex: Int, val chapterIndex: Int, val readCount: Int,
                                    val timeSpentInMillis: Long, val lastReadingTimestamp: Long) {
        fun isValid(): Boolean {
            if (bookIndex < 0 || bookIndex >= Bible.BOOK_COUNT) return false
            if (chapterIndex < 0 || chapterIndex >= Bible.getChapterCount(bookIndex)) return false
            if (readCount < 0 || timeSpentInMillis < 0L || lastReadingTimestamp < 0L) return false
            return true
        }
    }
}

class ReadingProgressManager(private val bibleReadingRepository: BibleReadingRepository,
                             private val readingProgressRepository: ReadingProgressRepository) {
    companion object {
        private val TAG: String = ReadingProgressManager::class.java.simpleName

        @VisibleForTesting
        const val TIME_SPENT_THRESHOLD_IN_MILLIS = 2500L
    }

    private var currentVerseIndexObserver: Flow<VerseIndex>? = null

    private var currentVerseIndex: VerseIndex = VerseIndex.INVALID
    private var lastTimestamp: Long = 0L

    fun startTracking() {
        if (currentVerseIndexObserver != null) {
            return
        }

        lastTimestamp = elapsedRealtime()
        currentVerseIndexObserver = bibleReadingRepository.currentVerseIndex()
        GlobalScope.launch(Dispatchers.Main) {
            currentVerseIndexObserver?.filterIsValid()
                    ?.collect {
                        trackReadingProgress()
                        currentVerseIndex = it
                        lastTimestamp = elapsedRealtime()
                    }
        }
    }

    private suspend fun trackReadingProgress() {
        try {
            val verseIndex = currentVerseIndex
            if (!verseIndex.isValid()
                    || lastTimestamp == 0L
                    || bibleReadingRepository.currentTranslation().first().isEmpty()) {
                return
            }

            val now = elapsedRealtime()
            val timeSpentInMillis = now - lastTimestamp
            if (timeSpentInMillis < TIME_SPENT_THRESHOLD_IN_MILLIS) {
                return
            }

            readingProgressRepository.trackReadingProgress(
                    verseIndex.bookIndex, verseIndex.chapterIndex, timeSpentInMillis, now)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track reading progress", e)
        }
    }

    suspend fun stopTracking() {
        trackReadingProgress()
        currentVerseIndex = VerseIndex.INVALID
        lastTimestamp = 0L
        currentVerseIndexObserver = null
    }

    suspend fun read(): ReadingProgress = readingProgressRepository.read()
}
