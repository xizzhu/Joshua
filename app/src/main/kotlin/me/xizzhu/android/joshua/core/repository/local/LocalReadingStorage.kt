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

package me.xizzhu.android.joshua.core.repository.local

import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex

interface LocalReadingStorage {
    suspend fun readCurrentVerseIndex(): VerseIndex

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex)

    suspend fun readCurrentTranslation(): String

    suspend fun saveCurrentTranslation(translationShortName: String)

    suspend fun readBookNames(translationShortName: String): List<String>

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int, bookName: String): List<Verse>

    suspend fun search(translationShortName: String, bookNames: List<String>, query: String): List<Verse>
}
