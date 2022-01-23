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

package me.xizzhu.android.joshua.search

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemSearchVerseBinding
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createHighlightSpans
import me.xizzhu.android.joshua.ui.createTitleSpans
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.toCharSequence
import me.xizzhu.android.joshua.ui.setSpans
import java.util.*

class SearchVerseItem(val verseIndex: VerseIndex, private val bookShortName: String,
                      private val text: String, private val query: String,
                      @ColorInt private val highlightColor: Int)
    : BaseItem(R.layout.item_search_verse, { inflater, parent -> SearchVerseItemViewHolder(inflater, parent) }) {
    companion object {
        private val BOOK_NAME_SPANS = createTitleSpans()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun openVerse(verseToOpen: VerseIndex)

        fun showPreview(verseIndex: VerseIndex)
    }

    val textForDisplay: CharSequence by lazy {
        // format:
        // <short book name> <chapter verseIndex>:<verse verseIndex>
        // <verse text>
        SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName)
                .append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpans(BOOK_NAME_SPANS)
                .append('\n')
                .append(text)

        // highlights the keywords
        SPANNABLE_STRING_BUILDER.highlightKeyword(query, SPANNABLE_STRING_BUILDER.length - text.length)

        // highlights the verse
        SPANNABLE_STRING_BUILDER.setSpans(
                createHighlightSpans(highlightColor), SPANNABLE_STRING_BUILDER.length - text.length, SPANNABLE_STRING_BUILDER.length
        )

        return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
    }
}

private class SearchVerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SearchVerseItem, ItemSearchVerseBinding>(ItemSearchVerseBinding.inflate(inflater, parent, false)) {
    init {
        itemView.setOnClickListener {
            item?.let { item ->
                (itemView.activity as? SearchVerseItem.Callback)?.openVerse(item.verseIndex)
                        ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement SearchVerseItem.Callback")
            }
        }
        itemView.setOnLongClickListener {
            item?.let { item ->
                (itemView.activity as? SearchVerseItem.Callback)?.showPreview(item.verseIndex)
                        ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement SearchVerseItem.Callback")
            }
            true
        }
    }

    override fun bind(settings: Settings, item: SearchVerseItem, payloads: List<Any>) {
        with(viewBinding.root) {
            setPrimaryTextSize(settings)
            text = item.textForDisplay
        }
    }
}
