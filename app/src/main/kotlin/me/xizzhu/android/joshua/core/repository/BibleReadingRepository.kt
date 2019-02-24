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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex

class BibleReadingRepository(private val localStorage: LocalStorage) {
    private val currentTranslationShortName: BroadcastChannel<String> = ConflatedBroadcastChannel("")
    private val currentVerseIndex: BroadcastChannel<VerseIndex> = ConflatedBroadcastChannel(VerseIndex.INVALID)

    init {
        GlobalScope.launch(Dispatchers.IO) {
            currentTranslationShortName.send(localStorage.readCurrentTranslation())
            currentVerseIndex.send(localStorage.readCurrentVerseIndex())
        }
    }

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = currentVerseIndex.openSubscription()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        currentVerseIndex.send(verseIndex)
        localStorage.saveCurrentVerseIndex(verseIndex)
    }

    fun observeCurrentTranslation(): ReceiveChannel<String> = currentTranslationShortName.openSubscription()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        currentTranslationShortName.send(translationShortName)
        localStorage.saveCurrentTranslation(translationShortName)
    }

    fun readBookNames(translationShortName: String): List<String> =
            localStorage.readBookNames(translationShortName)

    fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            localStorage.readVerses(translationShortName, bookIndex, chapterIndex)

    fun search(translationShortName: String, query: String): List<Verse> =
            localStorage.search(translationShortName, query)
}
