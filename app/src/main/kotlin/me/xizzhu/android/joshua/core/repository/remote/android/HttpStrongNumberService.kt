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
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberVerses
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

class HttpStrongNumberService : RemoteStrongNumberStorage {
    override suspend fun fetchVerses(channel: SendChannel<Int>): RemoteStrongNumberVerses = withContext(Dispatchers.IO) {
        val verses = hashMapOf<VerseIndex, List<Int>>()

        var progress = -1
        ZipInputStream(BufferedInputStream(getInputStream("tools/sn-verses.zip")))
                .forEachIndexed { index, entryName, contentReader ->
                    val (bookIndex, chapterIndex) = entryName.substring(0, entryName.length - 5).split("-")
                    contentReader.readStrongNumberVerses().forEach {
                        verses[VerseIndex(bookIndex.toInt(), chapterIndex.toInt(), it.key)] = it.value
                    }

                    // only emits if the progress is actually changed
                    val currentProgress = index / 12
                    if (currentProgress > progress) {
                        progress = currentProgress
                        channel.offer(progress)
                    }
                }

        return@withContext RemoteStrongNumberVerses(verses)
    }
}
