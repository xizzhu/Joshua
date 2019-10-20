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

package me.xizzhu.android.joshua.annotated.highlights.list

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesInteractor
import me.xizzhu.android.joshua.core.*

class HighlightsListInteractor(private val highlightManager: HighlightManager,
                               bibleReadingManager: BibleReadingManager,
                               settingsManager: SettingsManager,
                               dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseAnnotatedVersesInteractor<Highlight>(bibleReadingManager, settingsManager, dispatcher) {
    override fun sortOrder(): Flow<Int> = highlightManager.observeSortOrder()

    override suspend fun readVerseAnnotations(@Constants.SortOrder sortOrder: Int): List<Highlight> = highlightManager.read(sortOrder)
}
