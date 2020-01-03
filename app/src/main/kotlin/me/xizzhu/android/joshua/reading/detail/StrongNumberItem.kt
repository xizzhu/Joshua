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

package me.xizzhu.android.joshua.reading.detail

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.ui.createTitleSizeSpan
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText

data class StrongNumberItem(val strongNumber: StrongNumber)
    : BaseItem(R.layout.item_strong_number, { inflater, parent -> StrongNumberItemViewHolder(inflater, parent) }) {
    companion object {
        private val STRONG_NUMBER_SIZE_SPAN = createTitleSizeSpan()
        private val STRONG_NUMBER_STYLE_SPAN = createTitleStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    val textForDisplay: CharSequence by lazy {
        if (!strongNumber.isValid()) return@lazy ""

        SPANNABLE_STRING_BUILDER.clear()
        SPANNABLE_STRING_BUILDER.clearSpans()

        // format:
        // <strong number>
        // <meaning>
        SPANNABLE_STRING_BUILDER.append(strongNumber.sn)
        SPANNABLE_STRING_BUILDER.setSpan(STRONG_NUMBER_SIZE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        SPANNABLE_STRING_BUILDER.setSpan(STRONG_NUMBER_STYLE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        SPANNABLE_STRING_BUILDER.append('\n').append(strongNumber.meaning)

        return@lazy SPANNABLE_STRING_BUILDER.subSequence(0, SPANNABLE_STRING_BUILDER.length)
    }
}

private class StrongNumberItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<StrongNumberItem>(inflater.inflate(R.layout.item_strong_number, parent, false)) {
    private val strongNumber: TextView = itemView.findViewById(R.id.strong_number)

    override fun bind(settings: Settings, item: StrongNumberItem, payloads: List<Any>) {
        with(strongNumber) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
