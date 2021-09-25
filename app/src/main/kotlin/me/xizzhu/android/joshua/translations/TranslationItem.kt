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

package me.xizzhu.android.joshua.translations

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.ui.activity
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText

class TranslationItem(val translationInfo: TranslationInfo, val isCurrentTranslation: Boolean,
                      val rightDrawable: Int = if (isCurrentTranslation) R.drawable.ic_check else 0)
    : BaseItem(R.layout.item_translation, { inflater, parent -> TranslationItemViewHolder(inflater, parent) }) {
    interface Callback {
        fun selectTranslation(translationToSelect: TranslationInfo)

        fun downloadTranslation(translationToDownload: TranslationInfo)

        fun removeTranslation(translationToRemove: TranslationInfo)
    }
}

private class TranslationItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TranslationItem>(inflater.inflate(R.layout.item_translation, parent, false)) {
    private val callback: TranslationItem.Callback
        get() = (itemView.activity as? TranslationItem.Callback)
                ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement TranslationItem.Callback")
    private val textView = itemView as TextView

    init {
        itemView.setOnClickListener {
            item?.let { item ->
                if (item.translationInfo.downloaded) {
                    callback.selectTranslation(item.translationInfo)
                } else {
                    callback.downloadTranslation(item.translationInfo)
                }
            }
        }
        itemView.setOnLongClickListener {
            item?.let { item ->
                if (item.translationInfo.downloaded) {
                    if (!item.isCurrentTranslation) {
                        callback.removeTranslation(item.translationInfo)
                    }
                } else {
                    callback.downloadTranslation(item.translationInfo)
                }
            }
            return@setOnLongClickListener true
        }
    }

    override fun bind(settings: Settings, item: TranslationItem, payloads: List<Any>) {
        with(textView) {
            updateSettingsWithPrimaryText(settings)
            text = item.translationInfo.name
            setCompoundDrawablesWithIntrinsicBounds(0, 0, item.rightDrawable, 0)
        }
    }
}
