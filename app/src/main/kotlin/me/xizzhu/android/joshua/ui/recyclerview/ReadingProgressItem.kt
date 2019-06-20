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

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.progress.ReadingProgressBar
import me.xizzhu.android.joshua.ui.getPrimaryTextColor
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText

data class ReadingProgressSummaryItem(val continuousReadingDays: Int, val chaptersRead: Int,
                                      val finishedBooks: Int, val finishedOldTestament: Int,
                                      val finishedNewTestament: Int) : BaseItem {
    override fun getItemViewType(): Int = BaseItem.READING_PROGRESS_SUMMARY_ITEM
}

class ReadingProgressSummaryItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
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
        continuousReadingDaysTitle.updateSettingsWithPrimaryText(settings)
        continuousReadingDays.updateSettingsWithPrimaryText(settings)
        chaptersReadTitle.updateSettingsWithPrimaryText(settings)
        chaptersRead.updateSettingsWithPrimaryText(settings)
        finishedBooksTitle.updateSettingsWithPrimaryText(settings)
        finishedBooks.updateSettingsWithPrimaryText(settings)
        finishedOldTestamentTitle.updateSettingsWithPrimaryText(settings)
        finishedOldTestament.updateSettingsWithPrimaryText(settings)
        finishedNewTestamentTitle.updateSettingsWithPrimaryText(settings)
        finishedNewTestament.updateSettingsWithPrimaryText(settings)

        continuousReadingDays.text = continuousReadingDays.resources.getString(R.string.text_continuous_reading_count, item.continuousReadingDays)
        chaptersRead.text = item.chaptersRead.toString()
        finishedBooks.text = item.finishedBooks.toString()
        finishedOldTestament.text = item.finishedOldTestament.toString()
        finishedNewTestament.text = item.finishedNewTestament.toString()
    }
}

data class ReadingProgressDetailItem(val bookName: String, val bookIndex: Int,
                                     val chaptersRead: Array<Boolean>, val chaptersReadCount: Int,
                                     val onChapterClicked: (Int, Int) -> Unit) : BaseItem {
    override fun getItemViewType(): Int = BaseItem.READING_PROGRESS_DETAIL_ITEM
}

class ReadingProgressDetailItemViewHolder(private val inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<ReadingProgressDetailItem>(inflater.inflate(R.layout.item_reading_progress, parent, false)) {
    companion object {
        private const val ROW_CHILD_COUNT = 5
    }

    private val resources = itemView.resources
    private val chapterReadColor = ContextCompat.getColor(itemView.context, R.color.dark_lime)
    private val bookName: TextView = itemView.findViewById(R.id.book_name)
    private val readingProgressBar: ReadingProgressBar = itemView.findViewById(R.id.reading_progress_bar)
    private val chapters: LinearLayout = itemView.findViewById(R.id.chapters)

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        item?.let { it.onChapterClicked(it.bookIndex, v.tag as Int) }
    }

    override fun bind(settings: Settings, item: ReadingProgressDetailItem, payloads: List<Any>) {
        with(bookName) {
            updateSettingsWithPrimaryText(settings)
            text = item.bookName
        }

        with(readingProgressBar) {
            progress = item.chaptersReadCount * maxProgress / item.chaptersRead.size
            text = "${item.chaptersReadCount} / ${item.chaptersRead.size}"
        }

        val rowCount = item.chaptersRead.size / ROW_CHILD_COUNT + if (item.chaptersRead.size % ROW_CHILD_COUNT == 0) 0 else 1
        with(chapters) {
            if (childCount > rowCount) {
                removeViews(rowCount, childCount - rowCount)
            }
            repeat(rowCount - childCount) {
                (inflater.inflate(R.layout.row_reading_progress_chapters, this, false) as LinearLayout).also {
                    addView(it)
                    for (i in 0 until it.childCount) {
                        it.getChildAt(i).setOnClickListener(onClickListener)
                    }
                }
            }

            for (i in 0 until rowCount) {
                val row = chapters.getChildAt(i) as LinearLayout
                for (j in 0 until ROW_CHILD_COUNT) {
                    val chapter = i * ROW_CHILD_COUNT + j
                    with(row.getChildAt(j) as TextView) {
                        if (chapter >= item.chaptersRead.size) {
                            visibility = View.GONE
                        } else {
                            visibility = View.VISIBLE
                            tag = chapter
                            text = (chapter + 1).toString()
                            if (item.chaptersRead[chapter]) {
                                setTextColor(chapterReadColor)
                                setTypeface(null, Typeface.BOLD)
                            } else {
                                setTextColor(settings.getPrimaryTextColor(this@ReadingProgressDetailItemViewHolder.resources))
                                setTypeface(null, Typeface.NORMAL)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun ReadingProgress.toReadingProgressItems(bookNames: List<String>, onChapterClicked: (Int, Int) -> Unit): List<BaseItem> {
    var totalChaptersRead = 0
    val chaptersReadPerBook = Array(Bible.BOOK_COUNT) { i -> Array(Bible.getChapterCount(i)) { false } }
    val chaptersReadCountPerBook = Array(Bible.BOOK_COUNT) { 0 }
    for (chapter in chapterReadingStatus) {
        chaptersReadPerBook[chapter.bookIndex][chapter.chapterIndex] = true
        chaptersReadCountPerBook[chapter.bookIndex]++
        ++totalChaptersRead
    }

    var finishedBooks = 0
    var finishedOldTestament = 0
    var finishedNewTestament = 0
    val detailItems = ArrayList<ReadingProgressDetailItem>(Bible.BOOK_COUNT)
    for ((bookIndex, chaptersRead) in chaptersReadPerBook.withIndex()) {
        val chaptersReadCount = chaptersReadCountPerBook[bookIndex]
        if (chaptersReadCount == Bible.getChapterCount(bookIndex)) {
            ++finishedBooks
            if (bookIndex < Bible.OLD_TESTAMENT_COUNT) {
                ++finishedOldTestament
            } else {
                ++finishedNewTestament
            }
        }
        detailItems.add(ReadingProgressDetailItem(
                bookNames[bookIndex], bookIndex, chaptersRead, chaptersReadCount, onChapterClicked))
    }
    return mutableListOf<BaseItem>(ReadingProgressSummaryItem(continuousReadingDays, totalChaptersRead, finishedBooks,
            finishedOldTestament, finishedNewTestament)).apply { addAll(detailItems) }
}
