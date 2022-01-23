/*
 * Copyright (C) 2022 Xizhi Zhu
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalVerseAnnotationStorage
import me.xizzhu.android.logger.Log

class VerseAnnotationRepository<T : VerseAnnotation>(
        private val localVerseAnnotationStorage: LocalVerseAnnotationStorage<T>,
        appScope: CoroutineScope
) {
    companion object {
        private val TAG = VerseAnnotationRepository::class.java.simpleName
    }

    private val _sortOrder = MutableStateFlow<Int?>(null)
    val sortOrder: Flow<Int> = _sortOrder.filterNotNull()

    init {
        appScope.launch {
            try {
                _sortOrder.value = localVerseAnnotationStorage.readSortOrder()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize sort order", e)
                _sortOrder.value = Constants.DEFAULT_SORT_ORDER
            }
        }
    }

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        localVerseAnnotationStorage.saveSortOrder(sortOrder)
        _sortOrder.value = sortOrder
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<T> = localVerseAnnotationStorage.read(sortOrder)

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<T> = localVerseAnnotationStorage.read(bookIndex, chapterIndex)

    suspend fun read(verseIndex: VerseIndex): T = localVerseAnnotationStorage.read(verseIndex)

    suspend fun search(query: String): List<T> = localVerseAnnotationStorage.search(query)

    suspend fun save(verseAnnotation: T) {
        localVerseAnnotationStorage.save(verseAnnotation)
    }

    suspend fun save(verseAnnotations: List<T>) {
        localVerseAnnotationStorage.save(verseAnnotations)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        localVerseAnnotationStorage.remove(verseIndex)
    }
}
