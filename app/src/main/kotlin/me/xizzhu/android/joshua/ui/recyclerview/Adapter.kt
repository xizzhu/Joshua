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

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.core.Settings

interface BaseItem {
    @IntDef()
    @Retention(AnnotationRetention.SOURCE)
    annotation class ItemViewType

    @ItemViewType
    fun getItemViewType(): Int
}

abstract class BaseViewHolder<T : BaseItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(settings: Settings, item: T)

    open fun bind(settings: Settings, item: T, payloads: MutableList<Any>) {
        bind(settings, item)
    }
}

class Adapter : RecyclerView.Adapter<BaseViewHolder<BaseItem>>() {
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
            return when (viewType) {
                else -> throw IllegalStateException("Unknown view type - $viewType")
            }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int) {
        holder.bind(settings!!, items[position])
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int, payloads: MutableList<Any>) {
        holder.bind(settings!!, items[position], payloads)
    }
}
