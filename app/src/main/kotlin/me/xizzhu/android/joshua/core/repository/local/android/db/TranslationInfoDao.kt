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
import me.xizzhu.android.joshua.core.TranslationInfo

class TranslationInfoDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_TRANSLATION_INFO = "translationInfo"
        private const val COLUMN_SHORT_NAME = "shortName"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LANGUAGE = "language"
        private const val COLUMN_SIZE = "size"
        private const val COLUMN_DOWNLOADED = "downloaded"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_TRANSLATION_INFO (" +
                    "$COLUMN_SHORT_NAME TEXT PRIMARY KEY, $COLUMN_NAME TEXT NOT NULL, " +
                    " $COLUMN_LANGUAGE TEXT NOT NULL, $COLUMN_SIZE INTEGER NOT NULL, " +
                    " $COLUMN_DOWNLOADED INTEGER NOT NULL);")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun read(): List<TranslationInfo> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_TRANSLATION_INFO, null, null, null, null, null, null, null)
            return with(cursor) {
                val translations = ArrayList<TranslationInfo>(count)
                if (count > 0) {
                    val shortName = getColumnIndex(COLUMN_SHORT_NAME)
                    val name = getColumnIndex(COLUMN_NAME)
                    val language = getColumnIndex(COLUMN_LANGUAGE)
                    val size = getColumnIndex(COLUMN_SIZE)
                    val downloaded = getColumnIndex(COLUMN_DOWNLOADED)
                    while (moveToNext()) {
                        translations.add(TranslationInfo(getString(shortName), getString(name),
                                getString(language), getLong(size), getInt(downloaded) == 1))
                    }
                }
                return@with translations
            }
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun replace(translations: List<TranslationInfo>) {
        db.beginTransaction()
        try {
            db.delete(TABLE_TRANSLATION_INFO, null, null)

            val values = ContentValues(5)
            for (t in translations) {
                t.saveTo(values)
                db.insertWithOnConflict(TABLE_TRANSLATION_INFO, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun TranslationInfo.saveTo(`out`: ContentValues) {
        with(`out`) {
            put(COLUMN_SHORT_NAME, shortName)
            put(COLUMN_NAME, name)
            put(COLUMN_LANGUAGE, language)
            put(COLUMN_SIZE, size)
            put(COLUMN_DOWNLOADED, if (downloaded) 1 else 0)
        }
    }

    @WorkerThread
    fun save(translation: TranslationInfo) {
        val values = ContentValues(5)
        translation.saveTo(values)
        db.insertWithOnConflict(TABLE_TRANSLATION_INFO, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
