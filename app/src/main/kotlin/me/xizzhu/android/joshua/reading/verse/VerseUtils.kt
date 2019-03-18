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

package me.xizzhu.android.joshua.reading.verse

import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import java.lang.StringBuilder

fun VerseIndex.toPagePosition(): Int = indexToPagePosition(bookIndex, chapterIndex)

fun indexToPagePosition(bookIndex: Int, chapterIndex: Int): Int {
    if (bookIndex < 0 || bookIndex >= Bible.BOOK_COUNT) {
        throw IllegalArgumentException("Invalid book index: $bookIndex")
    }
    if (chapterIndex < 0 || chapterIndex >= Bible.getChapterCount(bookIndex)) {
        throw IllegalArgumentException("Invalid chapter index: $chapterIndex")
    }

    var position = 0
    for (i in 0 until bookIndex) {
        position += Bible.getChapterCount(i)
    }
    return position + chapterIndex
}

fun Int.toBookIndex(): Int {
    var position = this
    if (position < 0 || position >= Bible.TOTAL_CHAPTER_COUNT) {
        throw IllegalArgumentException("Invalid position: $position")
    }

    for (bookIndex in 0 until Bible.BOOK_COUNT) {
        position -= Bible.getChapterCount(bookIndex)
        if (position < 0) {
            return bookIndex
        }
    }

    throw IllegalArgumentException("Invalid position: $position")
}

fun Int.toChapterIndex(): Int {
    var position = this
    if (position < 0 || position >= Bible.TOTAL_CHAPTER_COUNT) {
        throw IllegalArgumentException("Invalid position: $position")
    }

    for (bookIndex in 0 until Bible.BOOK_COUNT) {
        val chapterCount = Bible.getChapterCount(bookIndex)
        if (position < chapterCount) {
            return position
        }
        position -= chapterCount
    }

    throw IllegalArgumentException("Invalid position: $position")
}

fun Collection<Verse>.toStringForSharing(): String {
    val stringBuilder = StringBuilder()
    for (verse in sortedBy { verse ->
        val verseIndex = verse.verseIndex
        verseIndex.bookIndex * 100000 + verseIndex.chapterIndex * 1000 + verseIndex.verseIndex
    }) {
        if (stringBuilder.isNotEmpty()) {
            stringBuilder.append('\n')
        }
        if (verse.parallel.isEmpty()) {
            // format: <book name> <chapter index>:<verse index> <text>
            stringBuilder.append(verse.text.bookName).append(' ')
                    .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1).append(' ')
                    .append(verse.text.text)
        } else {
            // format:
            // <book name> <chapter verseIndex>:<verse verseIndex>
            // <primary translation>: <verse text>
            // <parallel translation 1>: <verse text>
            // <parallel translation 2>: <verse text>
            stringBuilder.append(verse.text.bookName).append(' ')
                    .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1).append('\n')
                    .append(verse.text.translationShortName).append(": ").append(verse.text.text).append('\n')
            for (text in verse.parallel) {
                stringBuilder.append(text.translationShortName).append(": ").append(text.text).append('\n')
            }
            stringBuilder.setLength(stringBuilder.length - 1)
        }
    }
    return stringBuilder.toString()
}
