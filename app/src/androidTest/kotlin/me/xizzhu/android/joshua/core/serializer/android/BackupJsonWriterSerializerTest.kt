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

package me.xizzhu.android.joshua.core.serializer.android

import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupJsonWriterSerializerTest : BaseUnitTest() {
    private lateinit var serializer: BackupJsonWriterSerializer

    @BeforeTest
    override fun setup() {
        super.setup()
        serializer = BackupJsonWriterSerializer()
    }

    @Test
    fun testWithEmptyBookmarks() {
        assertEquals("{\n" +
                "  \"bookmarks\": []\n" +
                "}", serializer.withBookmarks(emptyList()).serialize())
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
                "  ]\n" +
                "}",
                serializer.withBookmarks(listOf(
                        Bookmark(VerseIndex(1, 2, 3), 4567890L)
                )).serialize())
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
                "  ]\n" +
                "}",
                serializer.withBookmarks(listOf(
                        Bookmark(VerseIndex(1, 2, 3), 4567890L),
                        Bookmark(VerseIndex(9, 8, 7), 6543210L)
                )).serialize())
    }

    @Test
    fun testWithEmptyHighlights() {
        assertEquals("{\n" +
                "  \"highlights\": []\n" +
                "}", serializer.withHighlights(emptyList()).serialize())
    }

    @Test
    fun testWithSingleHighlight() {
        assertEquals("{\n" +
                "  \"highlights\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"color\": ${Highlight.COLOR_BLUE},\n" +
                "      \"timestamp\": 4567890\n" +
                "    }\n" +
                "  ]\n" +
                "}",
                serializer.withHighlights(listOf(
                        Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L)
                )).serialize())
    }

    @Test
    fun testWithHighlights() {
        assertEquals("{\n" +
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
                "  ]\n" +
                "}",
                serializer.withHighlights(listOf(
                        Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 4567890L),
                        Highlight(VerseIndex(9, 8, 7), Highlight.COLOR_YELLOW, 6543210L)
                )).serialize())
    }

    @Test
    fun testWithEmptyNotes() {
        assertEquals("{\n" +
                "  \"notes\": []\n" +
                "}", serializer.withNotes(emptyList()).serialize())
    }

    @Test
    fun testWithSingleNote() {
        assertEquals("{\n" +
                "  \"notes\": [\n" +
                "    {\n" +
                "      \"bookIndex\": 1,\n" +
                "      \"chapterIndex\": 2,\n" +
                "      \"verseIndex\": 3,\n" +
                "      \"note\": \"random note\",\n" +
                "      \"timestamp\": 4567890\n" +
                "    }\n" +
                "  ]\n" +
                "}",
                serializer.withNotes(listOf(
                        Note(VerseIndex(1, 2, 3), "random note", 4567890L)
                )).serialize())
    }

    @Test
    fun testWithNotes() {
        assertEquals("{\n" +
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
                "  ]\n" +
                "}",
                serializer.withNotes(listOf(
                        Note(VerseIndex(1, 2, 3), "random note", 4567890L),
                        Note(VerseIndex(9, 8, 7), "yet another note", 6543210L)
                )).serialize())
    }

    @Test
    fun testWithEmptyReadingProgress() {
        assertEquals("{\n" +
                "  \"readingProgress\": {\n" +
                "    \"continuousReadingDays\": 1,\n" +
                "    \"lastReadingTimestamp\": 2,\n" +
                "    \"chapterReadingStatus\": []\n" +
                "  }\n" +
                "}", serializer.withReadingProgress(ReadingProgress(1, 2L, emptyList())).serialize())
    }

    @Test
    fun testWithSingleReadingProgress() {
        assertEquals("{\n" +
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
                serializer.withReadingProgress(ReadingProgress(1, 2L, listOf(
                        ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L)
                ))).serialize())
    }

    @Test
    fun testWithReadingProgress() {
        assertEquals("{\n" +
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
                serializer.withReadingProgress(ReadingProgress(1, 2L, listOf(
                        ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                        ReadingProgress.ChapterReadingStatus(9, 8, 7, 2L, 1L)
                ))).serialize())
    }
}
