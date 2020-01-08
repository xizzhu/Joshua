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

package me.xizzhu.android.joshua.core.repository.local.android.db

import android.database.sqlite.SQLiteOpenHelper
import me.xizzhu.android.ask.db.*
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex

class HighlightDao(sqliteHelper: SQLiteOpenHelper) : VerseAnnotationDao<Highlight>(sqliteHelper, TABLE_HIGHLIGHT) {
    companion object {
        private const val TABLE_HIGHLIGHT = "highlight"
        private const val COLUMN_COLOR = "color"
    }

    override fun MutableMap<String, ColumnModifiers>.putCustomColumnModifiers() {
        put(COLUMN_COLOR, INTEGER + NOT_NULL)
    }

    override fun Map<String, Any?>.toVerseAnnotation(verseIndex: VerseIndex, timestamp: Long): Highlight =
            Highlight(verseIndex, getInt(COLUMN_COLOR), timestamp)

    override fun getCustomColumns(): Array<String> = arrayOf(COLUMN_COLOR)

    override fun defaultVerseAnnotation(verseIndex: VerseIndex): Highlight = Highlight(verseIndex, Highlight.COLOR_NONE, -1L)

    override fun MutableMap<String, Any?>.putCustomColumnValues(verseAnnotation: Highlight) {
        put(COLUMN_COLOR, verseAnnotation.color)
    }
}
