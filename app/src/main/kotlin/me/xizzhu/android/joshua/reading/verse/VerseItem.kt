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

package me.xizzhu.android.joshua.reading.verse

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Color
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.databinding.ItemVerseBinding
import me.xizzhu.android.joshua.reading.VerseUpdate
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

class VerseItem(
        val verse: Verse, private val followingEmptyVerseCount: Int,
        var hasBookmark: Boolean, @Highlight.Companion.AvailableColor var highlightColor: Int, var hasNote: Boolean,
        var selected: Boolean = false
) : BaseItem(R.layout.item_verse, { inflater, parent -> VerseItemViewHolder(inflater, parent) }) {
    companion object {
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun onVerseClicked(verse: Verse)

        fun onVerseLongClicked(verse: Verse)

        fun onBookmarkClicked(verseIndex: VerseIndex, currentlyBookmarked: Boolean)

        fun onHighlightClicked(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor currentHighlightColor: Int)

        fun onNoteClicked(verseIndex: VerseIndex)
    }

    var textForDisplay: CharSequence = ""
        get() {
            if (field.isEmpty()) {
                field = SPANNABLE_STRING_BUILDER.format(verse, followingEmptyVerseCount, false, highlightColor)
            }
            return field
        }
}

private class VerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<VerseItem, ItemVerseBinding>(ItemVerseBinding.inflate(inflater, parent, false)) {
    companion object {
        private val ON = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)
        private val OFF = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }

    init {
        viewBinding.root.setOnClickListener { item?.let { callback().onVerseClicked(it.verse) } }
        viewBinding.root.setOnLongClickListener {
            return@setOnLongClickListener item?.let {
                callback().onVerseLongClicked(it.verse)
                true
            } ?: false
        }
        viewBinding.bookmark.setOnClickListener { item?.let { callback().onBookmarkClicked(it.verse.verseIndex, it.hasBookmark) } }
        viewBinding.highlight.setOnClickListener { item?.let { callback().onHighlightClicked(it.verse.verseIndex, it.highlightColor) } }
        viewBinding.note.setOnClickListener { item?.let { callback().onNoteClicked(it.verse.verseIndex) } }
    }

    private fun callback(): VerseItem.Callback = (itemView.activity as? VerseItem.Callback)
            ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement VerseItem.Callback")

    override fun bind(settings: Settings, item: VerseItem, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            viewBinding.text.text = item.textForDisplay
            viewBinding.text.setPrimaryTextSize(settings)

            viewBinding.bookmark.colorFilter = if (item.hasBookmark) ON else OFF
            viewBinding.highlight.colorFilter = if (item.highlightColor != Highlight.COLOR_NONE) ON else OFF
            viewBinding.note.colorFilter = if (item.hasNote) ON else OFF

            itemView.isSelected = item.selected
        } else {
            payloads.forEach { payload ->
                val update = payload as VerseUpdate
                when (update.operation) {
                    VerseUpdate.VERSE_SELECTED -> {
                        item.selected = true
                        viewBinding.root.isSelected = true
                        viewBinding.text.isSelected = true
                    }
                    VerseUpdate.VERSE_DESELECTED -> {
                        item.selected = false
                        viewBinding.root.isSelected = false
                        viewBinding.text.isSelected = false
                    }
                    VerseUpdate.NOTE_ADDED -> {
                        item.hasNote = true
                        viewBinding.note.colorFilter = ON
                    }
                    VerseUpdate.NOTE_REMOVED -> {
                        item.hasNote = false
                        viewBinding.note.colorFilter = OFF
                    }
                    VerseUpdate.BOOKMARK_ADDED -> {
                        item.hasBookmark = true
                        viewBinding.bookmark.colorFilter = ON
                    }
                    VerseUpdate.BOOKMARK_REMOVED -> {
                        item.hasBookmark = false
                        viewBinding.bookmark.colorFilter = OFF
                    }
                    VerseUpdate.HIGHLIGHT_UPDATED -> {
                        item.highlightColor = update.data as Int
                        item.textForDisplay = ""
                        viewBinding.text.text = item.textForDisplay
                        viewBinding.highlight.colorFilter = if (item.highlightColor != Highlight.COLOR_NONE) ON else OFF
                    }
                }
            }
        }
    }
}
