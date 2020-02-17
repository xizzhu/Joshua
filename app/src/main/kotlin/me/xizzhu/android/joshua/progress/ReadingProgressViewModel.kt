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

package me.xizzhu.android.joshua.progress

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.flowOf

data class ReadingProgressViewData(val readingProgress: ReadingProgress, val bookNames: List<String>)

class ReadingProgressViewModel(private val bibleReadingManager: BibleReadingManager,
                               private val readingProgressManager: ReadingProgressManager,
                               settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    fun readingProgress(): Flow<ViewData<ReadingProgressViewData>> = flowOf {
        ReadingProgressViewData(
                readingProgressManager.read(),
                bibleReadingManager.readBookNames(
                        bibleReadingManager.currentTranslation().first { it.isNotEmpty() }
                )
        )
    }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) = bibleReadingManager.saveCurrentVerseIndex(verseIndex)
}
