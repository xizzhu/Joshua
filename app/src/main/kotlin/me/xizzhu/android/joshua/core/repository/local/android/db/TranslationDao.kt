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

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.ask.db.*
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.logger.Log
import kotlin.math.max

class TranslationDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_TEXT = "text"

        private val TAG: String = TranslationDao::class.java.simpleName
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun read(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> = db.withTransaction {
        if (!hasTableAndLogIfNoTable(translationShortName)) return@withTransaction emptyList()

        select(translationShortName, COLUMN_TEXT) {
            (COLUMN_BOOK_INDEX eq bookIndex) and (COLUMN_CHAPTER_INDEX eq chapterIndex)
        }.orderBy(COLUMN_VERSE_INDEX).toListIndexed { index, row ->
            Verse(VerseIndex(bookIndex, chapterIndex, index),
                    Verse.Text(translationShortName, row.getString(COLUMN_TEXT)), emptyList())
        }
    }

    @WorkerThread
    private fun SQLiteDatabase.hasTableAndLogIfNoTable(table: String): Boolean = hasTable(table)
            .also { hasTable -> if (!hasTable) Log.e(TAG, "", IllegalStateException("Missing translation $table")) }

    @WorkerThread
    fun read(translationShortName: String, parallelTranslations: List<String>,
             bookIndex: Int, chapterIndex: Int): List<Verse> = db.withTransaction {
        if (!hasTableAndLogIfNoTable(translationShortName)) return emptyList()

        val primaryTexts = readVerseTexts(translationShortName, bookIndex, chapterIndex)
        var versesCount = primaryTexts.size
        val parallelTexts = ArrayList<List<Verse.Text>>(parallelTranslations.size).apply {
            val countVerses: (List<Verse.Text>) -> Unit = { versesCount = max(versesCount, it.size) }
            parallelTranslations.forEach { add(readVerseTexts(it, bookIndex, chapterIndex).also(countVerses)) }
        }

        val verses = ArrayList<Verse>(versesCount)
        for (verseIndex in 0 until versesCount) {
            val primary = if (primaryTexts.size > verseIndex) primaryTexts[verseIndex] else Verse.Text(translationShortName, "")

            val parallel = ArrayList<Verse.Text>(parallelTranslations.size)
            for ((i, translation) in parallelTranslations.withIndex()) {
                parallel.add(parallelTexts[i].let {
                    return@let if (it.size > verseIndex) it[verseIndex] else Verse.Text(translation, "")
                })
            }

            verses.add(Verse(VerseIndex(bookIndex, chapterIndex, verseIndex), primary, parallel))
        }

        return verses
    }

    @WorkerThread
    private fun SQLiteDatabase.readVerseTexts(translation: String,
                                              bookIndex: Int, chapterIndex: Int): List<Verse.Text> =
            if (hasTableAndLogIfNoTable(translation)) {
                select(translation, COLUMN_TEXT) {
                    (COLUMN_BOOK_INDEX eq bookIndex) and (COLUMN_CHAPTER_INDEX eq chapterIndex)
                }.orderBy(COLUMN_VERSE_INDEX).toList { Verse.Text(translation, it.getString(COLUMN_TEXT)) }
            } else {
                emptyList()
            }

    @WorkerThread
    fun read(translationShortName: String, verseIndexes: List<VerseIndex>): Map<VerseIndex, Verse> = db.withTransaction {
        if (!hasTableAndLogIfNoTable(translationShortName)) return@withTransaction emptyMap()

        val verses = hashMapOf<VerseIndex, Verse>()
        // Work-around until https://github.com/xizzhu/ask/issues/8 is solved.
        verseIndexes.chunked(333).forEach { chunkedVerseIndexes ->
            select(translationShortName) {
                var condition: Condition = noOp()
                chunkedVerseIndexes.forEach { verseIndex ->
                    ((COLUMN_BOOK_INDEX eq verseIndex.bookIndex) and
                            (COLUMN_CHAPTER_INDEX eq verseIndex.chapterIndex) and
                            (COLUMN_VERSE_INDEX eq verseIndex.verseIndex)).run {
                        condition = if (condition == Condition.NoOp) this else condition or this
                    }
                }
                condition
            }.forEach { row ->
                val verseIndex = VerseIndex(row.getInt(COLUMN_BOOK_INDEX), row.getInt(COLUMN_CHAPTER_INDEX), row.getInt(COLUMN_VERSE_INDEX))
                verses[verseIndex] = Verse(verseIndex, Verse.Text(translationShortName, row.getString(COLUMN_TEXT)), emptyList())
            }
        }
        verses
    }

    @WorkerThread
    fun search(translationShortName: String, query: String): List<Verse> = db.withTransaction {
        if (query.isBlank() || !hasTableAndLogIfNoTable(translationShortName)) return@withTransaction emptyList()

        select(translationShortName) {
            var condition: Condition = noOp()
            query.trim().replace("\\s+", " ").split(" ").forEach { keyword ->
                (COLUMN_TEXT like "%%$keyword%%").run {
                    condition = if (condition == Condition.NoOp) this else condition and this
                }
            }
            condition
        }.orderBy(COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX, COLUMN_VERSE_INDEX).toList { row ->
            Verse(
                    VerseIndex(row.getInt(COLUMN_BOOK_INDEX), row.getInt(COLUMN_CHAPTER_INDEX), row.getInt(COLUMN_VERSE_INDEX)),
                    Verse.Text(translationShortName, row.getString(COLUMN_TEXT)), emptyList()
            )
        }
    }

    @WorkerThread
    fun save(translationShortName: String, verses: Map<Pair<Int, Int>, List<String>>) = db.withTransaction {
        if (hasTable(translationShortName)) {
            Log.e(TAG, "", IllegalStateException("Translation $translationShortName already installed"))
            remove(translationShortName)
        }
        createTable(translationShortName) {
            it[COLUMN_BOOK_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_CHAPTER_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_VERSE_INDEX] = INTEGER + PRIMARY_KEY + NOT_NULL
            it[COLUMN_TEXT] = TEXT + NOT_NULL
        }

        verses.forEach { (index, verseList) ->
            val bookIndex = index.first
            val chapterIndex = index.second
            verseList.forEachIndexed { verseIndex, verse ->
                insert(translationShortName, SQLiteDatabase.CONFLICT_REPLACE) {
                    it[COLUMN_BOOK_INDEX] = bookIndex
                    it[COLUMN_CHAPTER_INDEX] = chapterIndex
                    it[COLUMN_VERSE_INDEX] = verseIndex
                    it[COLUMN_TEXT] = verse
                }
            }
        }
    }

    @WorkerThread
    fun remove(translationShortName: String) {
        db.dropTable(translationShortName)
    }
}
