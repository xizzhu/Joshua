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

package me.xizzhu.android.joshua.reading.detail

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.databinding.ItemStrongNumberBinding
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

class StrongNumberItem(val strongNumber: StrongNumber)
    : BaseItem(R.layout.item_strong_number, { inflater, parent -> StrongNumberItemViewHolder(inflater, parent) }) {
    companion object {
        private val STRONG_NUMBER_SIZE_SPAN = createTitleSizeSpan()
        private val STRONG_NUMBER_STYLE_SPAN = createTitleStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun openStrongNumber(strongNumber: String)
    }

    val textForDisplay: CharSequence by lazy {
        if (!strongNumber.isValid()) return@lazy ""

        // format:
        // <strong number>
        // <meaning>
        return@lazy SPANNABLE_STRING_BUILDER.clearAll()
                .append(strongNumber.sn)
                .setSpan(STRONG_NUMBER_SIZE_SPAN, STRONG_NUMBER_STYLE_SPAN)
                .append('\n').append(strongNumber.meaning)
                .toCharSequence()
    }
}

private class StrongNumberItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<StrongNumberItem>(ItemStrongNumberBinding.inflate(inflater, parent, false).root) {
    private val viewBinding: ItemStrongNumberBinding = ItemStrongNumberBinding.bind(itemView)

    init {
        itemView.setOnClickListener {
            item?.let { item ->
                (itemView.activity as? StrongNumberItem.Callback)?.openStrongNumber(item.strongNumber.sn)
                        ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement StrongNumberItem.Callback")
            }
        }
    }

    override fun bind(settings: Settings, item: StrongNumberItem, payloads: List<Any>) {
        with(viewBinding.strongNumber) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
