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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.joshua.core.repository.VerseAnnotationRepository
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
    private lateinit var bookmarkRepository: VerseAnnotationRepository<Bookmark>
    @Mock
    private lateinit var highlightRepository: VerseAnnotationRepository<Highlight>
    @Mock
    private lateinit var noteRepository: VerseAnnotationRepository<Note>
    @Mock
    private lateinit var readingProgressRepository: ReadingProgressRepository

    private lateinit var backupManager: BackupManager

    @BeforeTest
    override fun setup() {
        super.setup()

        runBlocking {
            `when`(bookmarkRepository.read(Constants.SORT_BY_BOOK)).thenReturn(emptyList())
            `when`(highlightRepository.read(Constants.SORT_BY_BOOK)).thenReturn(emptyList())
            `when`(noteRepository.read(Constants.SORT_BY_BOOK)).thenReturn(emptyList())
            `when`(readingProgressRepository.read()).thenReturn(ReadingProgress(0, 0L, emptyList()))
            backupManager = BackupManager(serializer, bookmarkRepository, highlightRepository, noteRepository, readingProgressRepository)
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
            `when`(bookmarkRepository.read(Constants.SORT_BY_BOOK)).thenThrow(RuntimeException("Random exception"))
            backupManager.prepareForBackup()
        }
    }

    @Test
    fun testRestoreWithMinimumContent() {
        runBlocking {
            `when`(serializer.deserialize("")).thenReturn(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())))
            backupManager.restore("")
            verify(readingProgressRepository, times(1)).save(ReadingProgress(1, 2L, emptyList()))
        }
    }

    @Test
    fun testRestoreWithBookmarks() {
        runBlocking {
            `when`(bookmarkRepository.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
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
            verify(bookmarkRepository, times(1)).save(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 33L),
                    Bookmark(VerseIndex(0, 1, 3), 456L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
        }
    }

    @Test
    fun testRestoreWithHighlights() {
        runBlocking {
            `when`(highlightRepository.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
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
            verify(highlightRepository, times(1)).save(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
        }
    }

    @Test
    fun testRestoreWithNotes() {
        runBlocking {
            `when`(noteRepository.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
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
            verify(noteRepository, times(1)).save(listOf(
                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
        }
    }

    @Test
    fun testRestoreWithReadingProgress() {
        runBlocking {
            `when`(readingProgressRepository.read()).thenReturn(ReadingProgress(0, 0L, listOf(
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
            verify(readingProgressRepository, times(1)).save(ReadingProgress(1, 2L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
        }
    }

    @Test
    fun testRestoreWithEverything() {
        runBlocking {
            `when`(bookmarkRepository.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 3L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
            `when`(highlightRepository.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_YELLOW, 3L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
            `when`(noteRepository.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Note(VerseIndex(0, 1, 2), "random note", 3L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
            `when`(readingProgressRepository.read()).thenReturn(ReadingProgress(0, 0L, listOf(
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
            verify(bookmarkRepository, times(1)).save(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 33L),
                    Bookmark(VerseIndex(0, 1, 3), 456L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
            verify(highlightRepository, times(1)).save(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
            verify(noteRepository, times(1)).save(listOf(
                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
            verify(readingProgressRepository, times(1)).save(ReadingProgress(1, 2L, listOf(
                    ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                    ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                    ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
            )))
        }
    }
}
