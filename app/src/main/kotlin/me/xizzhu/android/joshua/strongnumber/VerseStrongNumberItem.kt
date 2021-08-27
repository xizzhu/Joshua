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

package me.xizzhu.android.joshua.strongnumber

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.append
import me.xizzhu.android.joshua.ui.clearAll
import me.xizzhu.android.joshua.ui.createTitleSizeSpan
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.lifecycleScope
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.setSpan
import me.xizzhu.android.joshua.ui.toCharSequence
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText

data class VerseStrongNumberItem(val verseIndex: VerseIndex, private val bookShortName: String,
                                 private val verseText: String, val onClick: (VerseIndex) -> Flow<BaseViewModel.ViewData<Unit>>)
    : BaseItem(R.layout.item_verse_strong_number, { inflater, parent -> VerseStrongNumberItemViewHolder(inflater, parent) }) {
    companion object {
        private val BOOK_NAME_SIZE_SPAN = createTitleSizeSpan()
        private val BOOK_NAME_STYLE_SPAN = createTitleStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    val textForDisplay: CharSequence by lazy {
        // format:
        // <book short name> <chapter verseIndex>:<verse verseIndex> <verse text>
        return@lazy SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName).append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpan(BOOK_NAME_STYLE_SPAN, BOOK_NAME_SIZE_SPAN)
                .append(' ').append(verseText)
                .toCharSequence()
    }
}

private class VerseStrongNumberItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<VerseStrongNumberItem>(inflater.inflate(R.layout.item_verse_strong_number, parent, false)) {
    private val text: TextView = itemView.findViewById(R.id.text)

    init {
        itemView.setOnClickListener { openVerse() }
    }

    private fun openVerse() {
        item?.let { item ->
            item.onClick(item.verseIndex)
                    .onFailure {
                        itemView.activity().dialog(true, R.string.dialog_verse_selection_error,
                                { _, _ -> openVerse() })
                    }
                    .launchIn(itemView.lifecycleScope())
        }
    }

    override fun bind(settings: Settings, item: VerseStrongNumberItem, payloads: List<Any>) {
        with(text) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
