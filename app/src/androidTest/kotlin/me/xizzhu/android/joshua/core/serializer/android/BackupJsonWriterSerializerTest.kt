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

import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.VerseIndex
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
}
