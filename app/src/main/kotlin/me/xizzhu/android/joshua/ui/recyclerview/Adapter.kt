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

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.core.Settings

interface BaseItem {
    companion object {
        const val TITLE_ITEM = 0
        const val SEARCH_ITEM = 1
        const val BOOKMARK_ITEM = 2
        const val NOTE_ITEM = 3
        const val TRANSLATION_ITEM = 4

        @IntDef(TITLE_ITEM, SEARCH_ITEM, BOOKMARK_ITEM, NOTE_ITEM, TRANSLATION_ITEM)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ItemViewType
    }

    @ItemViewType
    fun getItemViewType(): Int
}

abstract class BaseViewHolder<T : BaseItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var item: T? = null
        protected set

    abstract fun bind(settings: Settings, item: T)

    open fun bind(settings: Settings, item: T, payloads: MutableList<Any>) {
        bind(settings, item)
    }
}

class CommonAdapter(context: Context) : RecyclerView.Adapter<BaseViewHolder<BaseItem>>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val resources: Resources = context.resources
    private val items: ArrayList<BaseItem> = ArrayList()
    private var settings: Settings? = null

    fun setItems(items: Collection<BaseItem>) {
        this.items.clear()
        this.items.addAll(items)

        if (settings != null) {
            notifyDataSetChanged()
        }
    }

    fun setSettings(settings: Settings) {
        this.settings = settings
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].getItemViewType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseItem> =
            when (viewType) {
                BaseItem.TITLE_ITEM -> TitleItemViewHolder(inflater, parent, resources)
                BaseItem.SEARCH_ITEM -> SearchItemViewHolder(inflater, parent, resources)
                BaseItem.BOOKMARK_ITEM -> BookmarkItemViewHolder(inflater, parent, resources)
                BaseItem.NOTE_ITEM -> NoteItemViewHolder(inflater, parent, resources)
                BaseItem.TRANSLATION_ITEM -> TranslationItemViewHolder(inflater, parent, resources)
                else -> throw IllegalStateException("Unknown view type - $viewType")
            } as BaseViewHolder<BaseItem>

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int) {
        holder.bind(settings!!, items[position])
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int, payloads: MutableList<Any>) {
        holder.bind(settings!!, items[position], payloads)
    }
}
