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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import java.lang.StringBuilder

data class SimpleVerseItem(val verse: Verse, private val totalVerseCount: Int, @ColorInt var highlightColor: Int,
                           val onClicked: (Verse) -> Unit, val onLongClicked: (Verse) -> Unit,
                           var selected: Boolean = false)
    : BaseItem(R.layout.item_simple_verse, { inflater, parent -> SimpleVerseItemViewHolder(inflater, parent) }) {
    companion object {
        private val STRING_BUILDER = StringBuilder()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    val indexForDisplay: CharSequence by lazy {
        if (verse.parallel.isEmpty()) {
            STRING_BUILDER.setLength(0)
            val verseIndex = verse.verseIndex.verseIndex
            if (totalVerseCount >= 10) {
                if (totalVerseCount < 100) {
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
            return@lazy STRING_BUILDER.toString()
        } else {
            return@lazy ""
        }
    }

    val textForDisplay: CharSequence by lazy { SPANNABLE_STRING_BUILDER.format(verse, true, highlightColor) }
}

private class SimpleVerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SimpleVerseItem>(inflater.inflate(R.layout.item_simple_verse, parent, false)) {
    private val resources = itemView.resources
    private val index = itemView.findViewById(R.id.index) as TextView
    private val text = itemView.findViewById(R.id.text) as TextView
    private val divider = itemView.findViewById(R.id.divider) as View

    init {
        itemView.setOnClickListener { item?.let { it.onClicked(it.verse) } }
        itemView.setOnLongClickListener {
            item?.let { it.onLongClicked(it.verse) }
            return@setOnLongClickListener true
        }
    }

    override fun bind(settings: Settings, item: SimpleVerseItem, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            text.text = item.textForDisplay
            text.setTextColor(if (item.selected) settings.getPrimarySelectedTextColor(resources) else settings.getPrimaryTextColor(resources))
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))

            if (item.verse.parallel.isEmpty()) {
                index.text = item.indexForDisplay
                index.setTextColor(if (item.selected) settings.getPrimarySelectedTextColor(resources) else settings.getPrimaryTextColor(resources))
                index.setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))
                index.visibility = View.VISIBLE

                divider.visibility = View.GONE
            } else {
                index.visibility = View.GONE
                divider.visibility = View.VISIBLE
            }

            itemView.isSelected = item.selected
        } else {
            payloads.forEach { payload ->
                when (payload as Int) {
                    VerseItemViewHolder.VERSE_SELECTED -> {
                        item.selected = true
                        itemView.isSelected = true
                        if (settings.nightModeOn) {
                            index.animateTextColor(settings.getPrimarySelectedTextColor(resources))
                            text.animateTextColor(settings.getPrimarySelectedTextColor(resources))
                        }
                    }
                    VerseItemViewHolder.VERSE_DESELECTED -> {
                        item.selected = false
                        itemView.isSelected = false
                        if (settings.nightModeOn) {
                            index.animateTextColor(settings.getPrimaryTextColor(resources))
                            text.animateTextColor(settings.getPrimaryTextColor(resources))
                        }
                    }
                }
            }
        }
    }
}
