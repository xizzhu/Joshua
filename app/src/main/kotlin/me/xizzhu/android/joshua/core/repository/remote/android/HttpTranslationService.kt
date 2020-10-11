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

package me.xizzhu.android.joshua.core.repository.remote.android

import android.util.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslation
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import java.io.*
import java.util.zip.ZipInputStream

class HttpTranslationService : RemoteTranslationService {
    override suspend fun fetchTranslations(): List<RemoteTranslationInfo> = withContext(Dispatchers.IO) {
        return@withContext JsonReader(BufferedReader(InputStreamReader(getInputStream("list.json"), "UTF-8")))
                .use { reader -> reader.readListJson() }
    }

    override suspend fun fetchTranslation(channel: SendChannel<Int>, translationInfo: RemoteTranslationInfo): RemoteTranslation = withContext(Dispatchers.IO) {
        lateinit var bookNamesShortNamesPair: Pair<List<String>, List<String>>
        val verses = HashMap<Pair<Int, Int>, List<String>>()

        var progress = -1
        ZipInputStream(BufferedInputStream(getInputStream("translations/${translationInfo.shortName}.zip")))
                .forEachIndexed { index, entryName, contentReader ->
                    if (entryName == "books.json") {
                        bookNamesShortNamesPair = contentReader.readBooksJson()
                    } else {
                        val (bookIndex, chapterIndex) = entryName.substring(0, entryName.length - 5).split("-")
                        verses[Pair(bookIndex.toInt(), chapterIndex.toInt())] = contentReader.readChapterJson()
                    }

                    // only emits if the progress is actually changed
                    val currentProgress = index / 12
                    if (currentProgress > progress) {
                        progress = currentProgress
                        channel.offer(progress)
                    }
                }

        return@withContext RemoteTranslation(translationInfo, bookNamesShortNamesPair.first, bookNamesShortNamesPair.second, verses)
    }
}
