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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import me.xizzhu.android.ask.db.transaction

class AndroidDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "DATABASE_JOSHUA"
        const val DATABASE_VERSION = 2
    }

    val bookmarkDao = BookmarkDao(this)
    val bookNamesDao = BookNamesDao(this)
    val highlightDao = HighlightDao(this)
    val metadataDao = MetadataDao(this)
    val noteDao = NoteDao(this)
    val readingProgressDao = ReadingProgressDao(this)
    val translationDao = TranslationDao(this)
    val translationInfoDao = TranslationInfoDao(this)

    override fun onCreate(db: SQLiteDatabase) {
        db.transaction {
            bookmarkDao.createTable(db)
            bookNamesDao.createTable(db)
            highlightDao.createTable(db)
            MetadataDao.createTable(db)
            NoteDao.createTable(db)
            ReadingProgressDao.createTable(db)
            TranslationInfoDao.createTable(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            1 -> highlightDao.createTable(db)
        }
    }
}
