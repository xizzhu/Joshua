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

package me.xizzhu.android.joshua.annotated

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemAnnotatedVerseBookmarkBinding
import me.xizzhu.android.joshua.databinding.ItemAnnotatedVerseHighlightBinding
import me.xizzhu.android.joshua.databinding.ItemAnnotatedVerseNoteBinding
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

class AnnotatedVerseAdapter(
    private val inflater: LayoutInflater,
    executor: Executor,
    private val onViewEvent: (ViewEvent) -> Unit,
) : VerticalRecyclerViewAdapter<AnnotatedVerseItem, AnnotatedVerseViewHolder<AnnotatedVerseItem, *>>(AnnotatedVerseItem.DiffCallback(), executor) {
    sealed class ViewEvent {
        data class OpenVerse(val verseToOpen: VerseIndex) : ViewEvent()
        data class ShowPreview(val verseToPreview: VerseIndex) : ViewEvent()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnotatedVerseViewHolder<AnnotatedVerseItem, *> = when (viewType) {
        AnnotatedVerseItem.Header.VIEW_TYPE -> AnnotatedVerseViewHolder.Header(inflater, parent)
        AnnotatedVerseItem.Bookmark.VIEW_TYPE -> AnnotatedVerseViewHolder.Bookmark(inflater, parent, onViewEvent)
        AnnotatedVerseItem.Highlight.VIEW_TYPE -> AnnotatedVerseViewHolder.Highlight(inflater, parent, onViewEvent)
        AnnotatedVerseItem.Note.VIEW_TYPE -> AnnotatedVerseViewHolder.Note(inflater, parent, onViewEvent)
        else -> throw IllegalStateException("Unknown view type - $viewType")
    } as AnnotatedVerseViewHolder<AnnotatedVerseItem, *>
}

sealed class AnnotatedVerseItem(viewType: Int) : VerticalRecyclerViewItem(viewType) {
    companion object {
        private val BOOK_NAME_SPANS = createTitleSpans()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    class DiffCallback : DiffUtil.ItemCallback<AnnotatedVerseItem>() {
        override fun areItemsTheSame(oldItem: AnnotatedVerseItem, newItem: AnnotatedVerseItem): Boolean = when {
            oldItem is Header && newItem is Header -> oldItem.text == newItem.text
            oldItem is Bookmark && newItem is Bookmark -> oldItem.verseIndex == newItem.verseIndex
            oldItem is Highlight && newItem is Highlight -> oldItem.verseIndex == newItem.verseIndex
            oldItem is Note && newItem is Note -> oldItem.verseIndex == newItem.verseIndex
            else -> false
        }

        override fun areContentsTheSame(oldItem: AnnotatedVerseItem, newItem: AnnotatedVerseItem): Boolean = oldItem == newItem
    }

    data class Header(
        val settings: Settings,
        val text: String
    ) : AnnotatedVerseItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_title
        }
    }

    data class Bookmark(
        val settings: Settings,
        val verseIndex: VerseIndex,
        private val bookName: String,
        private val bookShortName: String,
        private val verseText: String,
        @Constants.SortOrder private val sortOrder: Int
    ) : AnnotatedVerseItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_annotated_verse_bookmark
        }

        val textForDisplay: CharSequence by lazy {
            SPANNABLE_STRING_BUILDER.clearAll()

            if (sortOrder == Constants.SORT_BY_BOOK) {
                // format:
                // <book short name> <chapter verseIndex>:<verse verseIndex> <verse text>
                SPANNABLE_STRING_BUILDER.append(bookShortName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .setSpans(BOOK_NAME_SPANS)
                    .append(' ').append(verseText)
            } else {
                // format:
                // <book name> <chapter verseIndex>:<verse verseIndex>
                // <verse text>
                SPANNABLE_STRING_BUILDER.append(bookName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .setSpans(BOOK_NAME_SPANS)
                    .append('\n').append(verseText)
            }

            return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
        }
    }

    data class Highlight(
        val settings: Settings,
        val verseIndex: VerseIndex,
        private val bookName: String,
        private val bookShortName: String,
        private val verseText: String,
        @ColorInt private val highlightColor: Int,
        @Constants.SortOrder private val sortOrder: Int
    ) : AnnotatedVerseItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_annotated_verse_highlight
        }

        val textForDisplay: CharSequence by lazy {
            SPANNABLE_STRING_BUILDER.clearAll()

            if (sortOrder == Constants.SORT_BY_BOOK) {
                // format:
                // <book short name> <chapter verseIndex>:<verse verseIndex> <verse text>
                SPANNABLE_STRING_BUILDER.append(bookShortName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .setSpans(BOOK_NAME_SPANS)
                    .append(' ')
            } else {
                // format:
                // <book name> <chapter verseIndex>:<verse verseIndex>
                // <verse text>
                SPANNABLE_STRING_BUILDER.append(bookName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .setSpans(BOOK_NAME_SPANS)
                    .append('\n')
            }

            return@lazy SPANNABLE_STRING_BUILDER.append(verseText)
                .setSpans(createHighlightSpans(highlightColor), SPANNABLE_STRING_BUILDER.length - verseText.length, SPANNABLE_STRING_BUILDER.length)
                .toCharSequence()
        }
    }

    data class Note(
        val settings: Settings,
        val verseIndex: VerseIndex,
        private val bookShortName: String,
        private val verseText: String,
        val note: String
    ) : AnnotatedVerseItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_annotated_verse_note

            private val BOOK_NAME_STYLE_SPAN = createTitleStyleSpan()
        }

        val textForDisplay: CharSequence by lazy {
            // format:
            // <book short name> <chapter index>:<verse index> <verse text>
            return@lazy SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName).append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpan(BOOK_NAME_STYLE_SPAN)
                .append(' ').append(verseText)
                .toCharSequence()
        }
    }
}

