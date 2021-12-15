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

package me.xizzhu.android.joshua.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.databinding.ItemTextBinding
import me.xizzhu.android.joshua.ui.setPrimaryTextSize

data class TextItem(val title: CharSequence)
    : BaseItem(R.layout.item_text, { inflater, parent -> TextItemViewHolder(inflater, parent) })

private class TextItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TextItem, ItemTextBinding>(ItemTextBinding.inflate(inflater, parent, false)) {
    override fun bind(settings: Settings, item: TextItem, payloads: List<Any>) {
        with(viewBinding.title) {
            setPrimaryTextSize(settings)
            text = item.title
        }
    }
}
