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

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.ask.db.*

class MetadataDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_METADATA = "metadata"
        private const val COLUMN_KEY = "key"
        private const val COLUMN_VALUE = "value"

        const val KEY_CURRENT_TRANSLATION = "currentTranslation"
        const val KEY_PARALLEL_TRANSLATIONS = "parallelTranslations"
        const val KEY_CURRENT_BOOK_INDEX = "currentBookIndex"
        const val KEY_CURRENT_CHAPTER_INDEX = "currentChapterIndex"
        const val KEY_CURRENT_VERSE_INDEX = "currentVerseIndex"
        const val KEY_CONTINUOUS_READING_DAYS = "continuousReadingDays"
        const val KEY_LAST_READING_TIMESTAMP = "lastReadingTimestamp"
        const val KEY_SCREEN_ON = "screenOn"
        const val KEY_NIGHT_MODE_ON = "nightModeOn"
        const val KEY_SIMPLE_READING_MODE_ON = "simpleReadingModeOn"
        const val KEY_HIDE_SEARCH_BUTTON = "hideSearchButton"
        const val KEY_CONSOLIDATE_VERSES_FOR_SHARING = "consolidateVersesForSharing"
        const val KEY_FONT_SIZE_SCALE = "fontSizeScale"
        const val KEY_TRANSLATION_LIST_REFRESH_TIMESTAMP = "translationListRefreshTimestamp"
        const val KEY_BOOKMARKS_SORT_ORDER = "bookmarksSortOrder"
        const val KEY_HIGHLIGHTS_SORT_ORDER = "highlightsSortOrder"
        const val KEY_NOTES_SORT_ORDER = "notesSortOrder"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_METADATA) {
            it[COLUMN_KEY] = TEXT + PRIMARY_KEY
            it[COLUMN_VALUE] = TEXT + NOT_NULL
        }
    }

    @WorkerThread
    fun read(key: String, defaultValue: String): String =
            db.select(TABLE_METADATA, COLUMN_VALUE) { COLUMN_KEY eq key }
                    .firstOrDefault(defaultValue) { it.getString(COLUMN_VALUE) }

    @WorkerThread
    fun read(keys: List<Pair<String, String>>): Map<String, String> = HashMap<String, String>(keys.size).apply {
        if (keys.isEmpty()) return@apply

        db.select(TABLE_METADATA, COLUMN_KEY, COLUMN_VALUE) {
            var condition: Condition = noOp()
            keys.forEach { (key, defaultValue) ->
                set(key, defaultValue)

                condition = if (condition == Condition.NoOp) COLUMN_KEY eq key else condition or (COLUMN_KEY eq key)
            }
            condition
        }.forEach { row -> set(row.getString(COLUMN_KEY), row.getString(COLUMN_VALUE)) }
    }

    @WorkerThread
    fun save(key: String, value: String) {
        db.insert(TABLE_METADATA, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_KEY] = key
            it[COLUMN_VALUE] = value
        }
    }

    @WorkerThread
    fun save(entries: List<Pair<String, String>>) {
        db.transaction { entries.forEach { save(it.first, it.second) } }
    }

    @WorkerThread
    fun removeAll() {
        db.deleteAll(TABLE_METADATA)
    }
}
