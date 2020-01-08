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
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex

class NoteDao(sqliteHelper: SQLiteOpenHelper) : VerseAnnotationDao<Note>(sqliteHelper, TABLE_NOTE) {
    companion object {
        private const val TABLE_NOTE = "note"
        private const val COLUMN_NOTE = "note"
    }

    override fun MutableMap<String, ColumnModifiers>.putCustomColumnModifiers() {
        put(COLUMN_NOTE, TEXT + NOT_NULL)
    }

    override fun Map<String, Any?>.toVerseAnnotation(verseIndex: VerseIndex, timestamp: Long): Note =
            Note(verseIndex, getString(COLUMN_NOTE), timestamp)

    override fun getCustomColumns(): Array<String> = arrayOf(COLUMN_NOTE)

    override fun defaultVerseAnnotation(verseIndex: VerseIndex): Note = Note(verseIndex, "", -1L)

    override fun MutableMap<String, Any?>.putCustomColumnValues(verseAnnotation: Note) {
        put(COLUMN_NOTE, verseAnnotation.note)
    }
}
