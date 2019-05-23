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

package me.xizzhu.android.joshua.ui.recyclerview

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText
import java.lang.StringBuilder
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Color


data class VerseItem(val verse: Verse, var hasBookmark: Boolean, var hasNote: Boolean,
                     val onClicked: (Verse) -> Unit, val onLongClicked: (Verse) -> Unit,
                     val onNoteClicked: (VerseIndex) -> Unit, val onBookmarkClicked: (VerseIndex, Boolean) -> Unit,
                     var selected: Boolean = false) : BaseItem {
    companion object {
        private val STRING_BUILDER = StringBuilder()
        private val PARALLEL_VERSE_SIZE_SPAN = RelativeSizeSpan(0.95F)
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()

        private fun buildVerseForDisplay(out: StringBuilder, verseIndex: VerseIndex, text: Verse.Text) {
            if (out.isNotEmpty()) {
                out.append('\n').append('\n')
            }
            out.append(text.translationShortName).append(' ')
                    .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                    .append('\n').append(text.text)
        }
    }

    val textForDisplay: CharSequence by lazy {
        if (verse.parallel.isEmpty()) {
            // format:
            // <book name> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            STRING_BUILDER.setLength(0)
            STRING_BUILDER.append(verse.text.bookName).append(' ')
                    .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1).append('\n')
                    .append(verse.text.text)
            return@lazy STRING_BUILDER.toString()
        } else {
            // format:
            // <primary translation> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            // <empty line>
            // <parallel translation 1> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            // <parallel translation 2> <chapter verseIndex>:<verse verseIndex>
            // <verse text>

            STRING_BUILDER.setLength(0)
            buildVerseForDisplay(STRING_BUILDER, verse.verseIndex, verse.text)
            val primaryTextLength = STRING_BUILDER.length

            for (text in verse.parallel) {
                buildVerseForDisplay(STRING_BUILDER, verse.verseIndex, text)
            }

            SPANNABLE_STRING_BUILDER.clear()
            SPANNABLE_STRING_BUILDER.clearSpans()
            SPANNABLE_STRING_BUILDER.append(STRING_BUILDER)
            val length = SPANNABLE_STRING_BUILDER.length
            SPANNABLE_STRING_BUILDER.setSpan(PARALLEL_VERSE_SIZE_SPAN, primaryTextLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return@lazy SPANNABLE_STRING_BUILDER.subSequence(0, length)
        }
    }

    override fun getItemViewType(): Int = BaseItem.VERSE_ITEM
}

class VerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<VerseItem>(inflater.inflate(R.layout.item_verse, parent, false)) {
    companion object {
        const val VERSE_SELECTED = 1
        const val VERSE_DESELECTED = 2
        const val NOTE_ADDED = 3
        const val NOTE_REMOVED = 4
        const val BOOKMARK_ADDED = 5
        const val BOOKMARK_REMOVED = 6

        private val ON = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)
        private val OFF = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }

    private val text = itemView.findViewById<TextView>(R.id.text)
    private val bookmark = itemView.findViewById<ImageView>(R.id.bookmark)
    private val note = itemView.findViewById<ImageView>(R.id.note)

    init {
        itemView.setOnClickListener { item?.let { it.onClicked(it.verse) } }
        itemView.setOnLongClickListener {
            item?.let { it.onLongClicked(it.verse) }
            return@setOnLongClickListener true
        }
        note.setOnClickListener { item?.let { it.onNoteClicked(it.verse.verseIndex) } }
        bookmark.setOnClickListener { item?.let { it.onBookmarkClicked(it.verse.verseIndex, it.hasBookmark) } }
    }

    override fun bind(settings: Settings, item: VerseItem, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            text.updateSettingsWithPrimaryText(settings)
            text.text = item.textForDisplay

            bookmark.colorFilter = if (item.hasBookmark) ON else OFF
            note.colorFilter = if (item.hasNote) ON else OFF

            itemView.isSelected = item.selected
        } else {
            payloads.forEach { payload ->
                when (payload as Int) {
                    VERSE_SELECTED -> {
                        item.selected = true
                        itemView.isSelected = true
                    }
                    VERSE_DESELECTED -> {
                        item.selected = false
                        itemView.isSelected = false
                    }
                    NOTE_ADDED -> {
                        item.hasNote = true
                        note.colorFilter = ON
                    }
                    NOTE_REMOVED -> {
                        item.hasNote = false
                        note.colorFilter = OFF
                    }
                    BOOKMARK_ADDED -> {
                        item.hasBookmark = true
                        bookmark.colorFilter = ON
                    }
                    BOOKMARK_REMOVED -> {
                        item.hasBookmark = false
                        bookmark.colorFilter = OFF
                    }
                }
            }
        }
    }
}
