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

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository

data class ReadingProgress(val continuousReadingDays: Int, val lastReadingTimestamp: Long,
                           val chapterReadingStatus: List<ChapterReadingStatus>) {
    data class ChapterReadingStatus(val bookIndex: Int, val chapterIndex: Int,
                                    val readCount: Int, val lastReadingTimestamp: Long)
}

class ReadingProgressManager(private val bibleReadingManager: BibleReadingManager,
                             private val readingProgressRepository: ReadingProgressRepository) {
    private var currentVerseIndexObserver: ReceiveChannel<VerseIndex>? = null

    suspend fun startTracking() {
        if (currentVerseIndexObserver != null) {
            return
        }

        currentVerseIndexObserver = bibleReadingManager.observeCurrentVerseIndex()
        currentVerseIndexObserver!!.filter { it.isValid() }
                .consumeEach {
                    readingProgressRepository.trackReadingProgress(
                            it.bookIndex, it.chapterIndex, System.currentTimeMillis())
                }
    }

    fun stopTracking() {
        currentVerseIndexObserver?.cancel()
        currentVerseIndexObserver = null
    }
}
