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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.local.LocalStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberIndexes
import me.xizzhu.android.joshua.core.repository.remote.RemoteStrongNumberWords
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class StrongNumberRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localStrongNumberStorage: LocalStrongNumberStorage
    @Mock
    private lateinit var remoteStrongNumberStorage: RemoteStrongNumberStorage

    private lateinit var strongNumberRepository: StrongNumberRepository

    @BeforeTest
    override fun setup() {
        super.setup()

        strongNumberRepository = StrongNumberRepository(localStrongNumberStorage, remoteStrongNumberStorage)
    }

    @Test
    fun testDownload() = runBlocking {
        val versesDownloadProgress = Channel<Int>()
        val wordsDownloadProgress = Channel<Int>()
        `when`(remoteStrongNumberStorage.fetchIndexes(versesDownloadProgress)).thenReturn(RemoteStrongNumberIndexes(MockContents.strongNumberIndex, MockContents.strongNumberReverseIndex))
        `when`(remoteStrongNumberStorage.fetchWords(wordsDownloadProgress)).thenReturn(RemoteStrongNumberWords(MockContents.strongNumberWords))

        var called = false
        strongNumberRepository.download(versesDownloadProgress, wordsDownloadProgress)
                .collect {
                    assertTrue(it in (0..100))
                    if (it == 100) called = true
                }
        assertTrue(called)
        assertTrue(versesDownloadProgress.isClosedForSend && versesDownloadProgress.isClosedForReceive)
        assertTrue(wordsDownloadProgress.isClosedForSend && wordsDownloadProgress.isClosedForReceive)
        verify(localStrongNumberStorage, times(1)).save(
                MockContents.strongNumberIndex, MockContents.strongNumberReverseIndex, MockContents.strongNumberWords)
    }
}
