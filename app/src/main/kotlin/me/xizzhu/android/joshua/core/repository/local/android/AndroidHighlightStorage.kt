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

import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalHighlightStorage
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase

class AndroidHighlightStorage(private val androidDatabase: AndroidDatabase) : LocalHighlightStorage {
    override suspend fun read(bookIndex: Int, chapterIndex: Int): List<Highlight> =
            androidDatabase.highlightDao.read(bookIndex, chapterIndex)

    override suspend fun save(highlight: Highlight) {
        androidDatabase.highlightDao.save(highlight)
    }

    override suspend fun remove(verseIndex: VerseIndex) {
        androidDatabase.highlightDao.remove(verseIndex)
    }
}
