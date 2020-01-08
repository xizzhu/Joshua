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

package me.xizzhu.android.joshua.annotated.toolbar

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.infra.arch.Interactor

class AnnotatedVersesToolbarInteractor<V : VerseAnnotation>(private val verseAnnotationManager: VerseAnnotationManager<V>,
                                                            dispatcher: CoroutineDispatcher = Dispatchers.Default) : Interactor(dispatcher) {
    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        verseAnnotationManager.saveSortOrder(sortOrder)
    }

    @Constants.SortOrder
    suspend fun readSortOrder(): Int = verseAnnotationManager.sortOrder().first()
}
