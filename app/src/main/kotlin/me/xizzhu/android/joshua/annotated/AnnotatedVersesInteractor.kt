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

package me.xizzhu.android.joshua.annotated

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.toViewData
import me.xizzhu.android.joshua.infra.arch.viewData
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAndLoadingAwareInteractor

class AnnotatedVersesInteractor<V : VerseAnnotation>(private val verseAnnotationManager: VerseAnnotationManager<V>,
                                                     private val bibleReadingManager: BibleReadingManager,
                                                     settingsManager: SettingsManager,
                                                     dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAndLoadingAwareInteractor(settingsManager, dispatcher) {
    fun currentTranslation(): Flow<ViewData<String>> =
            bibleReadingManager.currentTranslation().filter { it.isNotEmpty() }.toViewData()

    suspend fun bookNames(currentTranslation: String): ViewData<List<String>> =
            viewData { bibleReadingManager.readBookNames(currentTranslation) }

    suspend fun bookShortNames(currentTranslation: String): ViewData<List<String>> =
            viewData { bibleReadingManager.readBookShortNames(currentTranslation) }

    suspend fun verses(currentTranslation: String, verseIndexes: List<VerseIndex>): ViewData<Map<VerseIndex, Verse>> =
            viewData { bibleReadingManager.readVerses(currentTranslation, verseIndexes) }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    fun sortOrder(): Flow<ViewData<Int>> = verseAnnotationManager.sortOrder().toViewData()

    suspend fun verseAnnotations(@Constants.SortOrder sortOrder: Int): ViewData<List<V>> =
            viewData { verseAnnotationManager.read(sortOrder) }
}
