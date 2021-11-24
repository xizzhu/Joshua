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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemSearchVersePreviewBinding
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createTitleSizeSpan
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.setSpan
import me.xizzhu.android.joshua.ui.toCharSequence
import java.util.*

class SearchVersePreviewItem(
        val verse: Verse, private val query: String
) : BaseItem(R.layout.item_search_verse_preview, { inflater, parent -> SearchVersePreviewItemViewHolder(inflater, parent) }) {
    companion object {
        private val VERSE_INDEX_SIZE_SPAN = createTitleSizeSpan()
        private val VERSE_INDEX_STYLE_SPAN = createTitleStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun openVerse(verseToOpen: VerseIndex)
    }

    val textForDisplay: CharSequence by lazy {
        // format:
        // <chapter verseIndex>:<verse verseIndex> <verse text>
        SPANNABLE_STRING_BUILDER.clearAll()
                .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1)
                .setSpan(VERSE_INDEX_SIZE_SPAN, VERSE_INDEX_STYLE_SPAN)
                .append(' ')
                .append(verse.text.text)

        // highlights the keywords
        SPANNABLE_STRING_BUILDER.highlight(query, SPANNABLE_STRING_BUILDER.length - verse.text.text.length)

        return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
    }
}

private class SearchVersePreviewItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SearchVersePreviewItem>(ItemSearchVersePreviewBinding.inflate(inflater, parent, false).root) {
    private val resources = itemView.resources
    private val viewBinding = ItemSearchVersePreviewBinding.bind(itemView).apply {
        root.setOnClickListener { item?.let { callback().openVerse(it.verse.verseIndex) } }
    }

    private fun callback(): SearchVersePreviewItem.Callback = (itemView.activity as? SearchVersePreviewItem.Callback)
            ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement SearchVersePreviewItemViewHolder.Callback")

    override fun bind(settings: Settings, item: SearchVersePreviewItem, payloads: List<Any>) {
        viewBinding.versePreview.text = item.textForDisplay
        viewBinding.versePreview.setTextColor(settings.getPrimaryTextColor(resources))
        viewBinding.versePreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))
    }
}
