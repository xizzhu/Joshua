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

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemCrossReferenceBinding
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

class CrossReferenceItem(
        val verseIndex: VerseIndex, val verseText: Verse.Text, private val bookName: String
) : BaseItem(R.layout.item_cross_reference, { inflater, parent -> CrossReferenceItemViewHolder(inflater, parent) }) {
    companion object {
        private val BOOK_NAME_SPANS = createTitleSpans()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun onCrossReferenceVerseClicked(verseIndex: VerseIndex)

        fun onCrossReferenceVerseLongClicked(verse: Verse)
    }

    val textForDisplay: CharSequence by lazy {
        if (!verseIndex.isValid() || !verseText.isValid()) {
            return@lazy ""
        }

        // format:
        // <book name> <chapter verseIndex>:<verse verseIndex>
        // <verse text>
        return@lazy SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookName).append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpans(BOOK_NAME_SPANS)
                .append('\n').append(verseText.text)
                .toCharSequence()
    }
}

private class CrossReferenceItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<CrossReferenceItem, ItemCrossReferenceBinding>(ItemCrossReferenceBinding.inflate(inflater, parent, false)) {
    init {
        itemView.setOnClickListener { item?.let { callback().onCrossReferenceVerseClicked(it.verseIndex) } }
        itemView.setOnLongClickListener {
            return@setOnLongClickListener item?.let { item ->
                callback().onCrossReferenceVerseLongClicked(Verse(item.verseIndex, item.verseText, emptyList()))
                true
            } ?: false
        }
    }

    private fun callback(): CrossReferenceItem.Callback = (itemView.activity as? CrossReferenceItem.Callback)
            ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement CrossReferenceItem.Callback")

    override fun bind(settings: Settings, item: CrossReferenceItem, payloads: List<Any>) {
        with(viewBinding.text) {
            setPrimaryTextSize(settings)
            text = item.textForDisplay
        }
    }
}
