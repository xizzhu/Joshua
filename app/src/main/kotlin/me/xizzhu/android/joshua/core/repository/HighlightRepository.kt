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

import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalHighlightStorage

class HighlightRepository(private val localHighlightStorage: LocalHighlightStorage) {
    suspend fun readSortOrder(): Int = localHighlightStorage.readSortOrder()

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        localHighlightStorage.saveSortOrder(sortOrder)
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<Highlight> =
            localHighlightStorage.read(sortOrder)

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<Highlight> =
            localHighlightStorage.read(bookIndex, chapterIndex)

    suspend fun read(verseIndex: VerseIndex): Highlight = localHighlightStorage.read(verseIndex)

    suspend fun save(highlight: Highlight) {
        localHighlightStorage.save(highlight)
    }

    suspend fun save(highlights: List<Highlight>) {
        localHighlightStorage.save(highlights)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        localHighlightStorage.remove(verseIndex)
    }
}
