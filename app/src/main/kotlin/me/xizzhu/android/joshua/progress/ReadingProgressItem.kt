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

package me.xizzhu.android.joshua.progress

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence

class ReadingProgressSummaryItem(
        val continuousReadingDays: Int, val chaptersRead: Int, val finishedBooks: Int,
        val finishedOldTestament: Int, val finishedNewTestament: Int
) : BaseItem(R.layout.item_reading_progress_header, { inflater, parent -> ReadingProgressSummaryItemViewHolder(inflater, parent) })

private class ReadingProgressSummaryItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
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
        continuousReadingDaysTitle.setPrimaryTextSize(settings)
        continuousReadingDays.setPrimaryTextSize(settings)
        chaptersReadTitle.setPrimaryTextSize(settings)
        chaptersRead.setPrimaryTextSize(settings)
        finishedBooksTitle.setPrimaryTextSize(settings)
        finishedBooks.setPrimaryTextSize(settings)
        finishedOldTestamentTitle.setPrimaryTextSize(settings)
        finishedOldTestament.setPrimaryTextSize(settings)
        finishedNewTestamentTitle.setPrimaryTextSize(settings)
        finishedNewTestament.setPrimaryTextSize(settings)

        continuousReadingDays.text = continuousReadingDays.resources.getString(R.string.text_continuous_reading_count, item.continuousReadingDays)
        chaptersRead.text = item.chaptersRead.toString()
        finishedBooks.text = item.finishedBooks.toString()
        finishedOldTestament.text = item.finishedOldTestament.toString()
        finishedNewTestament.text = item.finishedNewTestament.toString()
    }
}

class ReadingProgressDetailItem(
        val bookName: String, val bookIndex: Int,
        val chaptersRead: List<Boolean>, val chaptersReadCount: Int,
        val onBookClicked: (Int, Boolean) -> Unit,
        var expanded: Boolean
) : BaseItem(R.layout.item_reading_progress, { inflater, parent -> ReadingProgressDetailItemViewHolder(inflater, parent) }) {
    interface Callback {
        fun openVerse(verseToOpen: VerseIndex)
    }
}

private class ReadingProgressDetailItemViewHolder(private val inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<ReadingProgressDetailItem>(inflater.inflate(R.layout.item_reading_progress, parent, false)) {
    companion object {
        private const val ROW_CHILD_COUNT = 5

        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
        private val CHAPTER_READ_SPANS: Array<CharacterStyle> = arrayOf(
                ForegroundColorSpan(0xFF99CC00.toInt()), // R.color.dark_lime
                StyleSpan(Typeface.BOLD)
        )
    }

    private val bookName: TextView = itemView.findViewById(R.id.book_name)
    private val readingProgressBar: ReadingProgressBar = itemView.findViewById(R.id.reading_progress_bar)
    private val chapters: LinearLayout = itemView.findViewById(R.id.chapters)

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        item?.let {
            item?.let { item ->
                (itemView.activity as? ReadingProgressDetailItem.Callback)?.openVerse(VerseIndex(item.bookIndex, v.tag as Int, 0))
                        ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement ReadingProgressDetailItem.Callback")
            }
        }
    }

    init {
        itemView.setOnClickListener {
            item?.let {
                if (it.expanded) {
                    chapters.visibility = View.GONE
                    it.expanded = false
                } else {
                    showChapters(it)
                    it.expanded = true
                }
                it.onBookClicked(it.bookIndex, it.expanded)
            }
        }
    }

    override fun bind(settings: Settings, item: ReadingProgressDetailItem, payloads: List<Any>) {
        with(bookName) {
            setPrimaryTextSize(settings)
            text = item.bookName
        }

        with(readingProgressBar) {
            progress = item.chaptersReadCount * maxProgress / item.chaptersRead.size
            text = "${item.chaptersReadCount} / ${item.chaptersRead.size}"
        }

        if (item.expanded) {
            showChapters(item)
        } else {
            chapters.visibility = View.GONE
        }
    }

    private fun showChapters(item: ReadingProgressDetailItem) {
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

                            SPANNABLE_STRING_BUILDER.clearAll().append(chapter + 1)
                            if (item.chaptersRead[chapter]) {
                                SPANNABLE_STRING_BUILDER.setSpans(CHAPTER_READ_SPANS)
                            }
                            text = SPANNABLE_STRING_BUILDER.toCharSequence()
                        }
                    }
                }
            }

            visibility = View.VISIBLE
        }
    }
}
