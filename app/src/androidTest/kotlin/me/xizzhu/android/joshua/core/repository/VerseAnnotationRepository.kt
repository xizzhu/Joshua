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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.xizzhu.android.joshua.core.*
import java.lang.UnsupportedOperationException

open class VerseAnnotationRepository<T : VerseAnnotation> {
    val sortOrder: BroadcastChannel<Int> = ConflatedBroadcastChannel()
    var annotations: List<T> = emptyList()

    init {
        reset()
    }

    open fun reset() {
        sortOrder.offer(Constants.DEFAULT_SORT_ORDER)
        annotations = emptyList()
    }

    fun sortOrder(): Flow<Int> = sortOrder.asFlow()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        this.sortOrder.offer(sortOrder)
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<T> = annotations

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<T> = annotations

    suspend fun read(verseIndex: VerseIndex): T {
        throw UnsupportedOperationException()
    }

    suspend fun save(verseAnnotation: T) {}

    suspend fun save(verseAnnotations: List<T>) {}

    suspend fun remove(verseIndex: VerseIndex) {}
}

val BookmarksRepository: VerseAnnotationRepository<Bookmark> = VerseAnnotationRepository()

val HighlightsRepository: VerseAnnotationRepository<Highlight> = VerseAnnotationRepository()
