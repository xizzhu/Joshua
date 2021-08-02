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

package me.xizzhu.android.joshua.core.repository

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.perf.Perf
import me.xizzhu.android.joshua.core.repository.local.LocalStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberStorage
import me.xizzhu.android.logger.Log

class StrongNumberRepository(private val localStrongNumberStorage: LocalStrongNumberStorage,
                             private val remoteStrongNumberStorage: RemoteStrongNumberStorage) {
    companion object {
        private val TAG: String = StrongNumberRepository::class.java.simpleName
    }

    suspend fun readStrongNumber(strongNumber: String): StrongNumber = localStrongNumberStorage.readStrongNumber(strongNumber)

    suspend fun readStrongNumber(verseIndex: VerseIndex): List<StrongNumber> = localStrongNumberStorage.readStrongNumber(verseIndex)

    suspend fun readVerseIndexes(strongNumber: String): List<VerseIndex> = localStrongNumberStorage.readVerseIndexes(strongNumber)

    fun download(): Flow<Int> = download(Channel(Channel.CONFLATED), Channel(Channel.CONFLATED))

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun download(versesDownloadProgress: Channel<Int>, wordsDownloadProgress: Channel<Int>) = channelFlow {
        Log.i(TAG, "Start downloading Strong number")

        versesDownloadProgress.trySend(0)
        wordsDownloadProgress.trySend(0)
        versesDownloadProgress.consumeAsFlow().combine(wordsDownloadProgress.consumeAsFlow()) { v, w ->
            trySend((v * 0.9 + w * 0.1).toInt())
        }.launchIn(this)

        val remoteIndexesAsync = async { remoteStrongNumberStorage.fetchIndexes(versesDownloadProgress) }
        val remoteWords = remoteStrongNumberStorage.fetchWords(wordsDownloadProgress)
        val remoteIndexes = remoteIndexesAsync.await()
        Log.i(TAG, "Strong number downloaded")

        versesDownloadProgress.close()
        wordsDownloadProgress.close()
        trySend(100)

        Perf.trace("install_sn") {
            localStrongNumberStorage.save(remoteIndexes.indexes, remoteIndexes.reverseIndexes, remoteWords.words)
        }
        Log.i(TAG, "Strong number saved to database")
    }
}
