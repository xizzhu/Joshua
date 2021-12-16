/*
 * Copyright (C) 2021 Xizhi Zhu
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
import androidx.appcompat.app.AppCompatDelegate
import me.xizzhu.android.ask.db.Condition
import me.xizzhu.android.ask.db.ConditionBuilder.and
import me.xizzhu.android.ask.db.ConditionBuilder.like
import me.xizzhu.android.ask.db.delete
import me.xizzhu.android.ask.db.firstOrDefault
import me.xizzhu.android.ask.db.getString
import me.xizzhu.android.ask.db.insert
import me.xizzhu.android.ask.db.select
import me.xizzhu.android.ask.db.transaction
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.toKeywords

class AndroidDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "DATABASE_JOSHUA"
        const val DATABASE_VERSION = 5
    }

    val bookmarkDao = BookmarkDao(this)
    val bookNamesDao = BookNamesDao(this)
    val highlightDao = HighlightDao(this)
    val metadataDao = MetadataDao(this)
    val noteDao = NoteDao(this)
    val readingProgressDao = ReadingProgressDao(this)
    val strongNumberIndexDao = StrongNumberIndexDao(this)
    val strongNumberReverseIndexDao = StrongNumberReverseIndexDao(this)
    val strongNumberWordDao = StrongNumberWordDao(this)
    val translationDao = TranslationDao(this)
    val translationInfoDao = TranslationInfoDao(this)

    override fun onCreate(db: SQLiteDatabase) {
        db.transaction {
            bookmarkDao.createTable(db)
            bookNamesDao.createTable(db)
            highlightDao.createTable(db)
            metadataDao.createTable(db)
            noteDao.createTable(db)
            readingProgressDao.createTable(db)
            strongNumberIndexDao.createTable(db)
            strongNumberReverseIndexDao.createTable(db)
            strongNumberWordDao.createTable(db)
            translationInfoDao.createTable(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 1) {
            highlightDao.createTable(db)
        }
        if (oldVersion <= 2) {
            strongNumberIndexDao.createTable(db)
            strongNumberReverseIndexDao.createTable(db)
            strongNumberWordDao.createTable(db)
        }
        if (oldVersion <= 3) {
            val oldFontSizeScale = db.select("metadata", "value") { "key" eq "fontSizeScale" }
                    .firstOrDefault("1.0") { it.getString("value") }.toFloat()
            val newFontSizeScale = oldFontSizeScale / 2.0F
            db.insert("metadata", SQLiteDatabase.CONFLICT_REPLACE) {
                it["key"] = "fontSizeScale"
                it["value"] = newFontSizeScale.toString()
            }
        }
        if (oldVersion <= 4) {
            val nightModeOn = db.select("metadata", "value") { "key" eq "nightModeOn" }
                    .firstOrDefault("false") { it.getString("value") }.toBoolean()
            AppCompatDelegate.setDefaultNightMode(if (nightModeOn) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            db.delete("metadata") { "key" eq "nightModeOn" }

            db.insert("metadata", SQLiteDatabase.CONFLICT_REPLACE) {
                it["key"] = "nightMode"
                it["value"] = if (nightModeOn) Settings.NIGHT_MODE_ON.toString() else Settings.NIGHT_MODE_OFF.toString()
            }
        }
    }

    fun removeAll() {
        writableDatabase.transaction {
            bookmarkDao.removeAll()
            bookNamesDao.removeAll()
            highlightDao.removeAll()
            metadataDao.removeAll()
            noteDao.removeAll()
            readingProgressDao.removeAll()
            strongNumberIndexDao.removeAll()
            strongNumberReverseIndexDao.removeAll()
            strongNumberWordDao.removeAll()
            translationInfoDao.removeAll()
        }
    }
}

fun Condition.withQuery(column: String, query: String): Condition {
    var condition: Condition = this
    query.toKeywords().forEach { keyword ->
        val like = column like "%%$keyword%%"
        condition = if (condition == Condition.NoOp) like else condition and like
    }
    return condition
}
