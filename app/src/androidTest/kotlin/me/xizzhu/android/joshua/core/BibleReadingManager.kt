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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow

object BibleReadingManager {
    val currentTranslation = ConflatedBroadcastChannel<String>()

    init {
        reset()
    }

    fun reset() {
        currentTranslation.offer("")
    }

    fun currentVerseIndex(): Flow<VerseIndex> = emptyFlow()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
    }

    fun currentTranslation(): Flow<String> = currentTranslation.asFlow()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        currentTranslation.send(translationShortName)
    }

    fun parallelTranslations(): Flow<List<String>> = emptyFlow()

    suspend fun requestParallelTranslation(translationShortName: String) {
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
    }

    suspend fun clearParallelTranslation() {
    }

    suspend fun readBookNames(translationShortName: String): List<String> = emptyList()

    suspend fun readBookShortNames(translationShortName: String): List<String> = emptyList()

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> = emptyList()

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> = emptyList()

    suspend fun readVerses(translationShortName: String, verseIndexes: List<VerseIndex>): Map<VerseIndex, Verse> = emptyMap()

    suspend fun search(translationShortName: String, query: String): List<Verse> = emptyList()
}
