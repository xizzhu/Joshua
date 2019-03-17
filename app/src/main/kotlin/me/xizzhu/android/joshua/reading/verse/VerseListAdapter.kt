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
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import java.lang.StringBuilder

class VerseListAdapter(private val inflater: LayoutInflater) : RecyclerView.Adapter<VerseItemViewHolder>() {
    companion object {
        const val VERSE_SELECTED = 1
        const val VERSE_DESELECTED = 2

        @IntDef(VERSE_SELECTED, VERSE_DESELECTED)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Payload
    }

    private val verses = ArrayList<Verse>()
    private val selected = ArrayList<Boolean>()

    fun setVerses(verses: List<Verse>) {
        this.verses.clear()
        this.verses.addAll(verses)

        selected.ensureCapacity(verses.size)
        for (i in 0 until selected.size) {
            selected[i] = false
        }
        for (i in selected.size until verses.size) {
            selected.add(false)
        }

        notifyDataSetChanged()
    }

    fun selectVerse(verseIndex: VerseIndex) {
        selected[verseIndex.verseIndex] = true
        notifyItemChanged(verseIndex.verseIndex, VERSE_SELECTED)
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        selected[verseIndex.verseIndex] = false
        notifyItemChanged(verseIndex.verseIndex, VERSE_DESELECTED)
    }

    override fun getItemCount(): Int = verses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseItemViewHolder =
            VerseItemViewHolder(inflater, parent)

    override fun onBindViewHolder(holder: VerseItemViewHolder, position: Int) {
        holder.bind(verses[position], itemCount, selected[position])
    }

    override fun onBindViewHolder(holder: VerseItemViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        for (payload in payloads) {
            when (payload as Int) {
                VERSE_SELECTED -> holder.setSelected(true)
                VERSE_DESELECTED -> holder.setSelected(false)
            }
        }
    }
}

class VerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_verse, parent, false)) {
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

    private val index = itemView.findViewById(R.id.index) as TextView
    private val text = itemView.findViewById(R.id.text) as TextView
    private val divider = itemView.findViewById(R.id.divider) as View

    fun bind(verse: Verse, totalVerse: Int, selected: Boolean) {
        if (verse.parallel.isEmpty()) {
            STRING_BUILDER.setLength(0)
            val verseIndex = verse.verseIndex.verseIndex
            if (totalVerse >= 10) {
                if (totalVerse < 100) {
                    if (verseIndex + 1 < 10) {
                        STRING_BUILDER.append(' ')
                    }
                } else {
                    if (verseIndex + 1 < 10) {
                        STRING_BUILDER.append("  ")
                    } else if (verseIndex + 1 < 100) {
                        STRING_BUILDER.append(" ")
                    }
                }
            }
            STRING_BUILDER.append(verseIndex + 1)
            index.text = STRING_BUILDER.toString()
            index.visibility = View.VISIBLE

            text.text = verse.text.text

            divider.visibility = View.GONE
        } else {
            index.visibility = View.GONE

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
            SPANNABLE_STRING_BUILDER.setSpan(PARALLEL_VERSE_SIZE_SPAN, primaryTextLength, length, 0)
            text.text = SPANNABLE_STRING_BUILDER.subSequence(0, length)

            divider.visibility = View.VISIBLE
        }

        setSelected(selected)
    }

    fun setSelected(selected: Boolean) {
        itemView.isSelected = selected
    }
}
