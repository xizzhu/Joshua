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

package me.xizzhu.android.joshua.reading.detail

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.PageVerseDetailNoteBinding
import me.xizzhu.android.joshua.databinding.PageVerseDetailStrongNumberBinding
import me.xizzhu.android.joshua.databinding.PageVerseDetailVersesBinding
import me.xizzhu.android.joshua.reading.VerseDetailViewData
import me.xizzhu.android.joshua.ui.setPrimaryTextSize

class VerseDetailPagerAdapter(context: Context) : RecyclerView.Adapter<VerseDetailPage<*>>() {
    companion object {
        const val PAGE_VERSES = 0
        const val PAGE_NOTE = 1
        const val PAGE_STRONG_NUMBER = 2
        private const val PAGE_COUNT = 3
    }

    private val resources: Resources = context.resources
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var updateNote: ((VerseIndex, String) -> Unit)? = null
    private var requestStrongNumber: (() -> Unit)? = null

    var settings: Settings? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var verseDetail: VerseDetailViewData? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun initialize(updateNote: (VerseIndex, String) -> Unit, requestStrongNumber: () -> Unit) {
        this.updateNote = updateNote
        this.requestStrongNumber = requestStrongNumber
    }

    override fun getItemCount(): Int =
            if (settings != null && verseDetail != null && updateNote != null && requestStrongNumber != null) PAGE_COUNT else 0

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseDetailPage<*> =
            when (viewType) {
                PAGE_VERSES -> VersesPage(inflater, parent)
                PAGE_NOTE -> NotePage(inflater, parent, updateNote!!)
                PAGE_STRONG_NUMBER -> StrongNumberPage(inflater, parent, requestStrongNumber!!)
                else -> throw IllegalArgumentException("Unsupported view type: $viewType")
            }

    override fun onBindViewHolder(holder: VerseDetailPage<*>, position: Int) {
        holder.bind(verseDetail!!, settings!!)
    }

    override fun onViewAttachedToWindow(holder: VerseDetailPage<*>) {
        // Workaround for https://issuetracker.google.com/issues/37095917
        if (holder is NotePage) {
            holder.viewBinding.note.isEnabled = false
            holder.viewBinding.note.isEnabled = true
        }
    }

    fun pageTitle(position: Int): CharSequence = when (position) {
        PAGE_VERSES -> resources.getString(R.string.text_verse_comparison)
        PAGE_NOTE -> resources.getString(R.string.text_note)
        PAGE_STRONG_NUMBER -> resources.getString(R.string.text_strong_number)
        else -> throw IllegalArgumentException("Unsupported position: $position")
    }
}

abstract class VerseDetailPage<VB : ViewBinding>(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root) {
    abstract fun bind(verseDetail: VerseDetailViewData, settings: Settings)
}

private class VersesPage(inflater: LayoutInflater, container: ViewGroup)
    : VerseDetailPage<PageVerseDetailVersesBinding>(PageVerseDetailVersesBinding.inflate(inflater, container, false)) {
    init {
        viewBinding.verseTextList.isNestedScrollingEnabled = false
    }

    override fun bind(verseDetail: VerseDetailViewData, settings: Settings) {
        with(viewBinding.verseTextList) {
            setItems(verseDetail.verseTextItems)
            setSettings(settings)
            scrollToPosition(0)
        }
    }
}

private class NotePage(inflater: LayoutInflater, container: ViewGroup, updateNote: (VerseIndex, String) -> Unit)
    : VerseDetailPage<PageVerseDetailNoteBinding>(PageVerseDetailNoteBinding.inflate(inflater, container, false)) {
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            verseDetail?.let { updateNote(it.verseIndex, s.toString()) }
        }
    }

    private var verseDetail: VerseDetailViewData? = null

    init {
        viewBinding.note.addTextChangedListener(textWatcher)
    }

    override fun bind(verseDetail: VerseDetailViewData, settings: Settings) {
        this.verseDetail = verseDetail
        with(viewBinding.note) {
            removeTextChangedListener(textWatcher)
            setText(verseDetail.note)
            addTextChangedListener(textWatcher)

            setPrimaryTextSize(settings)
        }
    }
}

private class StrongNumberPage(inflater: LayoutInflater, container: ViewGroup, requestStrongNumber: () -> Unit)
    : VerseDetailPage<PageVerseDetailStrongNumberBinding>(PageVerseDetailStrongNumberBinding.inflate(inflater, container, false)) {
    init {
        with(viewBinding) {
            emptyStrongNumberList.setOnClickListener { requestStrongNumber() }
            strongNumberList.isNestedScrollingEnabled = false
        }
    }

    override fun bind(verseDetail: VerseDetailViewData, settings: Settings) {
        if (verseDetail.strongNumberItems.isEmpty()) {
            bindNoStrongNumberView(settings)
        } else {
            bindStrongNumberItems(verseDetail.strongNumberItems, settings)
        }
    }

    private fun bindNoStrongNumberView(settings: Settings) {
        with(viewBinding) {
            emptyStrongNumberList.visibility = View.VISIBLE
            emptyStrongNumberList.setPrimaryTextSize(settings)

            strongNumberList.visibility = View.GONE
        }
    }

    private fun bindStrongNumberItems(strongNumberItems: List<StrongNumberItem>, settings: Settings) {
        with(viewBinding) {
            emptyStrongNumberList.visibility = View.GONE

            strongNumberList.visibility = View.VISIBLE
            strongNumberList.setItems(strongNumberItems)
            strongNumberList.setSettings(settings)
        }
    }
}
