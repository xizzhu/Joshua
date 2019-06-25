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

package me.xizzhu.android.joshua.reading.detail

import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText
import android.text.Spannable
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

data class VerseTextItem(val verseIndex: VerseIndex, val verseText: Verse.Text,
                         val onClicked: (String) -> Unit, val onLongClicked: (Verse) -> Unit) : BaseItem {
    companion object {
        private val BOOK_NAME_SIZE_SPAN = RelativeSizeSpan(0.95F)
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()

        private const val VIEW_TYPE = R.layout.item_verse_text

        init {
            BaseItem.viewHolderCreator[VIEW_TYPE] = { inflater, parent -> VerseTextItemViewHolder(inflater, parent) }
        }
    }

    val textForDisplay: CharSequence by lazy {
        if (!verseIndex.isValid() || !verseText.isValid()) {
            return@lazy ""
        }

        SPANNABLE_STRING_BUILDER.clear()
        SPANNABLE_STRING_BUILDER.clearSpans()

        // format:
        // <translation short name>, <book name> <chapter verseIndex>:<verse verseIndex>
        // <verse text>
        SPANNABLE_STRING_BUILDER.append(verseText.translationShortName).append(", ")
                .append(verseText.bookName).append(' ')
                .append((verseIndex.chapterIndex + 1).toString()).append(':').append((verseIndex.verseIndex + 1).toString())
        SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_SIZE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        SPANNABLE_STRING_BUILDER.append('\n').append(verseText.text)

        return@lazy SPANNABLE_STRING_BUILDER.subSequence(0, SPANNABLE_STRING_BUILDER.length)
    }

    override fun getItemViewType(): Int = VIEW_TYPE
}

private class VerseTextItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<VerseTextItem>(inflater.inflate(R.layout.item_verse_text, parent, false)) {
    private val text: TextView = itemView.findViewById(R.id.text)

    init {
        itemView.setOnClickListener { item?.let { it.onClicked(it.verseText.translationShortName) } }
        itemView.setOnLongClickListener {
            item?.let { it.onLongClicked(Verse(it.verseIndex, it.verseText, emptyList())) }
            return@setOnLongClickListener true
        }
    }

    override fun bind(settings: Settings, item: VerseTextItem, payloads: List<Any>) {
        with(text) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
