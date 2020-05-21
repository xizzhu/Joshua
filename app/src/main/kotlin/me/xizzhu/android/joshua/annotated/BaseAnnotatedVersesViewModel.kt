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

package me.xizzhu.android.joshua.annotated

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel

data class AnnotatedVerses<V : VerseAnnotation>(
        @Constants.SortOrder val sortOrder: Int, val verses: List<Pair<V, Verse>>,
        val bookNames: List<String>, val bookShortNames: List<String>
)

data class LoadingRequest(val currentTranslation: String, @Constants.SortOrder val sortOrder: Int)

abstract class BaseAnnotatedVersesViewModel<V : VerseAnnotation>(private val bibleReadingManager: BibleReadingManager,
                                                                 private val verseAnnotationManager: VerseAnnotationManager<V>,
                                                                 settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    fun sortOrder(): Flow<Int> = verseAnnotationManager.sortOrder()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        verseAnnotationManager.saveSortOrder(sortOrder)
    }

    fun loadingRequest(): Flow<LoadingRequest> =
            combine(bibleReadingManager.currentTranslation().filter { it.isNotEmpty() },
                    verseAnnotationManager.sortOrder()) { currentTranslation, sortOrder ->
                LoadingRequest(currentTranslation, sortOrder)
            }

    fun annotatedVerses(loadingRequest: LoadingRequest): Flow<AnnotatedVerses<V>> = flow {
        val annotations = verseAnnotationManager.read(loadingRequest.sortOrder)
        val verses = bibleReadingManager.readVerses(loadingRequest.currentTranslation, annotations.map { it.verseIndex })
        emit(AnnotatedVerses(
                loadingRequest.sortOrder,
                annotations.mapNotNull { annotation -> verses[annotation.verseIndex]?.let { Pair(annotation, it) } },
                bibleReadingManager.readBookNames(loadingRequest.currentTranslation),
                bibleReadingManager.readBookShortNames(loadingRequest.currentTranslation)
        ))
    }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}
