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

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread

class MetadataDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_METADATA = "metadata"
        private const val COLUMN_KEY = "key"
        private const val COLUMN_VALUE = "value"

        const val KEY_CURRENT_TRANSLATION = "currentTranslation"
        const val KEY_CURRENT_BOOK_INDEX = "currentBookIndex"
        const val KEY_CURRENT_CHAPTER_INDEX = "currentChapterIndex"
        const val KEY_CURRENT_VERSE_INDEX = "currentVerseIndex"
        const val KEY_CONTINUOUS_READING_DAYS = "continuousReadingDays"
        const val KEY_LAST_READING_TIMESTAMP = "lastReadingTimestamp"
        const val KEY_SCREEN_ON = "screenOn"
        const val KEY_NIGHT_MODE_ON = "nightModeOn"
        const val KEY_SIMPLE_READING_MODE_ON = "simpleReadingModeOn"
        const val KEY_FONT_SIZE_SCALE = "fontSizeScale"
        const val KEY_TRANSLATION_LIST_REFRESH_TIMESTAMP = "translationListRefreshTimestamp"
        const val KEY_BOOKMARKS_SORT_ORDER = "bookmarksSortOrder"
        const val KEY_NOTES_SORT_ORDER = "notesSortOrder"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_METADATA (" +
                    "$COLUMN_KEY TEXT PRIMARY KEY, $COLUMN_VALUE TEXT NOT NULL);")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun read(key: String, defaultValue: String): String {
        db.query(TABLE_METADATA, arrayOf(COLUMN_VALUE), "$COLUMN_KEY = ?", arrayOf(key), null, null, null)
                .use {
                    return if (it.count > 0 && it.moveToNext()) {
                        it.getString(0)
                    } else {
                        defaultValue
                    }
                }
    }

    @WorkerThread
    fun read(keys: List<Pair<String, String>>): Map<String, String> {
        if (keys.isEmpty()) {
            return emptyMap()
        }

        val results = HashMap<String, String>(keys.size)

        val selection = StringBuilder()
        val selectionArgs = Array(keys.size) { "" }
        for ((i, key) in keys.withIndex()) {
            if (i > 0) {
                selection.append(" OR ")
            }
            selection.append("$COLUMN_KEY = ?")
            selectionArgs[i] = key.first

            results[key.first] = key.second
        }

        db.query(TABLE_METADATA, arrayOf(COLUMN_KEY, COLUMN_VALUE),
                selection.toString(), selectionArgs, null, null, null).use {
            if (it.count > 0) {
                val keyIndex = it.getColumnIndex(COLUMN_KEY)
                val valueIndex = it.getColumnIndex(COLUMN_VALUE)
                while (it.moveToNext()) {
                    results[it.getString(keyIndex)] = it.getString(valueIndex)
                }
            }
        }
        return results
    }

    @WorkerThread
    fun save(key: String, value: String) {
        val values = ContentValues(2)
        with(values) {
            put(COLUMN_KEY, key)
            put(COLUMN_VALUE, value)
        }
        db.insertWithOnConflict(TABLE_METADATA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @WorkerThread
    fun save(entries: List<Pair<String, String>>) {
        db.transaction {
            val values = ContentValues(2)
            for (entry in entries) {
                with(values) {
                    put(COLUMN_KEY, entry.first)
                    put(COLUMN_VALUE, entry.second)
                }
                insertWithOnConflict(TABLE_METADATA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }
}
