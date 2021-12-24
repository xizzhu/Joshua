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
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemSearchVersePreviewBinding
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createTitleSpans
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.nextNonEmpty
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence
import java.util.*

class SearchVersePreviewItem(
        val verse: Verse, private val query: String, private val followingEmptyVerseCount: Int
) : BaseItem(R.layout.item_search_verse_preview, { inflater, parent -> SearchVersePreviewItemViewHolder(inflater, parent) }) {
    companion object {
        private val VERSE_INDEX_SPANS = createTitleSpans()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun openVerse(verseToOpen: VerseIndex)
    }

    val textForDisplay: CharSequence by lazy {
        // format:
        // <chapter verseIndex>:<verse verseIndex> <verse text>
        SPANNABLE_STRING_BUILDER.clearAll()

        SPANNABLE_STRING_BUILDER.append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1)
        if (followingEmptyVerseCount > 0) {
            SPANNABLE_STRING_BUILDER.append('-').append(verse.verseIndex.verseIndex + followingEmptyVerseCount + 1)
        }

        SPANNABLE_STRING_BUILDER.setSpans(VERSE_INDEX_SPANS)
                .append(' ')
                .append(verse.text.text)

        // highlights the keywords
        SPANNABLE_STRING_BUILDER.highlightKeyword(query, SPANNABLE_STRING_BUILDER.length - verse.text.text.length)

        return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
    }
}

fun List<Verse>.toSearchVersePreviewItems(query: String): List<SearchVersePreviewItem> {
    val items = ArrayList<SearchVersePreviewItem>(size)

    val verseIterator = iterator()
    var verse: Verse? = null
    while (verse != null || verseIterator.hasNext()) {
        verse = verse ?: verseIterator.next()

        val (nextVerse, followingEmptyVerseCount) = verseIterator.nextNonEmpty(verse)

        items.add(SearchVersePreviewItem(verse, query, followingEmptyVerseCount))

        verse = nextVerse
    }

    return items
}

private class SearchVersePreviewItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SearchVersePreviewItem, ItemSearchVersePreviewBinding>(ItemSearchVersePreviewBinding.inflate(inflater, parent, false)) {
    init {
        viewBinding.root.setOnClickListener { item?.let { callback().openVerse(it.verse.verseIndex) } }
    }

    private fun callback(): SearchVersePreviewItem.Callback = (itemView.activity as? SearchVersePreviewItem.Callback)
            ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement SearchVersePreviewItemViewHolder.Callback")

    override fun bind(settings: Settings, item: SearchVersePreviewItem, payloads: List<Any>) {
        viewBinding.versePreview.text = item.textForDisplay
        viewBinding.versePreview.setPrimaryTextSize(settings)
    }
}
