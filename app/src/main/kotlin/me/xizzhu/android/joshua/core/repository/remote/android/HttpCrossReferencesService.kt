/*
 * Copyright (C) 2022 Xizhi Zhu
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
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.remote.RemoteCrossReferences
import me.xizzhu.android.joshua.core.repository.remote.RemoteCrossReferencesStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberIndexes
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberWords
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

class HttpCrossReferencesService(context: Context) : RemoteCrossReferencesStorage {
    private val cacheDir: File by lazy { File(context.cacheDir, "crossReferences").apply { mkdirs() } }

    override suspend fun fetchCrossReferences(downloadProgress: SendChannel<Int>): RemoteCrossReferences = withContext(Dispatchers.IO) {
        return@withContext File(cacheDir, "cross_references").apply {
            download(downloadProgress, "tools/cross_references.zip", 1696214, this) // TODO #21
        }.let { downloaded ->
            FileInputStream(downloaded).use { input ->
                toRemoteCrossReferences(input)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toRemoteCrossReferences(inputStream: InputStream): RemoteCrossReferences {
        val references = hashMapOf<VerseIndex, List<VerseIndex>>()

        ZipInputStream(BufferedInputStream(inputStream)).forEach { entryName, contentReader ->
            val book: Int
            val chapter: Int
            entryName.substring(0, entryName.length - 5).split("-").run {
                book = get(0).toInt()
                chapter = get(1).toInt()
            }
            contentReader.readCrossReferences().forEach { (verse, to) ->
                references[VerseIndex(book, chapter, verse)] = to
            }
        }

        return RemoteCrossReferences(references)
    }

    override suspend fun removeCrossReferencesCache(): Unit = withContext(Dispatchers.IO) {
        File(cacheDir, "cross_references").delete()
    }
}
