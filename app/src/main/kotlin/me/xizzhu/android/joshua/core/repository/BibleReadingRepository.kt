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

package me.xizzhu.android.joshua.core.repository

import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage

class BibleReadingRepository(private val localReadingStorage: LocalReadingStorage) {
    suspend fun readCurrentTranslation(): String = localReadingStorage.readCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        localReadingStorage.saveCurrentTranslation(translationShortName)
    }

    suspend fun readCurrentVerseIndex(): VerseIndex = localReadingStorage.readCurrentVerseIndex()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        localReadingStorage.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun readBookNames(translationShortName: String): List<String> =
            localReadingStorage.readBookNames(translationShortName)

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            localReadingStorage.readVerses(translationShortName, bookIndex, chapterIndex)

    suspend fun search(translationShortName: String, query: String): List<Verse> =
            localReadingStorage.search(translationShortName, query)
}
