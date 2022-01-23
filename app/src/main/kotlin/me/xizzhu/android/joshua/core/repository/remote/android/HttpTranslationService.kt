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
import android.util.JsonReader
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslation
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import java.io.*
import java.util.zip.ZipInputStream

class HttpTranslationService(context: Context) : RemoteTranslationService {
    private val cacheDir: File by lazy { File(context.cacheDir, "translations").apply { mkdirs() } }

    override suspend fun fetchTranslations(): List<RemoteTranslationInfo> = withContext(Dispatchers.IO) {
        return@withContext toRemoteTranslations(getInputStream("list.json"))
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toRemoteTranslations(inputStream: InputStream): List<RemoteTranslationInfo> =
            JsonReader(BufferedReader(InputStreamReader(inputStream, "UTF-8"))).use { reader -> reader.readListJson() }

    override suspend fun fetchTranslation(
            downloadProgress: SendChannel<Int>, translationInfo: RemoteTranslationInfo
    ): RemoteTranslation = withContext(Dispatchers.IO) {
        return@withContext File(cacheDir, translationInfo.shortName).apply {
            download(downloadProgress, "translations/${translationInfo.shortName}.zip", translationInfo.size, this)
        }.let { downloaded ->
            FileInputStream(downloaded).use { input ->
                toRemoteTranslation(translationInfo, input)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toRemoteTranslation(translationInfo: RemoteTranslationInfo, inputStream: InputStream): RemoteTranslation {
        val bookNames = arrayListOf<String>()
        val bookShortNames = arrayListOf<String>()
        val verses = HashMap<Pair<Int, Int>, List<String>>()

        ZipInputStream(BufferedInputStream(inputStream)).forEach { entryName, contentReader ->
            if (entryName == "books.json") {
                contentReader.readBooksJson(bookNames, bookShortNames)
            } else {
                val (bookIndex, chapterIndex) = entryName.substring(0, entryName.length - 5).split("-")
                verses[Pair(bookIndex.toInt(), chapterIndex.toInt())] = contentReader.readChapterJson()
            }
        }

        return RemoteTranslation(translationInfo, bookNames, bookShortNames, verses)
    }

    override suspend fun removeTranslationCache(translationInfo: RemoteTranslationInfo): Unit = withContext(Dispatchers.IO) {
        File(cacheDir, translationInfo.shortName).delete()
    }
}
