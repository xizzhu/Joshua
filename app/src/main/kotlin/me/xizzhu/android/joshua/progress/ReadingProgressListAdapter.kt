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

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R

class ReadingProgressListAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private val inflater = LayoutInflater.from(context)
    private val resources = context.resources

    private var readingProgress: ReadingProgressForDisplay? = null

    fun setData(readingProgress: ReadingProgressForDisplay) {
        this.readingProgress = readingProgress
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = readingProgress?.bookReadingStatus?.size?.plus(1) ?: 0

    override fun getItemViewType(position: Int) = if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                VIEW_TYPE_HEADER -> ReadingProgressHeaderViewHolder(inflater, parent, resources)
                VIEW_TYPE_ITEM -> ReadingProgressItemViewHolder(inflater, parent)
                else -> throw IllegalArgumentException("Unsupported view type: $viewType")
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_HEADER -> (holder as ReadingProgressHeaderViewHolder).bind(readingProgress!!)
            VIEW_TYPE_ITEM -> (holder as ReadingProgressItemViewHolder).bind(readingProgress!!.bookReadingStatus[position - 1])
            else -> throw IllegalArgumentException("Unsupported view type: ${getItemViewType(position)}")
        }
    }
}

private class ReadingProgressHeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup, private val resources: Resources)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_reading_progress_header, parent, false)) {
    private val continuousReadingDays: TextView = itemView.findViewById(R.id.continuous_reading_days_value)
    private val chaptersRead: TextView = itemView.findViewById(R.id.chapters_read_value)
    private val finishedBooks: TextView = itemView.findViewById(R.id.finished_books_value)
    private val finishedOldTestament: TextView = itemView.findViewById(R.id.finished_old_testament_value)
    private val finishedNewTestament: TextView = itemView.findViewById(R.id.finished_new_testament_value)

    fun bind(readingProgress: ReadingProgressForDisplay) {
        continuousReadingDays.text = resources.getString(R.string.text_continuous_reading_count, readingProgress.continuousReadingDays)
        chaptersRead.text = readingProgress.chaptersRead.toString()
        finishedBooks.text = readingProgress.finishedBooks.toString()
        finishedOldTestament.text = readingProgress.finishedOldTestament.toString()
        finishedNewTestament.text = readingProgress.finishedNewTestament.toString()
    }
}

private class ReadingProgressItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_reading_progress, parent, false)) {
    private val bookName: TextView = itemView.findViewById(R.id.book_name)
    private val readingProgressBar: ReadingProgressBar = itemView.findViewById(R.id.reading_progress_bar)

    fun bind(bookReadingStatus: ReadingProgressForDisplay.BookReadingStatus) {
        bookName.text = bookReadingStatus.bookName
        readingProgressBar.progress = bookReadingStatus.chaptersRead * readingProgressBar.maxProgress / bookReadingStatus.chaptersCount
        readingProgressBar.text = "${bookReadingStatus.chaptersRead} / ${bookReadingStatus.chaptersCount}"
    }
}