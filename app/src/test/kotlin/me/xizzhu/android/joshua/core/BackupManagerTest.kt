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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var serializer: BackupManager.Serializer
    @Mock
    private lateinit var deserializer: BackupManager.Deserializer
    @Mock
    private lateinit var bookmarkManager: BookmarkManager
    @Mock
    private lateinit var highlightManager: HighlightManager
    @Mock
    private lateinit var noteManager: NoteManager
    @Mock
    private lateinit var readingProgressManager: ReadingProgressManager

    private lateinit var backupManager: BackupManager

    @BeforeTest
    override fun setup() {
        super.setup()

        runBlocking {
            `when`(serializer.withBookmarks(any())).thenReturn(serializer)
            `when`(serializer.withHighlights(any())).thenReturn(serializer)
            `when`(serializer.withNotes(any())).thenReturn(serializer)
            `when`(serializer.withReadingProgress(any())).thenReturn(serializer)
            `when`(bookmarkManager.read(Constants.SORT_BY_DATE)).thenReturn(emptyList())
            `when`(highlightManager.read(Constants.SORT_BY_DATE)).thenReturn(emptyList())
            `when`(noteManager.read(Constants.SORT_BY_DATE)).thenReturn(emptyList())
            `when`(readingProgressManager.readReadingProgress()).thenReturn(ReadingProgress(1, 2L, emptyList()))
            backupManager = BackupManager({ serializer }, { deserializer }, bookmarkManager, highlightManager, noteManager, readingProgressManager)
        }
    }

    @Test
    fun testPrepareJsonForBackup() {
        runBlocking {
            `when`(serializer.serialize()).thenReturn("random value")
            assertEquals("random value", backupManager.prepareForBackup())
        }
    }

    @Test(expected = RuntimeException::class)
    fun testPrepareJsonForBackupWithException() {
        runBlocking {
            `when`(serializer.serialize()).thenThrow(RuntimeException("Random exception"))
            backupManager.prepareForBackup()
        }
    }

    @Test(expected = RuntimeException::class)
    fun testPrepareJsonForBackupWithAsyncFailed() {
        runBlocking {
            `when`(bookmarkManager.read(Constants.SORT_BY_DATE)).thenThrow(RuntimeException("Random exception"))
            backupManager.prepareForBackup()
        }
    }
}
