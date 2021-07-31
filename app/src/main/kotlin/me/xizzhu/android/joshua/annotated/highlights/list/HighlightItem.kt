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

package me.xizzhu.android.joshua.annotated.highlights.list

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

data class HighlightItem(val verseIndex: VerseIndex, private val bookName: String,
                         private val bookShortName: String, private val verseText: String,
                         @ColorInt private val highlightColor: Int, @Constants.SortOrder private val sortOrder: Int,
                         val onClick: (VerseIndex) -> Unit)
    : BaseItem(R.layout.item_highlight, { inflater, parent -> HighlightItemViewHolder(inflater, parent) }) {
    companion object {
        private val BOOK_NAME_SIZE_SPAN = createTitleSizeSpan()
        private val BOOK_NAME_STYLE_SPAN = createTitleStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    val textForDisplay: CharSequence by lazy {
        SPANNABLE_STRING_BUILDER.clearAll()

        if (sortOrder == Constants.SORT_BY_BOOK) {
            // format:
            // <book short name> <chapter verseIndex>:<verse verseIndex> <verse text>
            SPANNABLE_STRING_BUILDER.append(bookShortName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .setSpan(BOOK_NAME_STYLE_SPAN, BOOK_NAME_SIZE_SPAN)
                    .append(' ')
        } else {
            // format:
            // <book name> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            SPANNABLE_STRING_BUILDER.append(bookName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .setSpan(BOOK_NAME_STYLE_SPAN, BOOK_NAME_SIZE_SPAN)
                    .append('\n')
        }

        return@lazy SPANNABLE_STRING_BUILDER.append(verseText)
                .setSpan(BackgroundColorSpan(highlightColor),
                        ForegroundColorSpan(if (highlightColor == Highlight.COLOR_BLUE) Color.WHITE else Color.BLACK),
                        SPANNABLE_STRING_BUILDER.length - verseText.length, SPANNABLE_STRING_BUILDER.length)
                .toCharSequence()
    }
}

private class HighlightItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<HighlightItem>(inflater.inflate(R.layout.item_highlight, parent, false)) {
    private val text: TextView = itemView.findViewById(R.id.text)

    init {
        itemView.setOnClickListener { item?.let { it.onClick(it.verseIndex) } }
    }

    override fun bind(settings: Settings, item: HighlightItem, payloads: List<Any>) {
        with(text) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