sealed class AnnotatedVerseViewHolder<Item : AnnotatedVerseItem, VB : ViewBinding>(viewBinding: VB)
    : VerticalRecyclerViewHolder<Item, VB>(viewBinding) {
    class Header(inflater: LayoutInflater, parent: ViewGroup) : AnnotatedVerseViewHolder<AnnotatedVerseItem.Header, ItemTitleBinding>(
        ItemTitleBinding.inflate(inflater, parent, false)
    ) {
        init {
            viewBinding.divider.isVisible = true
        }

        override fun bind(item: AnnotatedVerseItem.Header, payloads: List<Any>) = with(viewBinding.title) {
            setSecondaryTextSize(item.settings)
            text = item.text
        }
    }

    class Bookmark(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (AnnotatedVerseAdapter.ViewEvent) -> Unit)
        : AnnotatedVerseViewHolder<AnnotatedVerseItem.Bookmark, ItemAnnotatedVerseBookmarkBinding>(
        ItemAnnotatedVerseBookmarkBinding.inflate(inflater, parent, false)
    ) {
        init {
            itemView.setOnClickListener {
                item?.let { onViewEvent(AnnotatedVerseAdapter.ViewEvent.OpenVerse(it.verseIndex)) }
            }
            itemView.setOnLongClickListener {
                item?.let { onViewEvent(AnnotatedVerseAdapter.ViewEvent.ShowPreview(it.verseIndex)) }
                true
            }
        }

        override fun bind(item: AnnotatedVerseItem.Bookmark, payloads: List<Any>) = with(viewBinding.text) {
            setPrimaryTextSize(item.settings)
            text = item.textForDisplay
        }
    }

    class Highlight(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (AnnotatedVerseAdapter.ViewEvent) -> Unit)
        : AnnotatedVerseViewHolder<AnnotatedVerseItem.Highlight, ItemAnnotatedVerseHighlightBinding>(
        ItemAnnotatedVerseHighlightBinding.inflate(inflater, parent, false)
    ) {
        init {
            itemView.setOnClickListener {
                item?.let { onViewEvent(AnnotatedVerseAdapter.ViewEvent.OpenVerse(it.verseIndex)) }
            }
            itemView.setOnLongClickListener {
                item?.let { onViewEvent(AnnotatedVerseAdapter.ViewEvent.ShowPreview(it.verseIndex)) }
                true
            }
        }

        override fun bind(item: AnnotatedVerseItem.Highlight, payloads: List<Any>) = with(viewBinding.text) {
            setPrimaryTextSize(item.settings)
            text = item.textForDisplay
        }
    }

    class Note(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (AnnotatedVerseAdapter.ViewEvent) -> Unit)
        : AnnotatedVerseViewHolder<AnnotatedVerseItem.Note, ItemAnnotatedVerseNoteBinding>(ItemAnnotatedVerseNoteBinding.inflate(inflater, parent, false)) {
        init {
            itemView.setOnClickListener {
                item?.let { onViewEvent(AnnotatedVerseAdapter.ViewEvent.OpenVerse(it.verseIndex)) }
            }
            itemView.setOnLongClickListener {
                item?.let { onViewEvent(AnnotatedVerseAdapter.ViewEvent.ShowPreview(it.verseIndex)) }
                true
            }
        }

        override fun bind(item: AnnotatedVerseItem.Note, payloads: List<Any>) = with(viewBinding) {
            verse.setSecondaryTextSize(item.settings)
            verse.text = item.textForDisplay

            text.setPrimaryTextSize(item.settings)
            text.text = item.note
        }
    }
}
