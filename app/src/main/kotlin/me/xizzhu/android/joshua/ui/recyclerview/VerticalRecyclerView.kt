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

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.util.concurrent.Executor

abstract class VerticalRecyclerViewItem(val viewType: Int)

abstract class VerticalRecyclerViewHolder<Item : VerticalRecyclerViewItem, VB : ViewBinding>(protected val viewBinding: VB)
    : RecyclerView.ViewHolder(viewBinding.root) {
    protected var item: Item? = null

    fun bindData(item: Item, payloads: List<Any> = emptyList()) {
        this.item = item
        bind(item, payloads)
    }

    protected abstract fun bind(item: Item, payloads: List<Any>)
}

abstract class VerticalRecyclerViewAdapter<Item : VerticalRecyclerViewItem, ViewHolder : VerticalRecyclerViewHolder<Item, *>>(
    diffCallback: DiffUtil.ItemCallback<Item>,
    executor: Executor
) : ListAdapter<Item, ViewHolder>(
    AsyncDifferConfig.Builder(diffCallback).setBackgroundThreadExecutor(executor).build()
) {
    override fun getItemViewType(position: Int): Int = getItem(position).viewType

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bindData(getItem(position), payloads)
    }
}

class VerticalRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }
}
