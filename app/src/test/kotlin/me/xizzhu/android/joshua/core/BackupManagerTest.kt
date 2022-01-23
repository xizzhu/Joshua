/*
 * Copyright (C) 2022 Xizhi Zhu
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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.joshua.core.repository.VerseAnnotationRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackupManagerTest : BaseUnitTest() {
    private lateinit var serializer: BackupManager.Serializer
    private lateinit var bookmarkRepository: VerseAnnotationRepository<Bookmark>
    private lateinit var highlightRepository: VerseAnnotationRepository<Highlight>
    private lateinit var noteRepository: VerseAnnotationRepository<Note>
    private lateinit var readingProgressRepository: ReadingProgressRepository
    private lateinit var backupManager: BackupManager

    @BeforeTest
    override fun setup() {
        super.setup()

        serializer = mockk()
        bookmarkRepository = mockk()
        coEvery { bookmarkRepository.read(Constants.SORT_BY_BOOK) } returns emptyList()
        coEvery { bookmarkRepository.save(any() as List<Bookmark>) } returns Unit
        highlightRepository = mockk()
        coEvery { highlightRepository.read(Constants.SORT_BY_BOOK) } returns emptyList()
        coEvery { highlightRepository.save(any() as List<Highlight>) } returns Unit
        noteRepository = mockk()
        coEvery { noteRepository.read(Constants.SORT_BY_BOOK) } returns emptyList()
        coEvery { noteRepository.save(any() as List<Note>) } returns Unit
        readingProgressRepository = mockk()
        coEvery { readingProgressRepository.read() } returns ReadingProgress(0, 0L, emptyList())
        coEvery { readingProgressRepository.save(any()) } returns Unit

        backupManager = BackupManager(serializer, bookmarkRepository, highlightRepository, noteRepository, readingProgressRepository)
    }

    @Test
    fun `test prepareForBackup()`() = runTest {
        every { serializer.serialize(any()) } returns "random value"
        assertEquals("random value", backupManager.prepareForBackup())
    }

    @Test(expected = RuntimeException::class)
    fun `test prepareForBackup() with exception`(): Unit = runTest {
        every { serializer.serialize(any()) } throws RuntimeException("Random exception")

        backupManager.prepareForBackup()
        fail()
    }

    @Test(expected = RuntimeException::class)
    fun `test prepareForBackup() with failure in async operations`(): Unit = runTest {
        coEvery { bookmarkRepository.read(Constants.SORT_BY_BOOK) } throws RuntimeException("Random exception")

        backupManager.prepareForBackup()
        fail()
    }

    @Test
    fun `test restore() with minimum content`() = runTest {
        every { serializer.deserialize("") } returns BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList()))
        coEvery { readingProgressRepository.save(ReadingProgress(1, 2L, emptyList())) } returns Unit

        backupManager.restore("")
        coVerify(exactly = 1) {
            bookmarkRepository.save(emptyList())
            highlightRepository.save(emptyList())
            noteRepository.save(emptyList())
            readingProgressRepository.save(ReadingProgress(1, 2L, emptyList()))
        }
    }

    @Test
    fun `test restore() with bookmarks`() = runTest {
        coEvery { bookmarkRepository.read(Constants.SORT_BY_BOOK) } returns listOf(
                Bookmark(VerseIndex(0, 1, 2), 3L),
                Bookmark(VerseIndex(4, 5, 6), 7L)
        )
        every { serializer.deserialize("") } returns BackupManager.Data(
                listOf(
                        Bookmark(VerseIndex(0, 1, 2), 33L),
                        Bookmark(VerseIndex(0, 1, 3), 456L),
                        Bookmark(VerseIndex(4, 5, 6), 1L)
                ),
                emptyList(),
                emptyList(),
                ReadingProgress(0, 0L, emptyList())
        )

        backupManager.restore("")
        coVerify(exactly = 1) {
            bookmarkRepository.save(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 33L),
                    Bookmark(VerseIndex(0, 1, 3), 456L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
            highlightRepository.save(emptyList())
            noteRepository.save(emptyList())
            readingProgressRepository.save(ReadingProgress(0, 0L, emptyList()))
        }
    }

    @Test
    fun `test restore() with highlights`() = runTest {
        coEvery { highlightRepository.read(Constants.SORT_BY_BOOK) } returns listOf(
                Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_YELLOW, 3L),
                Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
        )
        every { serializer.deserialize("") } returns BackupManager.Data(
                emptyList(),
                listOf(
                        Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                        Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                        Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_PURPLE, 1L)
                ),
                emptyList(),
                ReadingProgress(0, 0L, emptyList())
        )

        backupManager.restore("")
        coVerify(exactly = 1) {
            bookmarkRepository.save(emptyList())
            highlightRepository.save(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
            noteRepository.save(emptyList())
            readingProgressRepository.save(ReadingProgress(0, 0L, emptyList()))
        }
    }

    @Test
    fun `test restore() with notes`() = runTest {
        coEvery { noteRepository.read(Constants.SORT_BY_BOOK) } returns listOf(
                Note(VerseIndex(0, 1, 2), "random note", 3L),
                Note(VerseIndex(4, 5, 6), "random note 2", 7L)
        )
        every { serializer.deserialize("") } returns BackupManager.Data(
                emptyList(),
                emptyList(),
                listOf(
                        Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                        Note(VerseIndex(0, 1, 3), "another random note", 456L),
                        Note(VerseIndex(4, 5, 6), "older random note", 1L)
                ),
                ReadingProgress(0, 0L, emptyList())
        )

        backupManager.restore("")
        coVerify(exactly = 1) {
            bookmarkRepository.save(emptyList())
            highlightRepository.save(emptyList())
            noteRepository.save(listOf(
                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
            readingProgressRepository.save(ReadingProgress(0, 0L, emptyList()))
        }
    }

    @Test
    fun `test restore() with reading progress`() = runTest {
        coEvery { readingProgressRepository.read() } returns ReadingProgress(
                continuousReadingDays = 0,
                lastReadingTimestamp = 0L,
                chapterReadingStatus = listOf(
                        ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                        ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
                )
        )
        every { serializer.deserialize("") } returns BackupManager.Data(
                emptyList(),
                emptyList(),
                emptyList(),
                ReadingProgress(
                        continuousReadingDays = 1,
                        lastReadingTimestamp = 2L,
                        chapterReadingStatus = listOf(
                                ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                                ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                                ReadingProgress.ChapterReadingStatus(5, 6, 7, 2L, 1L)
                        )
                )
        )

        backupManager.restore("")
        coVerify(exactly = 1) {
            bookmarkRepository.save(emptyList())
            highlightRepository.save(emptyList())
            noteRepository.save(emptyList())
            readingProgressRepository.save(ReadingProgress(
                    continuousReadingDays = 1,
                    lastReadingTimestamp = 2L,
                    chapterReadingStatus = listOf(
                            ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                            ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                            ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
                    )
            ))
        }
    }

    @Test
    fun `test restore()`() = runTest {
        coEvery { bookmarkRepository.read(Constants.SORT_BY_BOOK) } returns listOf(
                Bookmark(VerseIndex(0, 1, 2), 3L),
                Bookmark(VerseIndex(4, 5, 6), 7L)
        )
        coEvery { highlightRepository.read(Constants.SORT_BY_BOOK) } returns listOf(
                Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_YELLOW, 3L),
                Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
        )
        coEvery { noteRepository.read(Constants.SORT_BY_BOOK) } returns listOf(
                Note(VerseIndex(0, 1, 2), "random note", 3L),
                Note(VerseIndex(4, 5, 6), "random note 2", 7L)
        )
        coEvery { readingProgressRepository.read() } returns ReadingProgress(0, 0L, listOf(
                ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 4L),
                ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
        ))
        every { serializer.deserialize("") } returns BackupManager.Data(
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
                ReadingProgress(
                        continuousReadingDays = 1,
                        lastReadingTimestamp = 2L,
                        chapterReadingStatus = listOf(
                                ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                                ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                                ReadingProgress.ChapterReadingStatus(5, 6, 7, 2L, 1L)
                        )
                )
        )

        backupManager.restore("")
        coVerify(exactly = 1) {
            bookmarkRepository.save(listOf(
                    Bookmark(VerseIndex(0, 1, 2), 33L),
                    Bookmark(VerseIndex(0, 1, 3), 456L),
                    Bookmark(VerseIndex(4, 5, 6), 7L)
            ))
            highlightRepository.save(listOf(
                    Highlight(VerseIndex(0, 1, 2), Highlight.COLOR_GREEN, 33L),
                    Highlight(VerseIndex(0, 1, 3), Highlight.COLOR_PINK, 456L),
                    Highlight(VerseIndex(4, 5, 6), Highlight.COLOR_BLUE, 7L)
            ))
            noteRepository.save(listOf(
                    Note(VerseIndex(0, 1, 2), "newer random note", 33L),
                    Note(VerseIndex(0, 1, 3), "another random note", 456L),
                    Note(VerseIndex(4, 5, 6), "random note 2", 7L)
            ))
            readingProgressRepository.save(ReadingProgress(
                    continuousReadingDays = 1,
                    lastReadingTimestamp = 2L,
                    chapterReadingStatus = listOf(
                            ReadingProgress.ChapterReadingStatus(0, 1, 2, 3L, 55L),
                            ReadingProgress.ChapterReadingStatus(1, 2, 3, 4L, 5L),
                            ReadingProgress.ChapterReadingStatus(5, 6, 7, 8L, 9L)
                    )
            ))
        }
    }
}
