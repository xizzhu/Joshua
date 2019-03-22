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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R

class ReadingProgressListAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    private var readingProgress: ReadingProgressForDisplay? = null

    fun setData(readingProgress: ReadingProgressForDisplay) {
        this.readingProgress = readingProgress

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = readingProgress?.bookReadingStatus?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ReadingProgressItemViewHolder(inflater, parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ReadingProgressItemViewHolder).bind(readingProgress!!.bookReadingStatus[position])
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
