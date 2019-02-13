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

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.internal.repository.BibleReadingRepository

class BibleReadingManager constructor(private val bibleReadingRepository: BibleReadingRepository) {
    private val currentTranslationShortName: BroadcastChannel<String> = ConflatedBroadcastChannel()
    private val currentIndex: BroadcastChannel<VerseIndex> = ConflatedBroadcastChannel()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            currentTranslationShortName.send(bibleReadingRepository.readCurrentTranslation())
            currentIndex.send(bibleReadingRepository.readCurrentVerseIndex())
        }
    }

    fun observeCurrentTranslation(): ReceiveChannel<String> = currentTranslationShortName.openSubscription()

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = currentIndex.openSubscription()

    var currentTranslation: String = ""
        @WorkerThread get() {
            if (field.isEmpty()) {
                field = bibleReadingRepository.readCurrentTranslation()
            }
            return field
        }
        @WorkerThread set(value) {
            if (value != field) {
                field = value
                bibleReadingRepository.saveCurrentTranslation(value)
            }
        }

    var currentVerseIndex: VerseIndex = VerseIndex.INVALID
        @WorkerThread get() {
            if (!field.isValid()) {
                field = bibleReadingRepository.readCurrentVerseIndex()
            }
            return field
        }
        @WorkerThread set(value) {
            if (field != value && value.isValid()) {
                field = value
                bibleReadingRepository.saveCurrentVerseIndex(value)

                GlobalScope.launch { currentIndex.send(value) }
            }
        }

    @WorkerThread
    fun readBookNames(translationShortName: String): List<String> =
            bibleReadingRepository.readBookNames(translationShortName)

    @WorkerThread
    fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingRepository.readVerses(translationShortName, bookIndex, chapterIndex)
}
