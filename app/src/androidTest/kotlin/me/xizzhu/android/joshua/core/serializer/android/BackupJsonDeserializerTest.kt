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

import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupJsonDeserializerTest : BaseUnitTest() {
    private lateinit var deserializer: BackupJsonDeserializer

    @BeforeTest
    override fun setup() {
        super.setup()
        deserializer = BackupJsonDeserializer()
    }

    @Test
    fun testWithMinimumContent() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(1, 2L, emptyList())),
                deserializer.withContent("{\n" +
                        "  \"bookmarks\": [],\n" +
                        "  \"highlights\": [],\n" +
                        "  \"notes\": [],\n" +
                        "  \"readingProgress\": {\n" +
                        "    \"continuousReadingDays\": 1,\n" +
                        "    \"lastReadingTimestamp\": 2,\n" +
                        "    \"chapterReadingStatus\": []\n" +
                        "  },\n" +
                        "  \"someUnknownField\": \"Not known yet\"\n" +
                        "}").deserialize())
    }

    @Test
    fun testWithBookmarks() {
        assertEquals(
                BackupManager.Data(
                        listOf(
                                Bookmark(VerseIndex(1, 2, 3), 4567890L),
                                Bookmark(VerseIndex(9, 8, 7), 6543210L)
                        ),
                        emptyList(),
                        emptyList(),
                        ReadingProgress(1, 2L, emptyList())),
                deserializer.withContent("{\n" +
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
                        "}").deserialize())
    }

    @Test
    fun testWithInvalidBookmarks() {
        assertEquals(
                BackupManager.Data(
                        listOf(
                                Bookmark(VerseIndex(9, 8, 7), 6543210L)
                        ),
                        emptyList(),
                        emptyList(),
                        ReadingProgress(1, 2L, emptyList())),
                deserializer.withContent("{\n" +
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
                        "}").deserialize())
    }

    @Test
    fun testWithReadingProgress() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(),
                ReadingProgress(1, 2L, listOf(
                        ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L),
                        ReadingProgress.ChapterReadingStatus(9, 8, 7, 2L, 1L)
                ))),
                deserializer.withContent("{\n" +
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
                        "}").deserialize())
    }

    @Test
    fun testWithReadingProgressAndInvalidChapterReadingStatus() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(),
                ReadingProgress(1, 2L, listOf(
                        ReadingProgress.ChapterReadingStatus(3, 4, 5, 6L, 7L)
                ))),
                deserializer.withContent("{\n" +
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
                        "}").deserialize())
    }
}
