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

package me.xizzhu.android.joshua.reading.verse

import android.text.SpannableStringBuilder
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
    : BaseViewHolder<SimpleVerseItem, ItemSimpleVerseBinding>(ItemSimpleVerseBinding.inflate(inflater, parent, false)) {
    init {
        viewBinding.root.setOnClickListener { item?.let { callback().onVerseClicked(it.verse) } }
        viewBinding.root.setOnLongClickListener {
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
            viewBinding.text.text = item.textForDisplay
            viewBinding.text.setPrimaryTextSize(settings)

            if (item.verse.parallel.isEmpty()) {
                viewBinding.index.text = item.indexForDisplay
                viewBinding.index.setPrimaryTextSize(settings)
                viewBinding.index.visibility = View.VISIBLE

                viewBinding.divider.visibility = View.GONE
            } else {
                viewBinding.index.visibility = View.GONE
                viewBinding.divider.visibility = View.VISIBLE
            }

            viewBinding.root.isSelected = item.selected
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
