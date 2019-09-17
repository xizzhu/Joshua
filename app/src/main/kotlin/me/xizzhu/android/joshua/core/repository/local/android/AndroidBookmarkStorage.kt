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

package me.xizzhu.android.joshua.core.repository.local.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalBookmarkStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.db.MetadataDao

class AndroidBookmarkStorage(private val androidDatabase: AndroidDatabase) : LocalBookmarkStorage {
    override suspend fun readSortOrder(): Int =
            withContext(Dispatchers.IO) { androidDatabase.metadataDao.read(MetadataDao.KEY_BOOKMARKS_SORT_ORDER, "0").toInt() }

    override suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        withContext(Dispatchers.IO) {
            androidDatabase.metadataDao.save(MetadataDao.KEY_BOOKMARKS_SORT_ORDER, sortOrder.toString())
        }
    }

    override suspend fun read(@Constants.SortOrder sortOrder: Int): List<Bookmark> =
            withContext(Dispatchers.IO) { androidDatabase.bookmarkDao.read(sortOrder) }

    override suspend fun read(bookIndex: Int, chapterIndex: Int): List<Bookmark> =
            withContext(Dispatchers.IO) { androidDatabase.bookmarkDao.read(bookIndex, chapterIndex) }

    override suspend fun read(verseIndex: VerseIndex): Bookmark =
            withContext(Dispatchers.IO) { androidDatabase.bookmarkDao.read(verseIndex) }

    override suspend fun save(bookmark: Bookmark) {
        withContext(Dispatchers.IO) { androidDatabase.bookmarkDao.save(bookmark) }
    }

    override suspend fun save(bookmarks: List<Bookmark>) {
        withContext(Dispatchers.IO) { androidDatabase.bookmarkDao.save(bookmarks) }
    }

    override suspend fun remove(verseIndex: VerseIndex) {
        withContext(Dispatchers.IO) { androidDatabase.bookmarkDao.remove(verseIndex) }
    }
}
