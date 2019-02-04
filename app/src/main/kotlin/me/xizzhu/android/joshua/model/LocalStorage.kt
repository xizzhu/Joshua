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

package me.xizzhu.android.joshua.model

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.reactivex.Completable
import io.reactivex.Single
import me.xizzhu.android.joshua.App
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorage @Inject constructor(app: App) : SQLiteOpenHelper(app, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "DATABASE_JOSHUA"
        const val DATABASE_VERSION = 1
    }

    val metadataDao by lazy { MetadataDao(writableDatabase) }
    val translationInfoDao by lazy { TranslationInfoDao(writableDatabase) }

    override fun onCreate(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            MetadataDao.createTable(db)
            TranslationInfoDao.createTable(db)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // do nothing
    }
}

class TranslationInfoDao(private val db: SQLiteDatabase) {
    companion object {
        private const val TABLE_TRANSLATION_INFO = "translation_info"
        private const val COLUMN_SHORT_NAME = "shortName"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LANGUAGE = "language"
        private const val COLUMN_SIZE = "size"
        private const val COLUMN_DOWNLOADED = "downloaded"

        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_TRANSLATION_INFO (" +
                    "$COLUMN_SHORT_NAME TEXT PRIMARY KEY, $COLUMN_NAME TEXT NOT NULL," +
                    " $COLUMN_LANGUAGE TEXT NOT NULL, $COLUMN_SIZE INTEGER NOT NULL," +
                    " $COLUMN_DOWNLOADED INTEGER NOT NULL);")
        }
    }

    fun load(): Single<List<TranslationInfo>> =
            Single.fromCallable {
                var cursor: Cursor? = null
                try {
                    cursor = db.query(TABLE_TRANSLATION_INFO, null, null, null, null, null, null, null)
                    val count = cursor.count
                    if (count == 0) {
                        emptyList<TranslationInfo>()
                    } else {
                        val shortName = cursor.getColumnIndex(COLUMN_SHORT_NAME)
                        val name = cursor.getColumnIndex(COLUMN_NAME)
                        val language = cursor.getColumnIndex(COLUMN_LANGUAGE)
                        val size = cursor.getColumnIndex(COLUMN_SIZE)
                        val downloaded = cursor.getColumnIndex(COLUMN_DOWNLOADED)
                        val translations = ArrayList<TranslationInfo>(count)
                        while (cursor.moveToNext()) {
                            translations.add(TranslationInfo(cursor.getString(shortName),
                                    cursor.getString(name), cursor.getString(language),
                                    cursor.getLong(size), cursor.getInt(downloaded) == 1))
                        }
                        translations
                    }
                } finally {
                    cursor?.close()
                }
            }

    fun save(translations: List<TranslationInfo>) {
        db.beginTransaction()
        try {
            val values = ContentValues(5)
            for (t in translations) {
                values.put(COLUMN_SHORT_NAME, t.shortName)
                values.put(COLUMN_NAME, t.name)
                values.put(COLUMN_LANGUAGE, t.language)
                values.put(COLUMN_SIZE, t.size)
                values.put(COLUMN_DOWNLOADED, if (t.downloaded) 1 else 0)
                db.insertWithOnConflict(TABLE_TRANSLATION_INFO, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}

class MetadataDao(private val db: SQLiteDatabase) {
    companion object {
        private const val TABLE_METADATA = "metadata"
        private const val COLUMN_KEY = "key"
        private const val COLUMN_VALUE = "value"

        const val KEY_LAST_TRANSLATION = "last_translation"

        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_METADATA (" +
                    "$COLUMN_KEY TEXT PRIMARY KEY, $COLUMN_VALUE TEXT NOT NULL);")
        }
    }

    fun load(key: String, defaultValue: String): Single<String> =
            Single.fromCallable {
                var cursor: Cursor? = null
                try {
                    cursor = db.query(TABLE_METADATA, arrayOf(COLUMN_VALUE),
                            "$COLUMN_KEY = ?", arrayOf(key), null, null, null)
                    if (cursor.count > 0 && cursor.moveToNext()) {
                        cursor.getString(0)
                    } else {
                        defaultValue
                    }
                } finally {
                    cursor?.close()
                }
            }

    fun save(key: String, value: String): Completable =
            Completable.fromAction {
                val values = ContentValues(2)
                values.put(COLUMN_KEY, key)
                values.put(COLUMN_VALUE, value)
                db.insertWithOnConflict(TABLE_METADATA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
}
