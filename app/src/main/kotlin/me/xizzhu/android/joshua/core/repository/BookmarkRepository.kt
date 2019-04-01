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

import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalBookmarkStorage

class BookmarkRepository(private val localBookmarkStorage: LocalBookmarkStorage) {
    suspend fun read(): List<Bookmark> = localBookmarkStorage.read()

    suspend fun read(verseIndex: VerseIndex): Bookmark = localBookmarkStorage.read(verseIndex)

    suspend fun save(bookmark: Bookmark) {
        localBookmarkStorage.save(bookmark)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        localBookmarkStorage.remove(verseIndex)
    }
}
