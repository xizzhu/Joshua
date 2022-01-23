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

package me.xizzhu.android.joshua.core.serializer.android

import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class BackupJsonSerializerTest : BaseUnitTest() {
    private lateinit var serializer: BackupJsonSerializer

    @BeforeTest
    override fun setup() {
        super.setup()
        serializer = BackupJsonSerializer()
    }

    @Test
    fun testWithEmptyData() {
        assertEquals("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 0,\n" +
                "    \"lastReadingTimestamp\": 0,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                emptyList(),
                                emptyList(),
                                emptyList(),
                                ReadingProgress(0, 0L, emptyList())
                        )
                )
        )
    }

    @Test
    fun testWithSingleBookmark() {
        assertEquals("{\n" +
                "  \"bookmarks\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"timestamp\": 4567890\n" +
                "    }\n" +
                "  ],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 0,\n" +
                "    \"lastReadingTimestamp\": 0,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                listOf(
                                        Bookmark(VerseIndex(1, 2, 3), 4567890L)
                                ),
                                emptyList(),
                                emptyList(),
                                ReadingProgress(0, 0L, emptyList())
                        )
                )
        )
    }

    @Test
    fun testWithBookmarks() {
        assertEquals("{\n" +
                "  \"bookmarks\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"timestamp\": 4567890\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookIndex\": 9,\n" +
                "      \"chapterIndex\": 8,\n" +
                "      \"verseIndex\": 7,\n" +
                "      \"timestamp\": 6543210\n" +
                "    }\n" +
                "  ],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 0,\n" +
                "    \"lastReadingTimestamp\": 0,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                listOf(
                                        Bookmark(VerseIndex(1, 2, 3), 4567890L),
                                        Bookmark(VerseIndex(9, 8, 7), 6543210L)
                                ),
                                emptyList(),
                                emptyList(),
                                ReadingProgress(0, 0L, emptyList())
                        )
                )
        )
    }

    @Test
    fun testWithSingleHighlight() {
        assertEquals("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"color\": ${Highlight.COLOR_BLUE},\n" +
                "      \"timestamp\": 4567890\n" +
                "    }\n" +
                "  ],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 0,\n" +
                "    \"lastReadingTimestamp\": 0,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                emptyList(),
                                listOf(
                                        Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L)
                                ),
                                emptyList(),
                                ReadingProgress(0, 0L, emptyList())
                        )
                )
        )
    }

    @Test
    fun testWithHighlights() {
        assertEquals("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"color\": ${Highlight.COLOR_BLUE},\n" +
                "      \"timestamp\": 4567890\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookIndex\": 9,\n" +
                "      \"chapterIndex\": 8,\n" +
                "      \"verseIndex\": 7,\n" +
                "      \"color\": ${Highlight.COLOR_YELLOW},\n" +
                "      \"timestamp\": 6543210\n" +
                "    }\n" +
                "  ],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 0,\n" +
                "    \"lastReadingTimestamp\": 0,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                emptyList(),
                                listOf(
                                        Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L),
                                        Highlight(VerseIndex(9, 8, 7), Highlight.COLOR_YELLOW, 6543210L)
                                ),
                                emptyList(),
                                ReadingProgress(0, 0L, emptyList())
                        )
                )
        )
    }

    @Test
    fun testWithSingleNote() {
        assertEquals("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"note\": \"random note\",\n" +
                "      \"timestamp\": 4567890\n" +
                "    }\n" +
                "  ],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 0,\n" +
                "    \"lastReadingTimestamp\": 0,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                emptyList(),
                                emptyList(),
                                listOf(
                                        Note(VerseIndex(1, 2, 3), "random note", 4567890L)
                                ),
                                ReadingProgress(0, 0L, emptyList())
                        )
                )
        )
    }

    @Test
    fun testWithNotes() {
        assertEquals("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"note\": \"random note\",\n" +
                "      \"timestamp\": 4567890\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookIndex\": 9,\n" +
                "      \"chapterIndex\": 8,\n" +
                "      \"verseIndex\": 7,\n" +
                "      \"note\": \"yet another note\",\n" +
                "      \"timestamp\": 6543210\n" +
                "    }\n" +
                "  ],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 0,\n" +
                "    \"lastReadingTimestamp\": 0,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                emptyList(),
                                emptyList(),
                                listOf(
                                        Note(VerseIndex(1, 2, 3), "random note", 4567890L),
                                        Note(VerseIndex(9, 8, 7), "yet another note", 6543210L)
                                ),
                                ReadingProgress(0, 0L, emptyList())
                        )
                )
        )
    }

    @Test
    fun testWithSingleReadingProgress() {
        assertEquals("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 1,\n" +
                "    \"lastReadingTimestamp\": 2,\n" +
                "    \"chapterReadingStatus\": [\n" +
                "      {\n" +
                "        \"bookIndex\": 3,\n" +
                "        \"chapterIndex\": 4,\n" +
                "        \"readCount\": 5,\n" +
                "        \"timeSpentInMillis\": 6,\n" +
                "        \"lastReadingTimestamp\": 7\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                emptyList(),
                                emptyList(),
                                emptyList(),
                                ReadingProgress(1, 2L,
                                        listOf(
                                                ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L)
                                        )
                                )
                        )
                )
        )
    }

    @Test
    fun testWithReadingProgress() {
        assertEquals("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 1,\n" +
                "    \"lastReadingTimestamp\": 2,\n" +
                "    \"chapterReadingStatus\": [\n" +
                "      {\n" +
                "        \"bookIndex\": 3,\n" +
                "        \"chapterIndex\": 4,\n" +
                "        \"readCount\": 5,\n" +
                "        \"timeSpentInMillis\": 6,\n" +
                "        \"lastReadingTimestamp\": 7\n" +
                "      },\n" +
                "      {\n" +
                "        \"bookIndex\": 9,\n" +
                "        \"chapterIndex\": 8,\n" +
                "        \"readCount\": 7,\n" +
                "        \"timeSpentInMillis\": 2,\n" +
                "        \"lastReadingTimestamp\": 1\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                emptyList(),
                                emptyList(),
                                emptyList(),
                                ReadingProgress(1, 2L,
                                        listOf(
                                                ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                                                ReadingProgress.ChapterReadingStatus(9, 8, 7, 2L, 1L)
                                        )
                                )
                        )
                )
        )
    }

    @Test
    fun testWithEverything() {
        assertEquals("{\n" +
                "  \"bookmarks\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"timestamp\": 4567890\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookIndex\": 9,\n" +
                "      \"chapterIndex\": 8,\n" +
                "      \"verseIndex\": 7,\n" +
                "      \"timestamp\": 6543210\n" +
                "    }\n" +
                "  ],\n" +
                "  \"highlights\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"color\": ${Highlight.COLOR_BLUE},\n" +
                "      \"timestamp\": 4567890\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookIndex\": 9,\n" +
                "      \"chapterIndex\": 8,\n" +
                "      \"verseIndex\": 7,\n" +
                "      \"color\": ${Highlight.COLOR_YELLOW},\n" +
                "      \"timestamp\": 6543210\n" +
                "    }\n" +
                "  ],\n" +
                "  \"notes\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"note\": \"random note\",\n" +
                "      \"timestamp\": 4567890\n" +
                "    },\n" +
                "    {\n" +
                "      \"bookIndex\": 9,\n" +
                "      \"chapterIndex\": 8,\n" +
                "      \"verseIndex\": 7,\n" +
                "      \"note\": \"yet another note\",\n" +
                "      \"timestamp\": 6543210\n" +
                "    }\n" +
                "  ],\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 1,\n" +
                "    \"lastReadingTimestamp\": 2,\n" +
                "    \"chapterReadingStatus\": [\n" +
                "      {\n" +
                "        \"bookIndex\": 3,\n" +
                "        \"chapterIndex\": 4,\n" +
                "        \"readCount\": 5,\n" +
                "        \"timeSpentInMillis\": 6,\n" +
                "        \"lastReadingTimestamp\": 7\n" +
                "      },\n" +
                "      {\n" +
                "        \"bookIndex\": 9,\n" +
                "        \"chapterIndex\": 8,\n" +
                "        \"readCount\": 7,\n" +
                "        \"timeSpentInMillis\": 2,\n" +
                "        \"lastReadingTimestamp\": 1\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}",
                serializer.serialize(
                        BackupManager.Data(
                                listOf(
                                        Bookmark(VerseIndex(1, 2, 3), 4567890L),
                                        Bookmark(VerseIndex(9, 8, 7), 6543210L)
                                ),
                                listOf(
                                        Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L),
                                        Highlight(VerseIndex(9, 8, 7), Highlight.COLOR_YELLOW, 6543210L)
                                ),
                                listOf(
                                        Note(VerseIndex(1, 2, 3), "random note", 4567890L),
                                        Note(VerseIndex(9, 8, 7), "yet another note", 6543210L)
                                ),
                                ReadingProgress(1, 2L,
                                        listOf(
                                                ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                                                ReadingProgress.ChapterReadingStatus(9, 8, 7, 2L, 1L)
                                        )
                                )
                        )
                )
        )
    }

    @Test(expected = IllegalStateException::class)
    fun testDeserializeWithoutBookmarks() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  },\n" +
                        "  \"someUnknownField\": \"Not known yet\"\n" +
                        "}"))
    }

    @Test(expected = IllegalStateException::class)
    fun testDeserializeWithoutHighlights() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  },\n" +
                        "  \"someUnknownField\": \"Not known yet\"\n" +
                        "}"))
    }

    @Test(expected = IllegalStateException::class)
    fun testDeserializeWithoutNotes() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  },\n" +
                        "  \"someUnknownField\": \"Not known yet\"\n" +
                        "}"))
    }

    @Test(expected = IllegalStateException::class)
    fun testDeserializeWithoutReadingProgress() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"someUnknownField\": \"Not known yet\"\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithMinimumContent() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  },\n" +
                        "  \"someUnknownField\": \"Not known yet\"\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithBookmarks() {
        assertEquals(
                BackupManager.Data(
                        listOf(
                                Bookmark(VerseIndex(1, 2, 3), 4567890L),
                                Bookmark(VerseIndex(9, 8, 7), 6543210L)
                        ),
                        emptyList(),
                        emptyList(),
                        ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"timestamp\": 4567890,\n" +
                        "      \"someRandomUnknownField\": 98765\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"timestamp\": 6543210\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithInvalidBookmarks() {
        assertEquals(
                BackupManager.Data(
                        listOf(
                                Bookmark(VerseIndex(9, 8, 7), 6543210L)
                        ),
                        emptyList(),
                        emptyList(),
                        ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [\n" +
                        "    {\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"timestamp\": 4567890,\n" +
                        "      \"someRandomUnknownField\": 98765\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"timestamp\": 6543210\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithHighlights() {
        assertEquals(
                BackupManager.Data(
                        emptyList(),
                        listOf(
                                Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L),
                                Highlight(VerseIndex(9, 8, 7), Highlight.COLOR_YELLOW, 6543210L)
                        ),
                        emptyList(),
                        ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"color\": ${Highlight.COLOR_BLUE},\n" +
                        "      \"timestamp\": 4567890,\n" +
                        "      \"someRandomUnknownField\": 98765\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"color\": ${Highlight.COLOR_YELLOW},\n" +
                        "      \"timestamp\": 6543210\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithInvalidHighlights() {
        assertEquals(
                BackupManager.Data(
                        emptyList(),
                        listOf(
                                Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L)
                        ),
                        emptyList(),
                        ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"color\": ${Highlight.COLOR_BLUE},\n" +
                        "      \"timestamp\": 4567890,\n" +
                        "      \"someRandomUnknownField\": 98765\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"color\": ${Highlight.COLOR_YELLOW}\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithNotes() {
        assertEquals(
                BackupManager.Data(
                        emptyList(),
                        emptyList(),
                        listOf(
                                Note(VerseIndex(1, 2, 3), "random note", 4567890L),
                                Note(VerseIndex(9, 8, 7), "yet another note", 6543210L)
                        ),
                        ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"note\": \"random note\",\n" +
                        "      \"timestamp\": 4567890,\n" +
                        "      \"someRandomUnknownField\": 98765\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"note\": \"yet another note\",\n" +
                        "      \"timestamp\": 6543210\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithInvalidNotes() {
        assertEquals(
                BackupManager.Data(
                        emptyList(),
                        emptyList(),
                        listOf(
                                Note(VerseIndex(1, 2, 3), "random note", 4567890L)
                        ),
                        ReadingProgress(1, 2L, emptyList())),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"note\": \"random note\",\n" +
                        "      \"timestamp\": 4567890,\n" +
                        "      \"someRandomUnknownField\": 98765\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"note\": \"yet another note\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithReadingProgress() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(),
                ReadingProgress(1, 2L, listOf(
                        ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                        ReadingProgress.ChapterReadingStatus(9, 8, 7, 2L, 1L)
                ))),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": [\n" +
                        "      {\n" +
                        "        \"bookIndex\": 3,\n" +
                        "        \"chapterIndex\": 4,\n" +
                        "        \"readCount\": 5,\n" +
                        "        \"timeSpentInMillis\": 6,\n" +
                        "        \"lastReadingTimestamp\": 7,\n" +
                        "        \"someRandomUnknownField\": 890\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"bookIndex\": 9,\n" +
                        "        \"chapterIndex\": 8,\n" +
                        "        \"readCount\": 7,\n" +
                        "        \"timeSpentInMillis\": 2,\n" +
                        "        \"lastReadingTimestamp\": 1\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"someUnknownField\": \"Known or unknown?\"\n" +
                        "  }\n" +
                        "}"))
    }

    @Test(expected = IllegalStateException::class)
    fun testDeserializeWithInvalidReadingProgress() {
        serializer.deserialize("{\n" +
                "  \"bookmarks\": [],\n" +
                "  \"highlights\": [],\n" +
                "  \"notes\": [],\n" +
                "  \"readingProgress\": {\n" +
                "    \"lastReadingTimestamp\": 2,\n" +
                "    \"chapterReadingStatus\": [\n" +
                "      {\n" +
                "        \"bookIndex\": 3,\n" +
                "        \"chapterIndex\": 4,\n" +
                "        \"readCount\": 5,\n" +
                "        \"timeSpentInMillis\": 6,\n" +
                "        \"lastReadingTimestamp\": 7,\n" +
                "        \"someRandomUnknownField\": 890\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}")
    }

    @Test
    fun testDeserializeWithReadingProgressAndInvalidChapterReadingStatus() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(),
                ReadingProgress(1, 2L, listOf(
                        ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L)
                ))),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": [\n" +
                        "      {\n" +
                        "        \"bookIndex\": 3,\n" +
                        "        \"chapterIndex\": 4,\n" +
                        "        \"readCount\": 5,\n" +
                        "        \"timeSpentInMillis\": 6,\n" +
                        "        \"lastReadingTimestamp\": 7,\n" +
                        "        \"someRandomUnknownField\": 890\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"bookIndex\": 9,\n" +
                        "        \"chapterIndex\": 8,\n" +
                        "        \"readCount\": 7,\n" +
                        "        \"timeSpentInMillis\": 2\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testDeserializeWithEverything() {
        assertEquals(
                BackupManager.Data(
                        listOf(
                                Bookmark(VerseIndex(1, 2, 3), 4567890L),
                                Bookmark(VerseIndex(9, 8, 7), 6543210L)
                        ),
                        listOf(
                                Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L),
                                Highlight(VerseIndex(9, 8, 7), Highlight.COLOR_YELLOW, 6543210L)
                        ),
                        listOf(
                                Note(VerseIndex(1, 2, 3), "random note", 4567890L),
                                Note(VerseIndex(9, 8, 7), "yet another note", 6543210L)
                        ),
                        ReadingProgress(1, 2L, listOf(
                                ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                                ReadingProgress.ChapterReadingStatus(9, 8, 7, 2L, 1L)
                        ))
                ),
                serializer.deserialize("{\n" +
                        "  \"bookmarks\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"timestamp\": 4567890\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"timestamp\": 6543210\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"highlights\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"color\": ${Highlight.COLOR_BLUE},\n" +
                        "      \"timestamp\": 4567890\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"color\": ${Highlight.COLOR_YELLOW},\n" +
                        "      \"timestamp\": 6543210\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"notes\": [\n" +
                        "    {\n" +
                        "      \"bookIndex\": 1,\n" +
                        "      \"chapterIndex\": 2,\n" +
                        "      \"verseIndex\": 3,\n" +
                        "      \"note\": \"random note\",\n" +
                        "      \"timestamp\": 4567890\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bookIndex\": 9,\n" +
                        "      \"chapterIndex\": 8,\n" +
                        "      \"verseIndex\": 7,\n" +
                        "      \"note\": \"yet another note\",\n" +
                        "      \"timestamp\": 6543210\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": [\n" +
                        "      {\n" +
                        "        \"bookIndex\": 3,\n" +
                        "        \"chapterIndex\": 4,\n" +
                        "        \"readCount\": 5,\n" +
                        "        \"timeSpentInMillis\": 6,\n" +
                        "        \"lastReadingTimestamp\": 7\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"bookIndex\": 9,\n" +
                        "        \"chapterIndex\": 8,\n" +
                        "        \"readCount\": 7,\n" +
                        "        \"timeSpentInMillis\": 2,\n" +
                        "        \"lastReadingTimestamp\": 1\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}"))
    }

    @Test
    fun testSerializeThenDeserialize() {
        val data = BackupManager.Data(
                listOf(
                        Bookmark(VerseIndex(1, 2, 3), 4567890L),
                        Bookmark(VerseIndex(9, 8, 7), 6543210L)
                ),
                listOf(
                        Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L),
                        Highlight(VerseIndex(9, 8, 7), Highlight.COLOR_YELLOW, 6543210L)
                ),
                listOf(
                        Note(VerseIndex(1, 2, 3), "random note", 4567890L),
                        Note(VerseIndex(9, 8, 7), "yet another note", 6543210L)
                ),
                ReadingProgress(1, 2L,
                        listOf(
                                ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                                ReadingProgress.ChapterReadingStatus(9, 8, 7, 2L, 1L)
                        )
                )
        )
        assertEquals(data, serializer.deserialize(serializer.serialize(data)))
    }
}
