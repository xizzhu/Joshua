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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemSearchNoteBinding
import me.xizzhu.android.joshua.databinding.ItemSearchVerseBinding
import me.xizzhu.android.joshua.databinding.ItemTitleBinding
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createHighlightSpans
import me.xizzhu.android.joshua.ui.createTitleSpans
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewAdapter
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewItem
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSecondaryTextSize
import me.xizzhu.android.joshua.ui.setSpan
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence
import java.util.concurrent.Executor

class SearchAdapter(
    private val inflater: LayoutInflater,
    executor: Executor,
    private val onViewEvent: (ViewEvent) -> Unit,
) : VerticalRecyclerViewAdapter<SearchItem, SearchViewHolder<SearchItem, *>>(SearchItem.DiffCallback(), executor) {
    sealed class ViewEvent {
        data class OpenVerse(val verseToOpen: VerseIndex) : ViewEvent()
        data class ShowPreview(val verseToPreview: VerseIndex) : ViewEvent()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder<SearchItem, *> = when (viewType) {
        SearchItem.Header.VIEW_TYPE -> SearchViewHolder.Header(inflater, parent)
        SearchItem.Note.VIEW_TYPE -> SearchViewHolder.Note(inflater, parent, onViewEvent)
        SearchItem.Verse.VIEW_TYPE -> SearchViewHolder.Verse(inflater, parent, onViewEvent)
        else -> throw IllegalStateException("Unknown view type - $viewType")
    } as SearchViewHolder<SearchItem, *>
}

sealed class SearchItem(viewType: Int) : VerticalRecyclerViewItem(viewType) {
    companion object {
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchItem>() {
        override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean = when {
            oldItem is Header && newItem is Header -> oldItem.text == newItem.text
            oldItem is Note && newItem is Note -> oldItem.verseIndex == newItem.verseIndex
            oldItem is Verse && newItem is Verse -> oldItem.verseIndex == newItem.verseIndex
            else -> false
        }

        override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean = oldItem == newItem
    }

    data class Header(
        val settings: Settings,
        val text: String,
    ) : SearchItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_title
        }
    }

    data class Note(
        val settings: Settings,
        val verseIndex: VerseIndex,
        private val bookShortName: String,
        private val verseText: String,
        private val query: String,
        private val note: String,
    ) : SearchItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_search_note

            private val BOOK_NAME_SPAN = createTitleStyleSpan()
        }

        val verseForDisplay: CharSequence by lazy {
            // format:
            // <book short name> <chapter index>:<verse index> <verse text>
            return@lazy SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName).append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpan(BOOK_NAME_SPAN)
                .append(' ').append(verseText)
                .toCharSequence()
        }

        val noteForDisplay: CharSequence by lazy {
            SPANNABLE_STRING_BUILDER.clearAll().append(note)
                .highlightKeyword(query, 0) // highlights the keywords

            return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
        }
    }

    data class Verse(
        val settings: Settings,
        val verseIndex: VerseIndex,
        private val bookShortName: String,
        private val verseText: String,
        private val query: String,
        @ColorInt private val highlightColor: Int,
    ) : SearchItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_search_verse

            private val BOOK_NAME_SPAN = createTitleSpans()
        }

        val textForDisplay: CharSequence by lazy {
            // format:
            // <short book name> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName)
                .append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpans(BOOK_NAME_SPAN)
                .append('\n')
                .append(verseText)

            // highlights the keywords
            SPANNABLE_STRING_BUILDER.highlightKeyword(query, SPANNABLE_STRING_BUILDER.length - verseText.length)

            // highlights the verse
            SPANNABLE_STRING_BUILDER.setSpans(
                createHighlightSpans(highlightColor), SPANNABLE_STRING_BUILDER.length - verseText.length, SPANNABLE_STRING_BUILDER.length
            )

            return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
        }
    }
}

sealed class SearchViewHolder<Item : SearchItem, VB : ViewBinding>(viewBinding: VB) : VerticalRecyclerViewHolder<Item, VB>(viewBinding) {
    class Header(inflater: LayoutInflater, parent: ViewGroup)
        : SearchViewHolder<SearchItem.Header, ItemTitleBinding>(ItemTitleBinding.inflate(inflater, parent, false)) {
        init {
            viewBinding.divider.isVisible = true
        }

        override fun bind(item: SearchItem.Header, payloads: List<Any>) = with(viewBinding.title) {
            setSecondaryTextSize(item.settings)
            text = item.text
        }
    }

    class Note(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (SearchAdapter.ViewEvent) -> Unit)
        : SearchViewHolder<SearchItem.Note, ItemSearchNoteBinding>(ItemSearchNoteBinding.inflate(inflater, parent, false)) {
        init {
            itemView.setOnClickListener {
                item?.let { onViewEvent(SearchAdapter.ViewEvent.OpenVerse(it.verseIndex)) }
            }
            itemView.setOnLongClickListener {
                item?.let { onViewEvent(SearchAdapter.ViewEvent.ShowPreview(it.verseIndex)) }
                true
            }
        }

        override fun bind(item: SearchItem.Note, payloads: List<Any>) = with(viewBinding) {
            verse.setSecondaryTextSize(item.settings)
            verse.text = item.verseForDisplay

            note.setPrimaryTextSize(item.settings)
            note.text = item.noteForDisplay
        }
    }

    class Verse(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (SearchAdapter.ViewEvent) -> Unit)
        : SearchViewHolder<SearchItem.Verse, ItemSearchVerseBinding>(ItemSearchVerseBinding.inflate(inflater, parent, false)) {
        init {
            itemView.setOnClickListener {
                item?.let { onViewEvent(SearchAdapter.ViewEvent.OpenVerse(it.verseIndex)) }
            }
            itemView.setOnLongClickListener {
                item?.let { onViewEvent(SearchAdapter.ViewEvent.ShowPreview(it.verseIndex)) }
                true
            }
        }

        override fun bind(item: SearchItem.Verse, payloads: List<Any>) = with(viewBinding.root) {
            setPrimaryTextSize(item.settings)
            text = item.textForDisplay
        }
    }
}
