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
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor

data class TranslationItem(val translationInfo: TranslationInfo, val isCurrentTranslation: Boolean) : BaseItem {
    override fun getItemViewType(): Int = BaseItem.TRANSLATION_ITEM
}

fun List<TranslationInfo>.toTranslationItems(currentTranslation: String): List<TranslationItem> {
    return ArrayList<TranslationItem>(size).apply {
        for (translationInfo in this@toTranslationItems) {
            add(TranslationItem(translationInfo, translationInfo.downloaded && translationInfo.shortName == currentTranslation))
        }
    }
}

class TranslationItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TranslationItem>(inflater.inflate(R.layout.item_translation, parent, false)) {
    private val textView = itemView as TextView

    override fun bind(settings: Settings, item: TranslationItem, payloads: List<Any>) {
        with(textView) {
            setTextColor(settings.getPrimaryTextColor(resources))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))

            text = item.translationInfo.name
            setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    if (item.isCurrentTranslation) R.drawable.ic_check else 0, 0)
        }
    }
}
