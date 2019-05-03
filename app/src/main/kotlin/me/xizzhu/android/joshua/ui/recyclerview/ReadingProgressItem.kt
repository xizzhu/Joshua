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

package me.xizzhu.android.joshua.ui.recyclerview

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.progress.ReadingProgressBar
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor

data class ReadingProgressSummaryItem(val continuousReadingDays: Int, val chaptersRead: Int,
                                      val finishedBooks: Int, val finishedOldTestament: Int,
                                      val finishedNewTestament: Int) : BaseItem {
    override fun getItemViewType(): Int = BaseItem.READING_PROGRESS_SUMMARY_ITEM
}

class ReadingProgressSummaryItemViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                           private val resources: Resources)
    : BaseViewHolder<ReadingProgressSummaryItem>(inflater.inflate(R.layout.item_reading_progress_header, parent, false)) {
    private val continuousReadingDaysTitle: TextView = itemView.findViewById(R.id.continuous_reading_days_title)
    private val continuousReadingDays: TextView = itemView.findViewById(R.id.continuous_reading_days_value)
    private val chaptersReadTitle: TextView = itemView.findViewById(R.id.chapters_read_title)
    private val chaptersRead: TextView = itemView.findViewById(R.id.chapters_read_value)
    private val finishedBooksTitle: TextView = itemView.findViewById(R.id.finished_books_title)
    private val finishedBooks: TextView = itemView.findViewById(R.id.finished_books_value)
    private val finishedOldTestamentTitle: TextView = itemView.findViewById(R.id.finished_old_testament_title)
    private val finishedOldTestament: TextView = itemView.findViewById(R.id.finished_old_testament_value)
    private val finishedNewTestamentTitle: TextView = itemView.findViewById(R.id.finished_new_testament_title)
    private val finishedNewTestament: TextView = itemView.findViewById(R.id.finished_new_testament_value)

    override fun bind(settings: Settings, item: ReadingProgressSummaryItem, payloads: List<Any>) {
        val primaryTextColor = settings.getPrimaryTextColor(resources)
        val bodyTextSize = settings.getBodyTextSize(resources).toFloat()

        continuousReadingDaysTitle.setTextColor(primaryTextColor)
        continuousReadingDaysTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        continuousReadingDays.setTextColor(primaryTextColor)
        continuousReadingDays.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        chaptersReadTitle.setTextColor(primaryTextColor)
        chaptersReadTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        chaptersRead.setTextColor(primaryTextColor)
        chaptersRead.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        finishedBooksTitle.setTextColor(primaryTextColor)
        finishedBooksTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        finishedBooks.setTextColor(primaryTextColor)
        finishedBooks.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        finishedOldTestamentTitle.setTextColor(primaryTextColor)
        finishedOldTestamentTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        finishedOldTestament.setTextColor(primaryTextColor)
        finishedOldTestament.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        finishedNewTestamentTitle.setTextColor(primaryTextColor)
        finishedNewTestamentTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)
        finishedNewTestament.setTextColor(primaryTextColor)
        finishedNewTestament.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize)

        continuousReadingDays.text = resources.getString(R.string.text_continuous_reading_count, item.continuousReadingDays)
        chaptersRead.text = item.chaptersRead.toString()
        finishedBooks.text = item.finishedBooks.toString()
        finishedOldTestament.text = item.finishedOldTestament.toString()
        finishedNewTestament.text = item.finishedNewTestament.toString()
    }
}

data class ReadingProgressDetailItem(val bookName: String, val chaptersRead: Int,
                                     val chaptersCount: Int) : BaseItem {
    override fun getItemViewType(): Int = BaseItem.READING_PROGRESS_DETAIL_ITEM
}

class ReadingProgressDetailItemViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                          private val resources: Resources)
    : BaseViewHolder<ReadingProgressDetailItem>(inflater.inflate(R.layout.item_reading_progress, parent, false)) {
    private val bookName: TextView = itemView.findViewById(R.id.book_name)
    private val readingProgressBar: ReadingProgressBar = itemView.findViewById(R.id.reading_progress_bar)

    override fun bind(settings: Settings, item: ReadingProgressDetailItem, payloads: List<Any>) {
        bookName.setTextColor(settings.getPrimaryTextColor(resources))
        bookName.setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources).toFloat())
        bookName.text = item.bookName
        readingProgressBar.progress = item.chaptersRead * readingProgressBar.maxProgress / item.chaptersCount
        readingProgressBar.text = "${item.chaptersRead} / ${item.chaptersCount}"
    }
}

fun ReadingProgress.toReadingProgressItems(bookNames: List<String>): List<BaseItem> {
    var totalChaptersRead = 0
    val chaptersReadPerBook = Array(Bible.BOOK_COUNT) { 0 }
    for (chapter in chapterReadingStatus) {
        chaptersReadPerBook[chapter.bookIndex]++
        ++totalChaptersRead
    }

    var finishedBooks = 0
    var finishedOldTestament = 0
    var finishedNewTestament = 0
    val detailItems = ArrayList<ReadingProgressDetailItem>(Bible.BOOK_COUNT)
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
        detailItems.add(ReadingProgressDetailItem(bookNames[bookIndex], chaptersRead, chaptersCount))
    }
    return mutableListOf<BaseItem>(ReadingProgressSummaryItem(continuousReadingDays, totalChaptersRead, finishedBooks,
            finishedOldTestament, finishedNewTestament)).apply { addAll(detailItems) }
}
