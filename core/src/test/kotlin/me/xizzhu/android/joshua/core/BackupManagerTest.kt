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
    private lateinit var bookmarkManager: VerseAnnotationManager<Bookmark>
    @Mock
    private lateinit var highlightManager: VerseAnnotationManager<Highlight>
    @Mock
    private lateinit var noteManager: VerseAnnotationManager<Note>
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
    fun testRestoreWithMinimumContent() {
        runBlocking {
            `when`(serializer.deserialize("")).thenReturn(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())))
            backupManager.restore("")
            verify(readingProgressManager, times(1)).save(ReadingProgress(1, 2L, emptyList()))
        }
    }

    @Test
    fun testRestoreWithBookmarks() {
        runBlocking {
            `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 3L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
            `when`(serializer.deserialize("")).thenReturn(
                    BackupManager.Data(
                            listOf(
                                    Bookmark(VerseIndex(0, 1, 2), 33L),
                                    Bookmark(VerseIndex(0, 1, 3), 456L),
                                    Bookmark(VerseIndex(4, 5, 6), 1L)
                            ),
                            emptyList(),
                            emptyList(),
                            ReadingProgress(0, 0L, emptyList())
                    )
            )

            backupManager.restore("")
            verify(bookmarkManager, times(1)).save(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 33L),
                    Bookmark(VerseIndex(0, 1, 3), 456L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
        }
    }

    @Test
    fun testRestoreWithHighlights() {
        runBlocking {
            `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_YELLOW, 3L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
            `when`(serializer.deserialize("")).thenReturn(
                    BackupManager.Data(
                            emptyList(),
                            listOf(
                                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_PURPLE, 1L)
                            ),
                            emptyList(),
                            ReadingProgress(0, 0L, emptyList())
                    )
            )

            backupManager.restore("")
            verify(highlightManager, times(1)).save(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
        }
    }

    @Test
    fun testRestoreWithNotes() {
        runBlocking {
            `when`(noteManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Note(VerseIndex(0, 1, 2), "random note", 3L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
            `when`(serializer.deserialize("")).thenReturn(
                    BackupManager.Data(
                            emptyList(),
                            emptyList(),
                            listOf(
                                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                                    Note(VerseIndex(4, 5, 6), "older random note", 1L)
                            ),
                            ReadingProgress(0, 0L, emptyList())
                    )
            )

            backupManager.restore("")
            verify(noteManager, times(1)).save(listOf(
                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
        }
    }

    @Test
    fun testRestoreWithReadingProgress() {
        runBlocking {
            `when`(readingProgressManager.read()).thenReturn(ReadingProgress(0, 0L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
            `when`(serializer.deserialize("")).thenReturn(BackupManager.Data(emptyList(), emptyList(), emptyList(),
                    ReadingProgress(1, 2L, listOf(
                            ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                            ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                            ReadingProgress.ChapterReadingStatus(5, 6, 7, 2L, 1L)
                    ))))

            backupManager.restore("")
            verify(readingProgressManager, times(1)).save(ReadingProgress(1, 2L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
        }
    }

    @Test
    fun testRestoreWithEverything() {
        runBlocking {
            `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 3L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
            `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_YELLOW, 3L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
            `when`(noteManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Note(VerseIndex(0, 1, 2), "random note", 3L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
            `when`(readingProgressManager.read()).thenReturn(ReadingProgress(0, 0L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
            `when`(serializer.deserialize("")).thenReturn(
                    BackupManager.Data(
                            listOf(
                                    Bookmark(VerseIndex(0, 1, 2), 33L),
                                    Bookmark(VerseIndex(0, 1, 3), 456L),
                                    Bookmark(VerseIndex(4, 5, 6), 1L)
                            ),
                            listOf(
                                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_PURPLE, 1L)
                            ),
                            listOf(
                                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                                    Note(VerseIndex(4, 5, 6), "older random note", 1L)
                            ),
                            ReadingProgress(1, 2L, listOf(
                                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 2L, 1L)
                            ))
                    )
            )

            backupManager.restore("")
            verify(bookmarkManager, times(1)).save(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 33L),
                    Bookmark(VerseIndex(0, 1, 3), 456L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
            verify(highlightManager, times(1)).save(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
            verify(noteManager, times(1)).save(listOf(
                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
            verify(readingProgressManager, times(1)).save(ReadingProgress(1, 2L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
        }
    }
}
