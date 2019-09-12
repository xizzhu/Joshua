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
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var serializer: BackupManager.Serializer
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
            `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(emptyList())
            `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(emptyList())
            `when`(noteManager.read(Constants.SORT_BY_BOOK)).thenReturn(emptyList())
            `when`(readingProgressManager.read()).thenReturn(ReadingProgress(0, 0L, emptyList()))
            backupManager = BackupManager(serializer, bookmarkManager, highlightManager, noteManager, readingProgressManager)
        }
    }

    @Test
    fun testPrepareForBackup() {
        runBlocking {
            `when`(serializer.serialize(any())).thenReturn("random value")
            assertEquals("random value", backupManager.prepareForBackup())
        }
    }

    @Test(expected = RuntimeException::class)
    fun testPrepareForBackupWithException() {
        runBlocking {
            `when`(serializer.serialize(any())).thenThrow(RuntimeException("Random exception"))
            backupManager.prepareForBackup()
        }
    }

    @Test(expected = RuntimeException::class)
    fun testPrepareForBackupWithAsyncFailed() {
        runBlocking {
            `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenThrow(RuntimeException("Random exception"))
            backupManager.prepareForBackup()
        }
    }

    @Test
    fun testRestoreWithEmptyReadingProgressChoosingBackup() {
        runBlocking {
            `when`(readingProgressManager.read()).thenReturn(ReadingProgress(100, 0L, emptyList()))
            `when`(serializer.deserialize("")).thenReturn(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())))
            backupManager.restore("")
            verify(readingProgressManager, times(1)).save(ReadingProgress(1, 2L, emptyList()))
        }
    }

    @Test
    fun testRestoreWithEmptyReadingProgressChoosingCurrent() {
        runBlocking {
            `when`(readingProgressManager.read()).thenReturn(ReadingProgress(3, 4L, emptyList()))
            `when`(serializer.deserialize("")).thenReturn(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(100, 0L, emptyList())))
            backupManager.restore("")
            verify(readingProgressManager, times(1)).save(ReadingProgress(3, 4L, emptyList()))
        }
    }

    @Test
    fun testRestoreWithReadingProgress() {
        runBlocking {
            `when`(readingProgressManager.read()).thenReturn(ReadingProgress(0, 0L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                    ReadingProgress.ChapterReadingStatus(1, 2, 30, 40L, 5L)
            )))
            `when`(serializer.deserialize("")).thenReturn(BackupManager.Data(emptyList(), emptyList(), emptyList(),
                    ReadingProgress(1, 2L, listOf(
                            ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 55L),
                            ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
                    ))))
            backupManager.restore("")
            verify(readingProgressManager, times(1)).save(ReadingProgress(1, 2L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 55L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
        }
    }

    @Test
    fun testRestoreWithReadingProgress2() {
        runBlocking {
            `when`(readingProgressManager.read()).thenReturn(ReadingProgress(0, 0L, listOf(
                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 55L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
            `when`(serializer.deserialize("")).thenReturn(BackupManager.Data(emptyList(), emptyList(), emptyList(),
                    ReadingProgress(1, 2L, listOf(
                            ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                            ReadingProgress.ChapterReadingStatus(1, 2, 30, 40L, 5L)
                    ))))
            backupManager.restore("")
            verify(readingProgressManager, times(1)).save(ReadingProgress(1, 2L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 55L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
        }
    }
}
