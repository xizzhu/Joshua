/*
 * Copyright (C) 2023 Xizhi Zhu
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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.databinding.ItemTitleBinding
import me.xizzhu.android.joshua.ui.setSecondaryTextSize

data class TitleItem(val title: CharSequence, val hideDivider: Boolean)
    : BaseItem(R.layout.item_title, { inflater, parent -> TitleItemViewHolder(inflater, parent) })

private class TitleItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TitleItem, ItemTitleBinding>(ItemTitleBinding.inflate(inflater, parent, false)) {
    override fun bind(settings: Settings, item: TitleItem, payloads: List<Any>) {
        with(viewBinding.title) {
            setSecondaryTextSize(settings)
            text = item.title
        }
        viewBinding.divider.visibility = if (item.hideDivider) View.GONE else View.VISIBLE
    }
}
