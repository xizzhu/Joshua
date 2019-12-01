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

package me.xizzhu.android.joshua.core.repository.local.android.db

import android.database.sqlite.SQLiteOpenHelper
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.VerseIndex

class BookmarkDao(sqliteHelper: SQLiteOpenHelper) : VerseAnnotationDao<Bookmark>(sqliteHelper, TABLE_BOOKMARK) {
    companion object {
        private const val TABLE_BOOKMARK = "bookmark"
    }

    override fun Map<String, Any?>.toVerseAnnotation(verseIndex: VerseIndex, timestamp: Long): Bookmark =
            Bookmark(verseIndex, timestamp)

    override fun defaultVerseAnnotation(verseIndex: VerseIndex): Bookmark = Bookmark(verseIndex, -1L)
}
