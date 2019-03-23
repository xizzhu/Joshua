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
import android.database.Cursor
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

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_METADATA (" +
                    "$COLUMN_KEY TEXT PRIMARY KEY, $COLUMN_VALUE TEXT NOT NULL);")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun read(key: String, defaultValue: String): String {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_METADATA, arrayOf(COLUMN_VALUE),
                    "$COLUMN_KEY = ?", arrayOf(key), null, null, null)
            return if (cursor.count > 0 && cursor.moveToNext()) {
                cursor.getString(0)
            } else {
                defaultValue
            }
        } finally {
            cursor?.close()
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

        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_METADATA, arrayOf(COLUMN_KEY, COLUMN_VALUE),
                    selection.toString(), selectionArgs, null, null, null)
            if (cursor.count > 0) {
                val keyIndex = cursor.getColumnIndex(COLUMN_KEY)
                val valueIndex = cursor.getColumnIndex(COLUMN_VALUE)
                while (cursor.moveToNext()) {
                    results[cursor.getString(keyIndex)] = cursor.getString(valueIndex)
                }
            }
            return results
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun save(key: String, value: String) {
        val values = ContentValues(2)
        values.put(COLUMN_KEY, key)
        values.put(COLUMN_VALUE, value)
        db.insertWithOnConflict(TABLE_METADATA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @WorkerThread
    fun save(entries: List<Pair<String, String>>) {
        db.beginTransaction()
        try {
            val values = ContentValues(2)
            for (entry in entries) {
                values.put(COLUMN_KEY, entry.first)
                values.put(COLUMN_VALUE, entry.second)
                db.insertWithOnConflict(TABLE_METADATA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
