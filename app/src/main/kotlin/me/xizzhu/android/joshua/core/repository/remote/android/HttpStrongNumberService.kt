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

package me.xizzhu.android.joshua.core.repository.remote.android

import android.content.Context
import androidx.annotation.VisibleForTesting
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
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

class HttpStrongNumberService(context: Context) : RemoteStrongNumberStorage {
    private val cacheDir: File by lazy { File(context.cacheDir, "strongNumbers").apply { mkdirs() } }

    override suspend fun fetchIndexes(downloadProgress: SendChannel<Int>): RemoteStrongNumberIndexes = withContext(Dispatchers.IO) {
        return@withContext File(cacheDir, "sn_indexes").apply {
            download(downloadProgress, "tools/sn_indexes.zip", 1195800, this) // TODO #21
        }.let { downloaded ->
            FileInputStream(downloaded).use { input ->
                toRemoteStrongNumberIndexes(input)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toRemoteStrongNumberIndexes(inputStream: InputStream): RemoteStrongNumberIndexes {
        val indexes = hashMapOf<VerseIndex, List<String>>()
        val reverseIndexes = hashMapOf<String, HashSet<VerseIndex>>()

        ZipInputStream(BufferedInputStream(inputStream)).forEachIndexed { index, entryName, contentReader ->
            val book: Int
            val chapter: Int
            entryName.substring(0, entryName.length - 5).split("-").run {
                book = get(0).toInt()
                chapter = get(1).toInt()
            }
            contentReader.readStrongNumberVerses().forEach { (verse, strongWords) ->
                val verseIndex = VerseIndex(book, chapter, verse - 1)
                strongWords.map { if (book < Bible.OLD_TESTAMENT_COUNT) "H$it" else "G$it" }.run {
                    indexes[verseIndex] = this
                    forEach { sn ->
                        (reverseIndexes[sn]
                                ?: HashSet<VerseIndex>().apply { reverseIndexes[sn] = this }).add(verseIndex)
                    }
                }
            }
        }

        return RemoteStrongNumberIndexes(indexes, reverseIndexes.mapValues { (_, verseIndexes) -> verseIndexes.toList() })
    }

    override suspend fun fetchWords(downloadProgress: SendChannel<Int>): RemoteStrongNumberWords = withContext(Dispatchers.IO) {
        return@withContext File(cacheDir, "sn_en").apply {
            download(downloadProgress, "tools/sn_en.zip", 190102, this) // TODO #21
        }.let { downloaded ->
            FileInputStream(downloaded).use { input ->
                toRemoteStrongNumberWords(input)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toRemoteStrongNumberWords(inputStream: InputStream): RemoteStrongNumberWords {
        val words = hashMapOf<String, String>()
        ZipInputStream(BufferedInputStream(inputStream)).forEachIndexed { _, entryName, contentReader ->
            when (entryName) {
                "hebrew.json" -> {
                    contentReader.readStrongNumberWords().apply {
                        if (size != Constants.STRONG_NUMBER_HEBREW_COUNT) {
                            throw IllegalStateException("Incorrect Strong number Hebrew words count: $size")
                        }
                    }.forEach { (sn, meaning) -> words["H$sn"] = meaning }
                }
                "greek.json" -> {
                    contentReader.readStrongNumberWords().apply {
                        if (size != Constants.STRONG_NUMBER_GREEK_COUNT) {
                            throw IllegalStateException("Incorrect Strong number Greek words count: $size")
                        }
                    }.forEach { (sn, meaning) -> words["G$sn"] = meaning }
                }
                else -> throw IllegalStateException("Unknown entry ($entryName) in Strong number words")
            }
        }

        if (words.size != Constants.STRONG_NUMBER_HEBREW_COUNT + Constants.STRONG_NUMBER_GREEK_COUNT) {
            throw IllegalStateException("Incorrect Strong number words count (${words.size})")
        }
        return RemoteStrongNumberWords(words)
    }
}
