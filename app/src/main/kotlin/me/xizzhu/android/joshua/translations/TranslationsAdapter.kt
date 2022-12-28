/*
 * Copyright (C) 2022 Xizhi Zhu
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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.databinding.ItemTitleBinding
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewAdapter
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.VerticalRecyclerViewItem
import me.xizzhu.android.joshua.ui.setPrimaryTextSize
import me.xizzhu.android.joshua.ui.setSecondaryTextSize
import java.util.concurrent.Executor
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.databinding.ItemTranslationBinding

class TranslationAdapter(
    private val inflater: LayoutInflater,
    executor: Executor,
    private val onViewEvent: (ViewEvent) -> Unit,
) : VerticalRecyclerViewAdapter<TranslationItem, TranslationViewHolder<TranslationItem, *>>(TranslationItem.DiffCallback(), executor) {
    sealed class ViewEvent {
        data class DownloadTranslation(val translationToDownload: TranslationInfo) : ViewEvent()
        data class RemoveTranslation(val translationToRemove: TranslationInfo) : ViewEvent()
        data class SelectTranslation(val translationToSelect: TranslationInfo) : ViewEvent()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder<TranslationItem, *> = when (viewType) {
        TranslationItem.Header.VIEW_TYPE -> TranslationViewHolder.Header(inflater, parent)
        TranslationItem.Translation.VIEW_TYPE -> TranslationViewHolder.Translation(inflater, parent, onViewEvent)
        else -> throw IllegalStateException("Unknown view type - $viewType")
    } as TranslationViewHolder<TranslationItem, *>
}

sealed class TranslationItem(viewType: Int) : VerticalRecyclerViewItem(viewType) {
    class DiffCallback : DiffUtil.ItemCallback<TranslationItem>() {
        override fun areItemsTheSame(oldItem: TranslationItem, newItem: TranslationItem): Boolean = when {
            oldItem is Header && newItem is Header -> true
            oldItem is Translation && newItem is Translation -> oldItem.translationInfo.shortName == newItem.translationInfo.shortName
            else -> false
        }

        override fun areContentsTheSame(oldItem: TranslationItem, newItem: TranslationItem): Boolean = oldItem == newItem
    }

    data class Header(
        val settings: Settings,
        val title: String,
        val hideDivider: Boolean,
    ) : TranslationItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_title
        }
    }

    data class Translation(
        val settings: Settings,
        val translationInfo: TranslationInfo,
        val isCurrentTranslation: Boolean,
    ) : TranslationItem(VIEW_TYPE) {
        companion object {
            const val VIEW_TYPE = R.layout.item_translation
        }
    }
}

sealed class TranslationViewHolder<Item : TranslationItem, VB : ViewBinding>(viewBinding: VB) : VerticalRecyclerViewHolder<Item, VB>(viewBinding) {
    class Header(inflater: LayoutInflater, parent: ViewGroup) : TranslationViewHolder<TranslationItem.Header, ItemTitleBinding>(
        ItemTitleBinding.inflate(inflater, parent, false)
    ) {
        init {
            viewBinding.divider.isVisible = true
        }

        override fun bind(item: TranslationItem.Header, payloads: List<Any>) = with(viewBinding) {
            title.setSecondaryTextSize(item.settings)
            title.text = item.title
            divider.isVisible = !item.hideDivider
        }
    }

    class Translation(inflater: LayoutInflater, parent: ViewGroup, onViewEvent: (TranslationAdapter.ViewEvent) -> Unit)
        : TranslationViewHolder<TranslationItem.Translation, ItemTranslationBinding>(ItemTranslationBinding.inflate(inflater, parent, false)
    ) {
        init {
            itemView.setOnClickListener {
                item?.let { item ->
                    if (item.translationInfo.downloaded) {
                        onViewEvent(TranslationAdapter.ViewEvent.SelectTranslation(item.translationInfo))
                    } else {
                        onViewEvent(TranslationAdapter.ViewEvent.DownloadTranslation(item.translationInfo))
                    }
                }
            }
            itemView.setOnLongClickListener {
                item?.let { item ->
                    if (item.translationInfo.downloaded) {
                        if (!item.isCurrentTranslation) {
                            onViewEvent(TranslationAdapter.ViewEvent.RemoveTranslation(item.translationInfo))
                        }
                    } else {
                        onViewEvent(TranslationAdapter.ViewEvent.DownloadTranslation(item.translationInfo))
                    }
                }
                true
            }
        }

        override fun bind(item: TranslationItem.Translation, payloads: List<Any>) = with(viewBinding.root) {
            setPrimaryTextSize(item.settings)
            text = item.translationInfo.name
            setCompoundDrawablesWithIntrinsicBounds(0, 0, if (item.isCurrentTranslation) R.drawable.ic_check else 0, 0)
        }
    }
}
