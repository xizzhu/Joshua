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

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.ui.getCaptionTextSize
import me.xizzhu.android.joshua.ui.getSecondaryTextColor

data class TitleItem(val title: CharSequence) : BaseItem {
    override fun getItemViewType(): Int = BaseItem.TITLE_ITEM
}

class TitleItemViewHolder(inflater: LayoutInflater, parent: ViewGroup, private val resources: Resources)
    : BaseViewHolder<TitleItem>(inflater.inflate(R.layout.item_title, parent, false)) {
    private val title: TextView = itemView.findViewById(R.id.title)

    override fun bind(settings: Settings, item: TitleItem, payloads: List<Any>) {
        with(title) {
            setTextColor(settings.getSecondaryTextColor(this@TitleItemViewHolder.resources))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getCaptionTextSize(this@TitleItemViewHolder.resources))
            text = item.title
        }
    }
}
