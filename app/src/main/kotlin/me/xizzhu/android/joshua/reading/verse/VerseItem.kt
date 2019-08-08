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

package me.xizzhu.android.joshua.reading.verse

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.reading.VerseUpdate
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

data class VerseItem(val verse: Verse, private val bookName: String, private val followingEmptyVerseCount: Int,
                     var hasNote: Boolean, @ColorInt var highlightColor: Int, var hasBookmark: Boolean,
                     val onClicked: (Verse) -> Unit, val onLongClicked: (Verse) -> Unit,
                     val onNoteClicked: (VerseIndex) -> Unit, val onHighlightClicked: (VerseIndex, Int) -> Unit,
                     val onBookmarkClicked: (VerseIndex, Boolean) -> Unit, var selected: Boolean = false)
    : BaseItem(R.layout.item_verse, { inflater, parent -> VerseItemViewHolder(inflater, parent) }) {
    companion object {
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    var textForDisplay: CharSequence = ""
        get() {
            if (field.isEmpty()) {
                field = SPANNABLE_STRING_BUILDER.format(verse, bookName, followingEmptyVerseCount, false, highlightColor)
            }
            return field
        }
}

private class VerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<VerseItem>(inflater.inflate(R.layout.item_verse, parent, false)) {
    companion object {
        private val ON = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)
        private val OFF = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }

    private val resources = itemView.resources
    private val text = itemView.findViewById<TextView>(R.id.text)
    private val bookmark = itemView.findViewById<ImageView>(R.id.bookmark)
    private val highlight = itemView.findViewById<ImageView>(R.id.highlight)
    private val note = itemView.findViewById<ImageView>(R.id.note)

    init {
        itemView.setOnClickListener { item?.let { it.onClicked(it.verse) } }
        itemView.setOnLongClickListener {
            item?.let { it.onLongClicked(it.verse) }
            return@setOnLongClickListener true
        }
        bookmark.setOnClickListener { item?.let { it.onBookmarkClicked(it.verse.verseIndex, it.hasBookmark) } }
        highlight.setOnClickListener { item?.let { it.onHighlightClicked(it.verse.verseIndex, it.highlightColor) } }
        note.setOnClickListener { item?.let { it.onNoteClicked(it.verse.verseIndex) } }
    }

    override fun bind(settings: Settings, item: VerseItem, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            text.text = item.textForDisplay
            text.setTextColor(if (item.selected) settings.getPrimarySelectedTextColor(resources) else settings.getPrimaryTextColor(resources))
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))

            bookmark.colorFilter = if (item.hasBookmark) ON else OFF
            highlight.colorFilter = if (item.highlightColor != Highlight.COLOR_NONE) ON else OFF
            note.colorFilter = if (item.hasNote) ON else OFF

            itemView.isSelected = item.selected
        } else {
            payloads.forEach { payload ->
                val update = payload as VerseUpdate
                when (update.operation) {
                    VerseUpdate.VERSE_SELECTED -> {
                        item.selected = true
                        itemView.isSelected = true
                        if (settings.nightModeOn) {
                            text.animateTextColor(settings.getPrimarySelectedTextColor(resources))
                        }
                    }
                    VerseUpdate.VERSE_DESELECTED -> {
                        item.selected = false
                        itemView.isSelected = false
                        if (settings.nightModeOn) {
                            text.animateTextColor(settings.getPrimaryTextColor(resources))
                        }
                    }
                    VerseUpdate.NOTE_ADDED -> {
                        item.hasNote = true
                        note.colorFilter = ON
                    }
                    VerseUpdate.NOTE_REMOVED -> {
                        item.hasNote = false
                        note.colorFilter = OFF
                    }
                    VerseUpdate.BOOKMARK_ADDED -> {
                        item.hasBookmark = true
                        bookmark.colorFilter = ON
                    }
                    VerseUpdate.BOOKMARK_REMOVED -> {
                        item.hasBookmark = false
                        bookmark.colorFilter = OFF
                    }
                    VerseUpdate.HIGHLIGHT_UPDATED -> {
                        item.highlightColor = update.data as Int
                        item.textForDisplay = ""
                        text.text = item.textForDisplay
                        highlight.colorFilter = if (item.highlightColor != Highlight.COLOR_NONE) ON else OFF
                    }
                }
            }
        }
    }
}
