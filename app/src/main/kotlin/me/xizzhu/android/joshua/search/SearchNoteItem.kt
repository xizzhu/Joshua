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

package me.xizzhu.android.joshua.search

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemSearchNoteBinding
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import java.util.*

class SearchNoteItem(val verseIndex: VerseIndex, private val bookShortName: String,
                     private val verseText: String, private val note: String, private val query: String)
    : BaseItem(R.layout.item_search_note, { inflater, parent -> SearchNoteItemViewHolder(inflater, parent) }) {
    companion object {
        private val BOOK_NAME_STYLE_SPAN = createTitleStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun openVerse(verseToOpen: VerseIndex)
    }

    val verseForDisplay: CharSequence by lazy {
        // format:
        // <book short name> <chapter index>:<verse index> <verse text>
        return@lazy SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName).append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpan(BOOK_NAME_STYLE_SPAN)
                .append(' ').append(verseText)
                .toCharSequence()
    }

    val noteForDisplay: CharSequence by lazy {
        SPANNABLE_STRING_BUILDER.clearAll().append(note)
                .highlightKeyword(query, 0) // highlights the keywords

        return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
    }
}

private class SearchNoteItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SearchNoteItem, ItemSearchNoteBinding>(ItemSearchNoteBinding.inflate(inflater, parent, false)) {
    init {
        itemView.setOnClickListener {
            item?.let { item ->
                (itemView.activity as? SearchNoteItem.Callback)?.openVerse(item.verseIndex)
                        ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement SearchNoteItem.Callback")
            }
        }
    }

    override fun bind(settings: Settings, item: SearchNoteItem, payloads: List<Any>) {
        with(viewBinding.verse) {
            setSecondaryTextSize(settings)
            text = item.verseForDisplay
        }
        with(viewBinding.text) {
            setPrimaryTextSize(settings)
            text = item.noteForDisplay
        }
    }
}
