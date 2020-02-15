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
import kotlinx.coroutines.flow.filter
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.toViewData

abstract class BaseAnnotatedVersesViewModel<V : VerseAnnotation>(private val bibleReadingManager: BibleReadingManager,
                                                                 private val verseAnnotationManager: VerseAnnotationManager<V>,
                                                                 settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    fun sortOrder(): Flow<ViewData<Int>> = verseAnnotationManager.sortOrder().toViewData()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        verseAnnotationManager.saveSortOrder(sortOrder)
    }

    fun currentTranslation(): Flow<ViewData<String>> =
            bibleReadingManager.currentTranslation().filter { it.isNotEmpty() }.toViewData()

    suspend fun bookNames(currentTranslation: String): List<String> =
            bibleReadingManager.readBookNames(currentTranslation)

    suspend fun bookShortNames(currentTranslation: String): List<String> =
            bibleReadingManager.readBookShortNames(currentTranslation)

    suspend fun verses(currentTranslation: String, verseIndexes: List<VerseIndex>): Map<VerseIndex, Verse> =
            bibleReadingManager.readVerses(currentTranslation, verseIndexes)

    suspend fun verseAnnotations(@Constants.SortOrder sortOrder: Int): List<V> =
            verseAnnotationManager.read(sortOrder)

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}
