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

import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.repository.VerseAnnotationRepository
import me.xizzhu.android.logger.Log

data class Highlight(override val verseIndex: VerseIndex, @ColorInt val color: Int, override val timestamp: Long) : VerseAnnotation(verseIndex, timestamp) {
    companion object {
        const val COLOR_NONE = 0
        const val COLOR_YELLOW = 0xFFFFFF00.toInt()
        const val COLOR_PINK = 0xFFFFC0CB.toInt()
        const val COLOR_PURPLE = 0xFFFF00FF.toInt()
        const val COLOR_GREEN = 0xFF00FF00.toInt()
        const val COLOR_BLUE = 0xFF0000FF.toInt()
        val AVAILABLE_COLORS = arrayOf(COLOR_NONE, COLOR_YELLOW, COLOR_PINK, COLOR_PURPLE, COLOR_GREEN, COLOR_BLUE)

        @IntDef(COLOR_NONE, COLOR_YELLOW, COLOR_PINK, COLOR_PURPLE, COLOR_GREEN, COLOR_BLUE)
        @Retention(AnnotationRetention.SOURCE)
        annotation class AvailableColor
    }

    override fun isValid(): Boolean = super.isValid() && AVAILABLE_COLORS.contains(color)
}

class HighlightManager(private val highlightRepository: VerseAnnotationRepository<Highlight>) {
    companion object {
        private val TAG = HighlightManager::class.java.simpleName
    }

    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val sortOrder: BroadcastChannel<Int> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                sortOrder.offer(highlightRepository.readSortOrder())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize bookmark sort order", e)
                sortOrder.offer(Constants.DEFAULT_SORT_ORDER)
            }
        }
    }

    fun observeSortOrder(): Flow<Int> = sortOrder.asFlow()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        highlightRepository.saveSortOrder(sortOrder)
        this.sortOrder.offer(sortOrder)
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<Highlight> = highlightRepository.read(sortOrder)

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<Highlight> = highlightRepository.read(bookIndex, chapterIndex)

    suspend fun read(verseIndex: VerseIndex): Highlight = highlightRepository.read(verseIndex)

    suspend fun save(highlight: Highlight) {
        highlightRepository.save(highlight)
    }

    suspend fun save(highlights: List<Highlight>) {
        highlightRepository.save(highlights)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        highlightRepository.remove(verseIndex)
    }
}
