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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

data class StrongNumberItem(val verseIndex: VerseIndex, val strongNumber: StrongNumber)
    : BaseItem(R.layout.item_strong_number, { inflater, parent -> StrongNumberItemViewHolder(inflater, parent) }) {
    val textForDisplay: CharSequence by lazy {
        // TODO
        return@lazy ""
    }
}

private class StrongNumberItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<StrongNumberItem>(inflater.inflate(R.layout.item_strong_number, parent, false)) {
    private val strongNumber: TextView = itemView.findViewById(R.id.strong_number)

    override fun bind(settings: Settings, item: StrongNumberItem, payloads: List<Any>) {
        // TODO
    }
}
