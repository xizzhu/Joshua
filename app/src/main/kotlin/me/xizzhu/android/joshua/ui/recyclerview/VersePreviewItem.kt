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

package me.xizzhu.android.joshua.ui.recyclerview

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemVersePreviewBinding
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createTitleSpans
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence
import java.util.*

class VersePreviewItem(val verse: Verse, followingEmptyVerseCount: Int)
    : BaseItem(R.layout.item_verse_preview, { inflater, parent -> VersePreviewItemViewHolder(inflater, parent) }) {
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

        return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
    }
}

fun List<Verse>.toVersePreviewItems(): List<VersePreviewItem> {
    val items = ArrayList<VersePreviewItem>(size)

    val verseIterator = iterator()
    var verse: Verse? = null
    while (verse != null || verseIterator.hasNext()) {
        verse = verse ?: verseIterator.next()

        val (nextVerse, followingEmptyVerseCount) = verseIterator.nextNonEmpty(verse)

        items.add(VersePreviewItem(verse, followingEmptyVerseCount))

        verse = nextVerse
    }

    return items
}

// skips the empty verses
private fun Iterator<Verse>.nextNonEmpty(current: Verse): Pair<Verse?, Int> {
    var nextVerse: Verse? = null
    while (hasNext()) {
        nextVerse = next()
        if (nextVerse.text.text.isEmpty()) {
            nextVerse = null
        } else {
            break
        }
    }

    val followingEmptyVerseCount = nextVerse
            ?.let { it.verseIndex.verseIndex - 1 - current.verseIndex.verseIndex }
            ?: 0

    return Pair(nextVerse, followingEmptyVerseCount)
}

private class VersePreviewItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<VersePreviewItem, ItemVersePreviewBinding>(ItemVersePreviewBinding.inflate(inflater, parent, false)) {
    init {
        viewBinding.root.setOnClickListener { item?.let { callback().openVerse(it.verse.verseIndex) } }
    }

    private fun callback(): VersePreviewItem.Callback = (itemView.activity as? VersePreviewItem.Callback)
            ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement VersePreviewItem.Callback")

    override fun bind(settings: Settings, item: VersePreviewItem, payloads: List<Any>) {
        viewBinding.versePreview.text = item.textForDisplay
        viewBinding.versePreview.setPrimaryTextSize(settings)
    }
}
