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
import me.xizzhu.android.joshua.core.VerseIndex

fun VerseIndex.toPagePosition(): Int = indexToPagePosition(bookIndex, chapterIndex)

fun indexToPagePosition(bookIndex: Int, chapterIndex: Int): Int {
    if (bookIndex < 0 || chapterIndex < 0) {
        return -1
    }

    var position = 0
    for (i in 0 until bookIndex) {
        position += Bible.getChapterCount(i)
    }
    return position + chapterIndex
}

fun pagePositionToBookIndex(position: Int): Int {
    if (position < 0) {
        throw IllegalArgumentException("Invalid position: $position")
    }

    var pos = position
    for (bookIndex in 0 until Bible.BOOK_COUNT) {
        pos -= Bible.getChapterCount(bookIndex)
        if (pos < 0) {
            return bookIndex
        }
    }

    throw IllegalArgumentException("Invalid position: $position")
}

fun pagePositionToChapterIndex(position: Int): Int {
    if (position < 0) {
        throw IllegalArgumentException("Invalid position: $position")
    }

    var pos = position
    for (bookIndex in 0 until Bible.BOOK_COUNT) {
        val chapterCount = Bible.getChapterCount(bookIndex)
        if (pos < chapterCount) {
            return pos
        }
        pos -= chapterCount
    }

    throw IllegalArgumentException("Invalid position: $position")
}
