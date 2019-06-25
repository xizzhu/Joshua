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

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.ui.getCaptionTextSize

data class TitleItem(val title: CharSequence, val hideDivider: Boolean) : BaseItem {
    companion object {
        private const val itemViewType = R.layout.item_title

        init {
            BaseItem.viewHolderCreator[itemViewType] = { inflater, parent -> TitleItemViewHolder(inflater, parent) }
        }
    }

    override fun getItemViewType(): Int = itemViewType
}

private class TitleItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TitleItem>(inflater.inflate(R.layout.item_title, parent, false)) {
    private val title: TextView = itemView.findViewById(R.id.title)
    private val divider: View = itemView.findViewById(R.id.divider)

    override fun bind(settings: Settings, item: TitleItem, payloads: List<Any>) {
        with(title) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getCaptionTextSize(resources))
            text = item.title
        }
        divider.visibility = if (item.hideDivider) View.GONE else View.VISIBLE
    }
}
