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

package me.xizzhu.android.joshua.preview

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import java.util.concurrent.Executor
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemPreviewVerseBinding
import me.xizzhu.android.joshua.databinding.ItemPreviewVerseWithQueryBinding
import me.xizzhu.android.joshua.search.highlightKeyword
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createTitleSpans
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewAdapter
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewItem
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence

class PreviewAdapter(
    private val inflater: LayoutInflater,
    executor: Executor,
    private val onViewEvent: (ViewEvent) -> Unit,
) : VerticalRecyclerViewAdapter<PreviewItem, PreviewViewHolder<PreviewItem, *>>(PreviewItem.DiffCallback(), executor) {
    sealed class ViewEvent {
        data class OpenVerse(val verseToOpen: VerseIndex) : ViewEvent()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder<PreviewItem, *> = when (viewType) {
        PreviewItem.Verse.VIEW_TYPE -> PreviewViewHolder.Verse(inflater, parent, onViewEvent)
        PreviewItem.VerseWithQuery.VIEW_TYPE -> PreviewViewHolder.VerseWithQuery(inflater, parent, onViewEvent)
        else -> throw IllegalStateException("Unknown view type - $viewType")
    } as PreviewViewHolder<PreviewItem, *>
}

sealed class PreviewItem(viewType: Int) : VerticalRecyclerViewItem(viewType) {
    companion object {
        private val VERSE_INDEX_SPANS = createTitleSpans()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    class DiffCallback : DiffUtil.ItemCallback<PreviewItem>() {
        override fun areItemsTheSame(oldItem: PreviewItem, newItem: PreviewItem): Boolean = when {
            oldItem is Verse && newItem is Verse -> oldItem.verseIndex == newItem.verseIndex
            oldItem is VerseWithQuery && newItem is VerseWithQuery -> oldItem.verseIndex == newItem.verseIndex
            else -> false
        }

        override fun areContentsTheSame(oldItem: PreviewItem, newItem: PreviewItem): Boolean = oldItem == newItem
    }

    data class Verse(
        val settings: Settings,
        val verseIndex: VerseIndex,
        val verseText: String,
        private val followingEmptyVerseCount: Int
    ) : PreviewItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_preview_verse
        }

        val textForDisplay: CharSequence by lazy(LazyThreadSafetyMode.NONE) {
            // format:
            // <chapter verseIndex>:<verse verseIndex> <verse text>
            SPANNABLE_STRING_BUILDER.clearAll()

            SPANNABLE_STRING_BUILDER.append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
            if (followingEmptyVerseCount > 0) {
                SPANNABLE_STRING_BUILDER.append('-').append(verseIndex.verseIndex + followingEmptyVerseCount + 1)
            }

            SPANNABLE_STRING_BUILDER.setSpans(VERSE_INDEX_SPANS)
                .append(' ')
                .append(verseText)

            return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
        }
    }

    data class VerseWithQuery(
        val settings: Settings,
        val verseIndex: VerseIndex,
        val verseText: String,
        val query: String,
        private val followingEmptyVerseCount: Int
    ) : PreviewItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_preview_verse_with_query
        }

        val textForDisplay: CharSequence by lazy(LazyThreadSafetyMode.NONE) {
            // format:
            // <chapter verseIndex>:<verse verseIndex> <verse text>
            SPANNABLE_STRING_BUILDER.clearAll()

            SPANNABLE_STRING_BUILDER.append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
            if (followingEmptyVerseCount > 0) {
                SPANNABLE_STRING_BUILDER.append('-').append(verseIndex.verseIndex + followingEmptyVerseCount + 1)
            }

            SPANNABLE_STRING_BUILDER.setSpans(VERSE_INDEX_SPANS)
                .append(' ')
                .append(verseText)

            // highlights the keywords
            SPANNABLE_STRING_BUILDER.highlightKeyword(query, SPANNABLE_STRING_BUILDER.length - verseText.length)

            return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
        }
    }
}

sealed class PreviewViewHolder<Item : PreviewItem, VB : ViewBinding>(viewBinding: VB) : VerticalRecyclerViewHolder<Item, VB>(viewBinding) {
    class Verse(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (PreviewAdapter.ViewEvent) -> Unit)
        : PreviewViewHolder<PreviewItem.Verse, ItemPreviewVerseBinding>(ItemPreviewVerseBinding.inflate(inflater, parent, false)) {
        init {
            itemView.setOnClickListener {
                item?.let { item -> onViewEvent(PreviewAdapter.ViewEvent.OpenVerse(item.verseIndex)) }
            }
        }

        override fun bind(item: PreviewItem.Verse, payloads: List<Any>) = with(viewBinding.verse) {
            text = item.textForDisplay
            setPrimaryTextSize(item.settings)
        }
    }

    class VerseWithQuery(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (PreviewAdapter.ViewEvent) -> Unit)
        : PreviewViewHolder<PreviewItem.VerseWithQuery, ItemPreviewVerseWithQueryBinding>(ItemPreviewVerseWithQueryBinding.inflate(inflater, parent, false)) {
        init {
            itemView.setOnClickListener {
                item?.let { item -> onViewEvent(PreviewAdapter.ViewEvent.OpenVerse(item.verseIndex)) }
            }
        }

        override fun bind(item: PreviewItem.VerseWithQuery, payloads: List<Any>) = with(viewBinding.verse) {
            text = item.textForDisplay
            setPrimaryTextSize(item.settings)
        }
    }
}
