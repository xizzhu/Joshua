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

package me.xizzhu.android.joshua.core.repository.remote.http

import android.util.JsonReader
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslation
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import java.io.*
import java.net.URL
import java.util.zip.ZipInputStream

open class HttpTranslationService : RemoteTranslationService {
    companion object {
        private const val TIMEOUT_IN_MILLISECONDS = 30000 // 30 seconds
        private const val BASE_URL = "https://xizzhu.me/bible/download"
    }

    override suspend fun fetchTranslations(): List<RemoteTranslationInfo> = withContext(Dispatchers.IO) {
        return@withContext JsonReader(BufferedReader(InputStreamReader(getInputStream("list.json"), "UTF-8")))
                .use { reader -> reader.readListJson() }
    }

    @VisibleForTesting
    open fun getInputStream(relativeUrl: String): InputStream =
            URL("$BASE_URL/$relativeUrl")
                    .openConnection()
                    .apply {
                        connectTimeout = TIMEOUT_IN_MILLISECONDS
                        readTimeout = TIMEOUT_IN_MILLISECONDS
                    }.getInputStream()

    override suspend fun fetchTranslation(channel: SendChannel<Int>, translationInfo: RemoteTranslationInfo): RemoteTranslation = withContext(Dispatchers.IO) {
        lateinit var bookNamesShortNamesPair: Pair<List<String>, List<String>>
        val verses = HashMap<Pair<Int, Int>, List<String>>()
        ZipInputStream(BufferedInputStream(getInputStream("${translationInfo.shortName}.zip")))
                .use { zipInputStream ->
                    val buffer = ByteArray(4096)
                    val os = ByteArrayOutputStream()
                    var downloaded = 0
                    var progress = -1
                    while (true) {
                        val entryName = zipInputStream.nextEntry?.name ?: break

                        os.reset()
                        while (true) {
                            val byteCount = zipInputStream.read(buffer)
                            if (byteCount < 0) break
                            os.write(buffer, 0, byteCount)
                        }
                        val jsonReader = JsonReader(StringReader(os.toString("UTF-8")))

                        if (entryName == "books.json") {
                            bookNamesShortNamesPair = jsonReader.readBooksJson()
                        } else {
                            val (bookIndex, chapterIndex) = entryName.substring(0, entryName.length - 5).split("-")
                            verses[Pair(bookIndex.toInt(), chapterIndex.toInt())] = jsonReader.readChapterJson()
                        }

                        // only emits if the progress is actually changed
                        val currentProgress = ++downloaded / 12
                        if (currentProgress > progress) {
                            progress = currentProgress
                            channel.send(progress)
                        }
                    }
                }

        return@withContext RemoteTranslation(translationInfo, bookNamesShortNamesPair.first, bookNamesShortNamesPair.second, verses)
    }
}
