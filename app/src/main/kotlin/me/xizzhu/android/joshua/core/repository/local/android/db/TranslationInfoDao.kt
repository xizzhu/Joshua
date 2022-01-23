/*
 * Copyright (C) 2022 Xizhi Zhu
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
import me.xizzhu.android.joshua.core.TranslationInfo

class TranslationInfoDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_TRANSLATION_INFO = "translationInfo"
        private const val COLUMN_SHORT_NAME = "shortName"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LANGUAGE = "language"
        private const val COLUMN_SIZE = "size"
        private const val COLUMN_DOWNLOADED = "downloaded"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_TRANSLATION_INFO) {
            it[COLUMN_SHORT_NAME] = TEXT + PRIMARY_KEY
            it[COLUMN_NAME] = TEXT + NOT_NULL
            it[COLUMN_LANGUAGE] = TEXT + NOT_NULL
            it[COLUMN_SIZE] = INTEGER + NOT_NULL
            it[COLUMN_DOWNLOADED] = INTEGER + NOT_NULL
        }
    }

    @WorkerThread
    fun read(): List<TranslationInfo> = db.select(TABLE_TRANSLATION_INFO)
            .toList { row ->
                TranslationInfo(row.getString(COLUMN_SHORT_NAME), row.getString(COLUMN_NAME),
                        row.getString(COLUMN_LANGUAGE), row.getLong(COLUMN_SIZE), row.getInt(COLUMN_DOWNLOADED) == 1)
            }

    @WorkerThread
    fun replace(translations: List<TranslationInfo>) {
        db.transaction {
            deleteAll(TABLE_TRANSLATION_INFO)
            translations.forEach { save(it) }
        }
    }

    @WorkerThread
    fun save(translation: TranslationInfo) {
        db.insert(TABLE_TRANSLATION_INFO, SQLiteDatabase.CONFLICT_REPLACE) {
            it[COLUMN_SHORT_NAME] = translation.shortName
            it[COLUMN_NAME] = translation.name
            it[COLUMN_LANGUAGE] = translation.language
            it[COLUMN_SIZE] = translation.size
            it[COLUMN_DOWNLOADED] = if (translation.downloaded) 1 else 0
        }
    }
}
