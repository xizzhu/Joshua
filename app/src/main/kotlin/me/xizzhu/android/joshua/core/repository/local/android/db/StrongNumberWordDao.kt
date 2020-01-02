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

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.ask.db.*
import me.xizzhu.android.joshua.core.StrongNumber

class StrongNumberWordDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_STRONG_NUMBER_WORD = "sn_en"
        private const val COLUMN_STRONG_NUMBER = "strongNumber"
        private const val COLUMN_MEANING = "meaning"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(db: SQLiteDatabase) {
        db.createTable(TABLE_STRONG_NUMBER_WORD) {
            it[COLUMN_STRONG_NUMBER] = TEXT + PRIMARY_KEY + NOT_NULL
            it[COLUMN_MEANING] = TEXT + NOT_NULL
        }
    }

    @WorkerThread
    fun read(strongNumbers: List<String>): List<StrongNumber> =
            db.select(TABLE_STRONG_NUMBER_WORD) {
                var condition: Condition = noOp()
                strongNumbers.forEach { strongNumber ->
                    (COLUMN_STRONG_NUMBER eq strongNumber).run {
                        condition = if (condition == Condition.NoOp) this else condition or this
                    }
                }
                condition
            }.toList { row -> StrongNumber(row.getString(COLUMN_STRONG_NUMBER), row.getString(COLUMN_MEANING)) }

    @WorkerThread
    fun replace(strongNumberWords: Map<String, String>) {
        db.transaction {
            deleteAll(TABLE_STRONG_NUMBER_WORD)

            strongNumberWords.forEach { (sn, meaning) ->
                insert(TABLE_STRONG_NUMBER_WORD, SQLiteDatabase.CONFLICT_REPLACE) {
                    it[COLUMN_STRONG_NUMBER] = sn
                    it[COLUMN_MEANING] = meaning
                }
            }
        }
    }
}
