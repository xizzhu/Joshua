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

package me.xizzhu.android.joshua.progress

import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.ReadingProgress

data class ReadingProgressForDisplay(val continuousReadingDays: Int, val chaptersRead: Int,
                                     val finishedBooks: Int, val finishedOldTestament: Int,
                                     val finishedNewTestament: Int, val bookReadingStatus: List<BookReadingStatus>) {
    data class BookReadingStatus(val bookName: String, val chaptersRead: Int, val chaptersCount: Int)
}

fun ReadingProgress.toReadingProgressForDisplay(bookNames: List<String>): ReadingProgressForDisplay {
    var totalChaptersRead = 0
    val chaptersReadPerBook = Array(Bible.BOOK_COUNT) { 0 }
    for (chapter in chapterReadingStatus) {
        chaptersReadPerBook[chapter.bookIndex]++
        ++totalChaptersRead
    }

    var finishedBooks = 0
    var finishedOldTestament = 0
    var finishedNewTestament = 0
    val bookReadingStatus = ArrayList<ReadingProgressForDisplay.BookReadingStatus>(Bible.BOOK_COUNT)
    for ((bookIndex, chaptersRead) in chaptersReadPerBook.withIndex()) {
        val chaptersCount = Bible.getChapterCount(bookIndex)
        if (chaptersRead == chaptersCount) {
            ++finishedBooks
            if (bookIndex < Bible.OLD_TESTAMENT_COUNT) {
                ++finishedOldTestament
            } else {
                ++finishedNewTestament
            }
        }
        bookReadingStatus.add(ReadingProgressForDisplay.BookReadingStatus(
                bookNames[bookIndex], chaptersRead, chaptersCount))
    }
    return ReadingProgressForDisplay(continuousReadingDays, totalChaptersRead, finishedBooks,
            finishedOldTestament, finishedNewTestament, bookReadingStatus)
}
