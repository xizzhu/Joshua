/*
 * Copyright (C) 2022 Xizhi Zhu
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
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemReadingProgressBinding
import me.xizzhu.android.joshua.databinding.ItemReadingProgressHeaderBinding
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewAdapter
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewItem
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence
import java.util.concurrent.Executor

class ReadingProgressAdapter(
    private val inflater: LayoutInflater,
    executor: Executor,
    private val onViewEvent: (ViewEvent) -> Unit,
) : VerticalRecyclerViewAdapter<ReadingProgressItem, ReadingProgressViewHolder<ReadingProgressItem, *>>(ReadingProgressItem.DiffCallback(), executor) {
    sealed class ViewEvent {
        data class ExpandOrCollapseBook(val bookIndex: Int) : ViewEvent()
        data class OpenVerse(val verseToOpen: VerseIndex) : ViewEvent()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingProgressViewHolder<ReadingProgressItem, *> = when (viewType) {
        ReadingProgressItem.Summary.VIEW_TYPE -> ReadingProgressViewHolder.Summary(inflater, parent)
        ReadingProgressItem.Book.VIEW_TYPE -> ReadingProgressViewHolder.Book(inflater, parent, onViewEvent)
        else -> throw IllegalStateException("Unknown view type - $viewType")
    } as ReadingProgressViewHolder<ReadingProgressItem, *>
}

sealed class ReadingProgressItem(viewType: Int) : VerticalRecyclerViewItem(viewType) {
    class DiffCallback : DiffUtil.ItemCallback<ReadingProgressItem>() {
        override fun areItemsTheSame(oldItem: ReadingProgressItem, newItem: ReadingProgressItem): Boolean = when {
            oldItem is Summary && newItem is Summary -> true
            oldItem is Book && newItem is Book -> oldItem.bookIndex == newItem.bookIndex
            else -> false
        }

        override fun areContentsTheSame(oldItem: ReadingProgressItem, newItem: ReadingProgressItem): Boolean = oldItem == newItem
    }

    data class Summary(
        val settings: Settings,
        val continuousReadingDays: Int,
        val chaptersRead: Int,
        val finishedBooks: Int,
        val finishedOldTestament: Int,
        val finishedNewTestament: Int
    ) : ReadingProgressItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_reading_progress_header
        }
    }

    data class Book(
        val settings: Settings,
        val bookName: String,
        val bookIndex: Int,
        val chaptersRead: List<Boolean>,
        val chaptersReadCount: Int,
        val expanded: Boolean
    ) : ReadingProgressItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_reading_progress
        }
    }
}

sealed class ReadingProgressViewHolder<Item : ReadingProgressItem, VB : ViewBinding>(viewBinding: VB)
    : VerticalRecyclerViewHolder<Item, VB>(viewBinding) {
    class Summary(inflater: LayoutInflater, parent: ViewGroup) : ReadingProgressViewHolder<ReadingProgressItem.Summary, ItemReadingProgressHeaderBinding>(
        ItemReadingProgressHeaderBinding.inflate(inflater, parent, false)
    ) {
        override fun bind(item: ReadingProgressItem.Summary, payloads: List<Any>) = with(viewBinding) {
            continuousReadingDaysTitle.setPrimaryTextSize(item.settings)
            continuousReadingDaysValue.setPrimaryTextSize(item.settings)
            chaptersReadTitle.setPrimaryTextSize(item.settings)
            chaptersReadValue.setPrimaryTextSize(item.settings)
            finishedBooksTitle.setPrimaryTextSize(item.settings)
            finishedBooksValue.setPrimaryTextSize(item.settings)
            finishedOldTestamentTitle.setPrimaryTextSize(item.settings)
            finishedOldTestamentValue.setPrimaryTextSize(item.settings)
            finishedNewTestamentTitle.setPrimaryTextSize(item.settings)
            finishedNewTestamentValue.setPrimaryTextSize(item.settings)

            continuousReadingDaysValue.text = continuousReadingDaysValue.resources.getString(R.string.text_continuous_reading_count, item.continuousReadingDays)
            chaptersReadValue.text = item.chaptersRead.toString()
            finishedBooksValue.text = item.finishedBooks.toString()
            finishedOldTestamentValue.text = item.finishedOldTestament.toString()
            finishedNewTestamentValue.text = item.finishedNewTestament.toString()
        }
    }

    class Book(private val inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (ReadingProgressAdapter.ViewEvent) -> Unit)
        : ReadingProgressViewHolder<ReadingProgressItem.Book, ItemReadingProgressBinding>(
        ItemReadingProgressBinding.inflate(inflater, parent, false)
    ) {
        companion object {
            private const val ROW_CHILD_COUNT = 5

            private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
            private val CHAPTER_READ_SPANS: Array<CharacterStyle> = arrayOf(
                ForegroundColorSpan(0xFF99CC00.toInt()), // R.color.dark_lime
                StyleSpan(Typeface.BOLD)
            )
        }

        private val onChapterClickListener: View.OnClickListener = View.OnClickListener { v ->
            item?.let { item ->
                onViewEvent(ReadingProgressAdapter.ViewEvent.OpenVerse(verseToOpen = VerseIndex(item.bookIndex, v.tag as Int, 0)))
            }
        }

        init {
            itemView.setOnClickListener {
                item?.let { item ->
                    onViewEvent(ReadingProgressAdapter.ViewEvent.ExpandOrCollapseBook(bookIndex = item.bookIndex))
                }
            }
        }

        override fun bind(item: ReadingProgressItem.Book, payloads: List<Any>) = with(viewBinding) {
            bookName.setPrimaryTextSize(item.settings)
            bookName.text = item.bookName

            readingProgressBar.progress = item.chaptersReadCount * readingProgressBar.maxProgress / item.chaptersRead.size
            readingProgressBar.text = "${item.chaptersReadCount} / ${item.chaptersRead.size}"

            if (item.expanded) {
                val rowCount = item.chaptersRead.size / ROW_CHILD_COUNT + if (item.chaptersRead.size % ROW_CHILD_COUNT == 0) 0 else 1
                if (chapters.childCount > rowCount) {
                    chapters.removeViews(rowCount, chapters.childCount - rowCount)
                }
                repeat(rowCount - chapters.childCount) {
                    val row = inflater.inflate(R.layout.row_reading_progress_chapters, chapters, false) as LinearLayout
                    chapters.addView(row)
                    row.children.forEach { it.setOnClickListener(onChapterClickListener) }
                }

                for (i in 0 until rowCount) {
                    val row = chapters.getChildAt(i) as LinearLayout
                    for (j in 0 until ROW_CHILD_COUNT) {
                        val chapter = i * ROW_CHILD_COUNT + j
                        with(row.getChildAt(j) as TextView) {
                            if (chapter >= item.chaptersRead.size) {
                                isVisible = false
                            } else {
                                isVisible = true
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

                chapters.isVisible = true
            } else {
                chapters.isVisible = false
            }
        }
    }
}
