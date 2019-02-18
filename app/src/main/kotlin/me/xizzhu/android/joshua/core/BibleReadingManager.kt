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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.internal.repository.BibleReadingRepository

class BibleReadingManager constructor(private val bibleReadingRepository: BibleReadingRepository) {
    private val currentTranslationShortName: BroadcastChannel<String> = ConflatedBroadcastChannel()
    private val currentVerseIndex: BroadcastChannel<VerseIndex> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            currentTranslationShortName.send(bibleReadingRepository.readCurrentTranslation())
            currentVerseIndex.send(bibleReadingRepository.readCurrentVerseIndex())
        }
    }

    fun observeCurrentTranslation(): ReceiveChannel<String> = currentTranslationShortName.openSubscription()

    suspend fun updateCurrentTranslation(translationShortName: String): Unit =
            withContext(Dispatchers.IO) {
                currentTranslationShortName.send(translationShortName)
                bibleReadingRepository.saveCurrentTranslation(translationShortName)
            }

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = currentVerseIndex.openSubscription()

    suspend fun updateCurrentVerseIndex(verseIndex: VerseIndex): Unit =
            withContext(Dispatchers.IO) {
                currentVerseIndex.send(verseIndex)
                bibleReadingRepository.saveCurrentVerseIndex(verseIndex)
            }

    suspend fun readBookNames(translationShortName: String): List<String> =
            withContext(Dispatchers.IO) {
                bibleReadingRepository.readBookNames(translationShortName)
            }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            withContext(Dispatchers.IO) {
                bibleReadingRepository.readVerses(translationShortName, bookIndex, chapterIndex)
            }
}
