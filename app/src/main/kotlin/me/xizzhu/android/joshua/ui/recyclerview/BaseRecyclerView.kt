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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.core.Settings

abstract class BaseItem protected constructor(val viewType: Int,
                                              viewHolderCreator: (LayoutInflater, ViewGroup) -> BaseViewHolder<out BaseItem>) {
    companion object {
        private val VIEW_HOLDER_CREATORS = mutableMapOf<Int, (LayoutInflater, ViewGroup) -> BaseViewHolder<out BaseItem>>()

        fun getViewHolderCreator(viewType: Int) = VIEW_HOLDER_CREATORS
                .getOrElse(viewType, { throw IllegalStateException("Unknown view type - $viewType") })
    }

    init {
        VIEW_HOLDER_CREATORS[viewType] = viewHolderCreator
    }
}

abstract class BaseViewHolder<T : BaseItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var item: T? = null
        private set

    fun bindData(settings: Settings, item: T, payloads: List<Any> = emptyList()) {
        this.item = item
        bind(settings, item, payloads)
    }

    protected abstract fun bind(settings: Settings, item: T, payloads: List<Any>)
}

private class CommonAdapter(context: Context) : RecyclerView.Adapter<BaseViewHolder<BaseItem>>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
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

        if (items.isNotEmpty()) {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = if (settings != null) items.size else 0

    override fun getItemViewType(position: Int): Int = items[position].viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseItem> =
            BaseItem.getViewHolderCreator(viewType).invoke(inflater, parent) as BaseViewHolder<BaseItem>

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int) {
        holder.bindData(settings!!, items[position])
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int, payloads: MutableList<Any>) {
        holder.bindData(settings!!, items[position], payloads)
    }
}

abstract class BaseRecyclerView : RecyclerView {
    private val adapter: CommonAdapter = CommonAdapter(context).apply { setAdapter(this) }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }

    fun onSettingsUpdated(settings: Settings) {
        adapter.setSettings(settings)
    }

    protected fun setItems(items: Collection<BaseItem>) {
        adapter.setItems(items)
    }
}
