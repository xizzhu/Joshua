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

package me.xizzhu.android.joshua.reading.detail.pages

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.reading.detail.VerseDetail

class VerseDetailPagerAdapter(context: Context) : RecyclerView.Adapter<VerseDetailPage>() {
    companion object {
        const val PAGE_VERSES = 0
        const val PAGE_NOTE = 1
        const val PAGE_STRONG_NUMBER = 2
        private const val PAGE_COUNT = 3
    }

    private val resources: Resources = context.resources
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var onNoteUpdated: ((String) -> Unit)? = null
    var onNoStrongNumberClicked: (() -> Unit)? = null
    var settings: Settings? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var verseDetail: VerseDetail = VerseDetail.INVALID
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = if (settings != null && onNoteUpdated != null && onNoStrongNumberClicked != null) PAGE_COUNT else 0

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseDetailPage =
            when (viewType) {
                PAGE_VERSES -> VersesPage(inflater, parent)
                PAGE_NOTE -> NotePage(inflater, parent, onNoteUpdated!!)
                PAGE_STRONG_NUMBER -> StrongNumberPage(inflater, parent, onNoStrongNumberClicked!!)
                else -> throw IllegalArgumentException("Unsupported view type: $viewType")
            }

    override fun onBindViewHolder(holder: VerseDetailPage, position: Int) {
        holder.bind(verseDetail, settings!!)
    }

    override fun onViewAttachedToWindow(holder: VerseDetailPage) {
        // Workaround for https://issuetracker.google.com/issues/37095917
        if (holder is NotePage) {
            holder.note.isEnabled = false
            holder.note.isEnabled = true
        }
    }

    fun pageTitle(position: Int): CharSequence = when (position) {
        PAGE_VERSES -> resources.getString(R.string.text_verse_comparison)
        PAGE_NOTE -> resources.getString(R.string.text_note)
        PAGE_STRONG_NUMBER -> resources.getString(R.string.text_strong_number)
        else -> throw IllegalArgumentException("Unsupported position: $position")
    }
}
