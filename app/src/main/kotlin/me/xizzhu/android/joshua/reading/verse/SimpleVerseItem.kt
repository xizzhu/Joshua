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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.databinding.ItemSimpleVerseBinding
import me.xizzhu.android.joshua.reading.VerseUpdate
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

class SimpleVerseItem(
        val verse: Verse, private val totalVerseCount: Int, private val followingEmptyVerseCount: Int,
        @ColorInt var highlightColor: Int, var selected: Boolean = false
) : BaseItem(R.layout.item_simple_verse, { inflater, parent -> SimpleVerseItemViewHolder(inflater, parent) }) {
    companion object {
        private val STRING_BUILDER = StringBuilder()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun onVerseClicked(verse: Verse)

        fun onVerseLongClicked(verse: Verse)
    }

    val indexForDisplay: CharSequence by lazy {
        if (verse.parallel.isEmpty()) {
            STRING_BUILDER.setLength(0)

            val verseIndex = verse.verseIndex.verseIndex
            if (followingEmptyVerseCount > 0) {
                STRING_BUILDER.append(verseIndex + 1).append('-').append(verseIndex + followingEmptyVerseCount + 1)
            } else {
                if (totalVerseCount >= 10) {
                    if (totalVerseCount < 100) {
                        if (verseIndex + 1 < 10) {
                            STRING_BUILDER.append(' ')
                        }
                    } else {
                        if (verseIndex + 1 < 10) {
                            STRING_BUILDER.append("  ")
                        } else if (verseIndex + 1 < 100) {
                            STRING_BUILDER.append(' ')
                        }
                    }
                }
                STRING_BUILDER.append(verseIndex + 1)
            }

            return@lazy STRING_BUILDER.toString()
        } else {
            return@lazy ""
        }
    }

    var textForDisplay: CharSequence = ""
        get() {
            if (field.isEmpty()) {
                field = SPANNABLE_STRING_BUILDER.format(verse, followingEmptyVerseCount, true, highlightColor)
            }
            return field
        }
}

private class SimpleVerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SimpleVerseItem>(ItemSimpleVerseBinding.inflate(inflater, parent, false).root) {
    private val resources = itemView.resources
    private val viewBinding = ItemSimpleVerseBinding.bind(itemView).apply {
        root.setOnClickListener { item?.let { callback().onVerseClicked(it.verse) } }
        root.setOnLongClickListener {
            return@setOnLongClickListener item?.let {
                callback().onVerseLongClicked(it.verse)
                true
            } ?: false
        }
    }

    private fun callback(): SimpleVerseItem.Callback = (itemView.activity as? SimpleVerseItem.Callback)
            ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement SimpleVerseItem.Callback")

    override fun bind(settings: Settings, item: SimpleVerseItem, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            val textColor = if (item.selected) settings.getPrimarySelectedTextColor(resources) else settings.getPrimaryTextColor(resources)
            val textSize = settings.getBodyTextSize(resources)

            viewBinding.text.text = item.textForDisplay
            viewBinding.text.setTextColor(textColor)
            viewBinding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

            if (item.verse.parallel.isEmpty()) {
                viewBinding.index.text = item.indexForDisplay
                viewBinding.index.setTextColor(textColor)
                viewBinding.index.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.85F)
                viewBinding.index.visibility = View.VISIBLE

                viewBinding.divider.visibility = View.GONE
            } else {
                viewBinding.index.visibility = View.GONE
                viewBinding.divider.visibility = View.VISIBLE
            }

            itemView.isSelected = item.selected
        } else {
            payloads.forEach { payload ->
                val update = payload as VerseUpdate
                when (update.operation) {
                    VerseUpdate.VERSE_SELECTED -> {
                        item.selected = true
                        itemView.isSelected = true
                        if (settings.nightModeOn) {
                            viewBinding.text.animateTextColor(settings.getPrimarySelectedTextColor(resources))
                        }
                    }
                    VerseUpdate.VERSE_DESELECTED -> {
                        item.selected = false
                        itemView.isSelected = false
                        if (settings.nightModeOn) {
                            viewBinding.text.animateTextColor(settings.getPrimaryTextColor(resources))
                        }
                    }
                    VerseUpdate.HIGHLIGHT_UPDATED -> {
                        item.highlightColor = update.data as Int
                        item.textForDisplay = ""
                        viewBinding.text.text = item.textForDisplay
                    }
                }
            }
        }
    }
}
