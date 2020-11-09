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

package me.xizzhu.android.joshua.core.repository.local

import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex

interface LocalVerseAnnotationStorage<T : VerseAnnotation> {
    suspend fun readSortOrder(): Int

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int)

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<T>

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<T>

    suspend fun read(verseIndex: VerseIndex): T

    suspend fun search(query: String): List<T>

    suspend fun save(verseAnnotation: T)

    suspend fun save(verseAnnotations: List<T>)

    suspend fun remove(verseIndex: VerseIndex)
}
