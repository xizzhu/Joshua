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

package me.xizzhu.android.joshua.translations

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText
import java.util.*
import kotlin.collections.ArrayList

data class TranslationItem(val translationInfo: TranslationInfo, val isCurrentTranslation: Boolean,
                           val onClicked: (TranslationInfo) -> Unit,
                           val onLongClicked: (TranslationInfo, Boolean) -> Unit,
                           val rightDrawable: Int = if (isCurrentTranslation) R.drawable.ic_check else 0)
    : BaseItem(R.layout.item_translation, { inflater, parent -> TranslationItemViewHolder(inflater, parent) })

fun List<TranslationInfo>.toTranslationItems(currentTranslation: String, groupByLanguage: Boolean,
                                             onClicked: (TranslationInfo) -> Unit,
                                             onLongClicked: (TranslationInfo, Boolean) -> Unit): List<BaseItem> {
    return ArrayList<BaseItem>(size).apply {
        var currentLanguage = ""
        for (translationInfo in this@toTranslationItems) {
            if (groupByLanguage) {
                val language = translationInfo.language.split("_")[0]
                if (currentLanguage != language) {
                    if (currentLanguage.isNotEmpty()) {
                        add(TitleItem(Locale(language).displayLanguage, true))
                    }
                    currentLanguage = language
                }
            }
            add(TranslationItem(translationInfo,
                    translationInfo.downloaded && translationInfo.shortName == currentTranslation,
                    onClicked, onLongClicked))
        }
    }
}

private class TranslationItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TranslationItem>(inflater.inflate(R.layout.item_translation, parent, false)) {
    private val textView = itemView as TextView

    init {
        itemView.setOnClickListener { item?.let { it.onClicked(it.translationInfo) } }
        itemView.setOnLongClickListener {
            item?.let { it.onLongClicked(it.translationInfo, it.isCurrentTranslation) }
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
