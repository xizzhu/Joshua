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

package me.xizzhu.android.joshua.reading.verse

import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VerseTransformerTest : BaseUnitTest() {
    @Test
    fun testToSimpleVerseItems() {
        val verses = MockContents.kjvVerses
        val actual = verses.toSimpleVerseItems(emptyList())
        assertEquals(verses.size, actual.size)
        actual.forEachIndexed { index, verseItem ->
            assertEquals(verses[index], verseItem.verse)
            assertEquals(Highlight.COLOR_NONE, verseItem.highlightColor)
        }
    }

    @Test
    fun testToSimpleVerseItemsWithFollowingEmptyVerse() {
        val verses = MockContents.msgVerses
        val actual = verses.toSimpleVerseItems(emptyList())
        assertTrue(verses.size > actual.size)

        var index = 0
        actual.forEach { verseItem ->
            while (index < verses.size) {
                if (verses[index] == verseItem.verse) {
                    break
                }
                index++
            }
            assertTrue(index < verses.size)

            assertEquals(Highlight.COLOR_NONE, verseItem.highlightColor)
        }
    }

    @Test
    fun testToSimpleVerseItemsWithFollowingEmptyVerseAndParallel() {
        val verses = MockContents.msgVersesWithKjvParallel
        val actual = verses.toSimpleVerseItems(emptyList())
        assertEquals(1, actual.size)
        assertEquals(
                Verse(
                        VerseIndex(0, 0, 0), verses[0].text,
                        listOf(Verse.Text(MockContents.kjvShortName, "${MockContents.msgVersesWithKjvParallel[0].parallel[0].text} ${MockContents.msgVersesWithKjvParallel[1].parallel[0].text}"))
                ),
                actual[0].verse)
    }

    @Test
    fun testToSimpleVerseItemsWithHighlights() {
        val verses = MockContents.kjvVerses
        val simpleVerseItems = verses.toSimpleVerseItems(
                listOf(
                        Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_PINK, 1L),
                        Highlight(VerseIndex(0, 0, 6), Highlight.COLOR_BLUE, 2L),
                        Highlight(VerseIndex(0, 0, 10), Highlight.COLOR_PURPLE, 3L)
                )
        )
        assertEquals(verses.size, simpleVerseItems.size)
        simpleVerseItems.forEachIndexed { index, verseItem ->
            assertEquals(verses[index], verseItem.verse)
            assertEquals(when (index) {
                0 -> Highlight.COLOR_PINK
                6 -> Highlight.COLOR_BLUE
                else -> Highlight.COLOR_NONE
            }, verseItem.highlightColor)
        }
    }

    @Test
    fun testToVerseItems() {
        val verses = MockContents.kjvVerses
        val verseItems = verses.toVerseItems(emptyList(), emptyList(), emptyList())
        assertEquals(verses.size, verseItems.size)
        verseItems.forEachIndexed { index, verseItem ->
            assertEquals(verses[index], verseItem.verse)
            assertFalse(verseItem.hasBookmark)
            assertEquals(Highlight.COLOR_NONE, verseItem.highlightColor)
            assertFalse(verseItem.hasNote)
        }
    }

    @Test
    fun testToVerseItemsWithBookmarksHighlightsNotes() {
        val verses = MockContents.kjvVerses
        val verseItems = verses.toVerseItems(
                listOf(
                        Bookmark(VerseIndex(0, 0, 0), 0L),
                        Bookmark(VerseIndex(0, 0, 5), 0L),
                        Bookmark(VerseIndex(0, 0, 10), 0L)
                ),
                listOf(
                        Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_PINK, 1L),
                        Highlight(VerseIndex(0, 0, 6), Highlight.COLOR_BLUE, 2L),
                        Highlight(VerseIndex(0, 0, 10), Highlight.COLOR_PURPLE, 3L)
                ),
                listOf(
                        Note(VerseIndex(0, 0, 0), "", 0L),
                        Note(VerseIndex(0, 0, 7), "", 0L),
                        Note(VerseIndex(0, 0, 10), "", 0L)
                )
        )
        assertEquals(verses.size, verseItems.size)
        verseItems.forEachIndexed { index, verseItem ->
            assertEquals(verses[index], verseItem.verse)
            assertTrue(if (index == 0 || index == 5) verseItem.hasBookmark else !verseItem.hasBookmark)
            assertEquals(when (index) {
                0 -> Highlight.COLOR_PINK
                6 -> Highlight.COLOR_BLUE
                else -> Highlight.COLOR_NONE
            }, verseItem.highlightColor)
            assertTrue(if (index == 0 || index == 7) verseItem.hasNote else !verseItem.hasNote)
        }
    }

    @Test
    fun testVerseToStringForSharing() {
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1 ${MockContents.kjvVerses[0].text.text}",
                MockContents.kjvVerses[0].toStringForSharing(MockContents.kjvBookNames[0])
        )
    }

    @Test
    fun testVerseWithParallelTranslationsToStringForSharing() {
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[0].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[0].text.text}",
                MockContents.kjvVersesWithCuvParallel[0].toStringForSharing(MockContents.kjvBookNames[0])
        )
    }

    @Test
    fun testVersesToStringForSharing() {
        // single verse
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1 ${MockContents.kjvVerses[0].text.text}",
                listOf(MockContents.kjvVerses[0]).toStringForSharing(MockContents.kjvBookNames[0], false)
        )

        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1 ${MockContents.kjvVerses[0].text.text}",
                listOf(MockContents.kjvVerses[0]).toStringForSharing(MockContents.kjvBookNames[0], true)
        )

        // multiple verses
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1 ${MockContents.kjvVerses[0].text.text}\n" +
                        "${MockContents.kjvBookNames[0]} 1:2 ${MockContents.kjvVerses[1].text.text}\n" +
                        "${MockContents.kjvBookNames[0]} 1:10 ${MockContents.kjvVerses[9].text.text}",
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[9])
                        .toStringForSharing(MockContents.kjvBookNames[0], false)
        )

        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1-2\n" +
                        "${MockContents.kjvVerses[0].text.text} ${MockContents.kjvVerses[1].text.text}\n\n" +
                        "${MockContents.kjvBookNames[0]} 1:10\n" +
                        MockContents.kjvVerses[9].text.text,
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[9])
                        .toStringForSharing(MockContents.kjvBookNames[0], true)
        )
    }

    @Test
    fun testVersesWithParallelTranslationsToStringForSharing() {
        // single verse
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[0].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[0].text.text}",
                listOf(MockContents.kjvVersesWithCuvParallel[0]).toStringForSharing(MockContents.kjvBookNames[0], false)
        )

        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[0].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[0].text.text}",
                listOf(MockContents.kjvVersesWithCuvParallel[0]).toStringForSharing(MockContents.kjvBookNames[0], true)
        )

        // multiple verses
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[0].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[0].text.text}\n" +
                        "${MockContents.kjvBookNames[0]} 1:2\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[1].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[1].text.text}",
                listOf(MockContents.kjvVersesWithCuvParallel[0], MockContents.kjvVersesWithCuvParallel[1])
                        .toStringForSharing(MockContents.kjvBookNames[0], false)
        )

        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1-2\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[0].text.text} ${MockContents.kjvVerses[1].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[0].text.text} ${MockContents.cuvVerses[1].text.text}",
                listOf(MockContents.kjvVersesWithCuvParallel[0], MockContents.kjvVersesWithCuvParallel[1])
                        .toStringForSharing(MockContents.kjvBookNames[0], true)
        )
    }

    @Test
    fun testVersesWithRandomOrderToStringForSharing() {
        // without parallel
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1 ${MockContents.kjvVerses[0].text.text}\n" +
                        "${MockContents.kjvBookNames[0]} 1:2 ${MockContents.kjvVerses[1].text.text}\n" +
                        "${MockContents.kjvBookNames[0]} 1:10 ${MockContents.kjvVerses[9].text.text}",
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[9], MockContents.kjvVerses[1])
                        .toStringForSharing(MockContents.kjvBookNames[0], false)
        )

        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1-2\n" +
                        "${MockContents.kjvVerses[0].text.text} ${MockContents.kjvVerses[1].text.text}\n\n" +
                        "${MockContents.kjvBookNames[0]} 1:10\n" +
                        MockContents.kjvVerses[9].text.text,
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[9], MockContents.kjvVerses[1])
                        .toStringForSharing(MockContents.kjvBookNames[0], true)
        )

        // with parallel
        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[0].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[0].text.text}\n" +
                        "${MockContents.kjvBookNames[0]} 1:2\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[1].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[1].text.text}",
                listOf(MockContents.kjvVersesWithCuvParallel[1], MockContents.kjvVersesWithCuvParallel[0])
                        .toStringForSharing(MockContents.kjvBookNames[0], false)
        )

        assertEquals(
                "${MockContents.kjvBookNames[0]} 1:1-2\n" +
                        "${MockContents.kjvShortName}: ${MockContents.kjvVerses[0].text.text} ${MockContents.kjvVerses[1].text.text}\n" +
                        "${MockContents.cuvShortName}: ${MockContents.cuvVerses[0].text.text} ${MockContents.cuvVerses[1].text.text}",
                listOf(MockContents.kjvVersesWithCuvParallel[1], MockContents.kjvVersesWithCuvParallel[0])
                        .toStringForSharing(MockContents.kjvBookNames[0], true)
        )
    }
}
