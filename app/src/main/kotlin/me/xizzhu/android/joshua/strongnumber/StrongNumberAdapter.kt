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

package me.xizzhu.android.joshua.strongnumber

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemStrongNumberVerseBinding
import me.xizzhu.android.joshua.databinding.ItemTextBinding
import me.xizzhu.android.joshua.databinding.ItemTitleBinding
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createTitleSpans
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewAdapter
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewItem
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSecondaryTextSize
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence
import java.util.concurrent.Executor

class StrongNumberAdapter(
    private val inflater: LayoutInflater,
    executor: Executor,
    private val onViewEvent: (ViewEvent) -> Unit,
) : VerticalRecyclerViewAdapter<StrongNumberItem, StrongNumberViewHolder<StrongNumberItem, *>>(StrongNumberItem.DiffCallback(), executor) {
    sealed class ViewEvent {
        data class OpenVerse(val verseToOpen: VerseIndex) : ViewEvent()
        data class ShowPreview(val verseToPreview: VerseIndex) : ViewEvent()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StrongNumberViewHolder<StrongNumberItem, *> = when (viewType) {
        StrongNumberItem.StrongNumber.VIEW_TYPE -> StrongNumberViewHolder.StrongNumber(inflater, parent)
        StrongNumberItem.BookName.VIEW_TYPE -> StrongNumberViewHolder.BookName(inflater, parent)
        StrongNumberItem.Verse.VIEW_TYPE -> StrongNumberViewHolder.Verse(inflater, parent, onViewEvent)
        else -> throw IllegalStateException("Unknown view type - $viewType")
    } as StrongNumberViewHolder<StrongNumberItem, *>
}

sealed class StrongNumberItem(viewType: Int) : VerticalRecyclerViewItem(viewType) {
    class DiffCallback : DiffUtil.ItemCallback<StrongNumberItem>() {
        override fun areItemsTheSame(oldItem: StrongNumberItem, newItem: StrongNumberItem): Boolean = when {
            oldItem is StrongNumber && newItem is StrongNumber -> true
            oldItem is BookName && newItem is BookName -> true
            oldItem is Verse && newItem is Verse -> oldItem.verseIndex == newItem.verseIndex
            else -> false
        }

        override fun areContentsTheSame(oldItem: StrongNumberItem, newItem: StrongNumberItem): Boolean = oldItem == newItem
    }

    data class StrongNumber(
        val settings: Settings,
        val text: CharSequence,
    ) : StrongNumberItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_text
        }
    }

    data class BookName(
        val settings: Settings,
        val bookName: String,
    ) : StrongNumberItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_title
        }
    }

    data class Verse(
        val settings: Settings,
        val verseIndex: VerseIndex,
        private val bookShortName: String,
        private val verseText: String
    ) : StrongNumberItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_strong_number_verse

            private val BOOK_NAME_SPANS = createTitleSpans()
            private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
        }

        val textForDisplay: CharSequence by lazy(LazyThreadSafetyMode.NONE) {
            // format:
            // <book short name> <chapter verseIndex>:<verse verseIndex> <verse text>
            return@lazy SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName).append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpans(BOOK_NAME_SPANS)
                .append(' ').append(verseText)
                .toCharSequence()
        }
    }
}

sealed class StrongNumberViewHolder<Item : StrongNumberItem, VB : ViewBinding>(viewBinding: VB) : VerticalRecyclerViewHolder<Item, VB>(viewBinding) {
    class StrongNumber(inflater: LayoutInflater, parent: ViewGroup) : StrongNumberViewHolder<StrongNumberItem.StrongNumber, ItemTextBinding>(
        ItemTextBinding.inflate(inflater, parent, false)
    ) {
        override fun bind(item: StrongNumberItem.StrongNumber, payloads: List<Any>) = with(viewBinding.title) {
            setPrimaryTextSize(item.settings)
            text = item.text
        }
    }

    class BookName(inflater: LayoutInflater, parent: ViewGroup) : StrongNumberViewHolder<StrongNumberItem.BookName, ItemTitleBinding>(
        ItemTitleBinding.inflate(inflater, parent, false)
    ) {
        init {
            viewBinding.divider.isVisible = true
        }

        override fun bind(item: StrongNumberItem.BookName, payloads: List<Any>) = with(viewBinding.title) {
            setSecondaryTextSize(item.settings)
            text = item.bookName
        }
    }

    class Verse(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (StrongNumberAdapter.ViewEvent) -> Unit)
        : StrongNumberViewHolder<StrongNumberItem.Verse, ItemStrongNumberVerseBinding>(ItemStrongNumberVerseBinding.inflate(inflater, parent, false)
    ) {
        init {
            itemView.setOnClickListener {
                item?.let { onViewEvent(StrongNumberAdapter.ViewEvent.OpenVerse(it.verseIndex)) }
            }
            itemView.setOnLongClickListener {
                item?.let { onViewEvent(StrongNumberAdapter.ViewEvent.ShowPreview(it.verseIndex)) }
                true
            }
        }

        override fun bind(item: StrongNumberItem.Verse, payloads: List<Any>) = with(viewBinding.text) {
            setPrimaryTextSize(item.settings)
            text = item.textForDisplay
        }
    }
}
