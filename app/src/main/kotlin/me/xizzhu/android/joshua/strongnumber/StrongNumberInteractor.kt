/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.strongnumber

import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseInteractor
import me.xizzhu.android.joshua.utils.firstNotEmpty

class StrongNumberInteractor(
        private val bibleReadingManager: BibleReadingManager,
        private val strongNumberManager: StrongNumberManager,
        settingsManager: SettingsManager
) : BaseInteractor(settingsManager) {
    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun bookNames(): List<String> = bibleReadingManager.readBookNames(currentTranslation())

    private suspend fun currentTranslation(): String = bibleReadingManager.currentTranslation().firstNotEmpty()

    suspend fun bookShortNames(): List<String> = bibleReadingManager.readBookShortNames(currentTranslation())

    suspend fun verses(sn: String): List<Verse> =
            bibleReadingManager.readVerses(currentTranslation(), strongNumberManager.readVerseIndexes(sn)).values
                    .sortedBy { with(it.verseIndex) { bookIndex * 100000 + chapterIndex * 1000 + verseIndex } }

    suspend fun strongNumber(sn: String): StrongNumber = strongNumberManager.readStrongNumber(sn)
}
