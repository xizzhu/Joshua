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

package me.xizzhu.android.joshua.core.repository.remote.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberIndexes
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberWords
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

class HttpStrongNumberService : RemoteStrongNumberStorage {
    override suspend fun fetchIndexes(channel: SendChannel<Int>): RemoteStrongNumberIndexes = withContext(Dispatchers.IO) {
        val indexes = hashMapOf<VerseIndex, List<String>>()

        var progress = -1
        ZipInputStream(BufferedInputStream(getInputStream("tools/sn_indexes.zip")))
                .forEachIndexed { index, entryName, contentReader ->
                    val bookIndex: Int
                    val chapterIndex: Int
                    entryName.substring(0, entryName.length - 5).split("-").run {
                        bookIndex = get(0).toInt()
                        chapterIndex = get(1).toInt()
                    }
                    contentReader.readStrongNumberVerses().forEach { (verseIndex, strongWords) ->
                        indexes[VerseIndex(bookIndex, chapterIndex, verseIndex - 1)] =
                                strongWords.map { if (bookIndex < Bible.OLD_TESTAMENT_COUNT) "H$it" else "G$it" }
                    }

                    // only emits if the progress is actually changed
                    val currentProgress = index / 12
                    if (currentProgress > progress) {
                        progress = currentProgress
                        channel.offer(progress)
                    }
                }

        return@withContext RemoteStrongNumberIndexes(indexes)
    }

    override suspend fun fetchWords(channel: SendChannel<Int>): RemoteStrongNumberWords = withContext(Dispatchers.IO) {
        val words = hashMapOf<String, String>()

        var progress = 0
        ZipInputStream(BufferedInputStream(getInputStream("tools/sn_en.zip")))
                .forEachIndexed { index, entryName, contentReader ->
                    when (entryName) {
                        "hebrew.json" -> {
                            contentReader.readStrongNumberWords().apply {
                                if (size != Constants.STRONG_NUMBER_HEBREW_COUNT) {
                                    throw IllegalStateException("Incorrect Strong number Hebrew words count: $size")
                                }
                            }.forEach { (sn, meaning) -> words["H$sn"] = meaning }

                            progress += 50
                            channel.offer(progress)
                        }
                        "greek.json" -> {
                            contentReader.readStrongNumberWords().apply {
                                if (size != Constants.STRONG_NUMBER_GREEK_COUNT) {
                                    throw IllegalStateException("Incorrect Strong number Greek words count: $size")
                                }
                            }.forEach { (sn, meaning) -> words["G$sn"] = meaning }

                            progress += 50
                            channel.offer(progress)
                        }
                        else -> throw IllegalStateException("Unknown entry ($entryName) in Strong number words")
                    }
                }

        if (words.size != Constants.STRONG_NUMBER_HEBREW_COUNT + Constants.STRONG_NUMBER_GREEK_COUNT) {
            throw IllegalStateException("Incorrect Strong number words count (${words.size})")
        }
        return@withContext RemoteStrongNumberWords(words)
    }
}
