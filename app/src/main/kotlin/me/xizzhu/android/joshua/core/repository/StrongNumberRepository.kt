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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberStorage

class StrongNumberRepository(private val localStrongNumberStorage: LocalStrongNumberStorage,
                             private val remoteStrongNumberStorage: RemoteStrongNumberStorage) {
    suspend fun read(verseIndex: VerseIndex): List<StrongNumber> = localStrongNumberStorage.read(verseIndex)

    fun download(): Flow<Int> = download(Channel(Channel.CONFLATED), Channel(Channel.CONFLATED))

    @VisibleForTesting
    fun download(versesDownloadProgress: Channel<Int>, wordsDownloadProgress: Channel<Int>) = channelFlow {
        versesDownloadProgress.offer(0)
        wordsDownloadProgress.offer(0)
        versesDownloadProgress.consumeAsFlow().combine(wordsDownloadProgress.consumeAsFlow()) { v, w ->
            offer((v * 0.9 + w * 0.1).toInt())
        }.launchIn(this)

        val remoteVersesAsync = async { remoteStrongNumberStorage.fetchIndexes(versesDownloadProgress) }
        val remoteWords = remoteStrongNumberStorage.fetchWords(wordsDownloadProgress)
        val remoteVerses = remoteVersesAsync.await()

        versesDownloadProgress.close()
        wordsDownloadProgress.close()
        offer(100)

        localStrongNumberStorage.save(remoteVerses.indexes, remoteVerses.reverseIndexes, remoteWords.words)

        offer(101)
    }
}
