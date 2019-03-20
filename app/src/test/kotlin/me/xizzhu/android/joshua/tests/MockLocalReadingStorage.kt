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

package me.xizzhu.android.joshua.tests

import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage

class MockLocalReadingStorage : LocalReadingStorage {
    private var currentVerseIndex: VerseIndex = VerseIndex.INVALID
    private var currentTranslation: String = ""

    override suspend fun readCurrentVerseIndex(): VerseIndex {
        return currentVerseIndex
    }

    override suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        currentVerseIndex = verseIndex
    }

    override suspend fun readCurrentTranslation(): String {
        return currentTranslation
    }

    override suspend fun saveCurrentTranslation(translationShortName: String) {
        currentTranslation = translationShortName
    }

    override suspend fun readBookNames(translationShortName: String): List<String> {
        return if (MockContents.kjvShortName == translationShortName) {
            MockContents.kjvBookNames
        } else {
            emptyList()
        }
    }

    override suspend fun readVerses(translationShortName: String, bookIndex: Int,
                                    chapterIndex: Int, bookName: String): List<Verse> {
        return if (MockContents.kjvShortName == translationShortName && bookIndex == 0 && chapterIndex == 0) {
            MockContents.kjvVerses
        } else {
            emptyList()
        }
    }

    override suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                                    bookIndex: Int, chapterIndex: Int): List<Verse> {
        return readVerses(translationShortName, bookIndex, chapterIndex, "")
    }

    override suspend fun readVerse(translationShortName: String, verseIndex: VerseIndex): Verse {
        return readVerses(translationShortName, verseIndex.bookIndex, verseIndex.chapterIndex, "")[verseIndex.verseIndex]
    }

    override suspend fun search(translationShortName: String, bookNames: List<String>, query: String): List<Verse> {
        return if (MockContents.kjvShortName == translationShortName) {
            val result = mutableListOf<Verse>()
            for (verse in MockContents.kjvVerses) {
                if (verse.text.text.contains(query, true)) {
                    result.add(verse)
                }
            }
            result
        } else {
            emptyList()
        }
    }
}
