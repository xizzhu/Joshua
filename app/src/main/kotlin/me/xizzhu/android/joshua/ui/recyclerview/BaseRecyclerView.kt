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
import androidx.annotation.IntDef
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.utils.BaseSettingsView

interface BaseItem {
    companion object {
        const val TITLE_ITEM = 0
        const val TEXT_ITEM = 1
        const val SEARCH_ITEM = 2
        const val BOOKMARK_ITEM = 3
        const val NOTE_ITEM = 4
        const val TRANSLATION_ITEM = 5
        const val READING_PROGRESS_SUMMARY_ITEM = 6
        const val READING_PROGRESS_DETAIL_ITEM = 7
        const val SIMPLE_VERSE_ITEM = 8

        @IntDef(TITLE_ITEM, TEXT_ITEM, SEARCH_ITEM, BOOKMARK_ITEM, NOTE_ITEM, TRANSLATION_ITEM,
                READING_PROGRESS_SUMMARY_ITEM, READING_PROGRESS_DETAIL_ITEM, SIMPLE_VERSE_ITEM)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ItemViewType
    }

    @ItemViewType
    fun getItemViewType(): Int
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
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].getItemViewType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseItem> =
            when (viewType) {
                BaseItem.TITLE_ITEM -> TitleItemViewHolder(inflater, parent)
                BaseItem.TEXT_ITEM -> TextItemViewHolder(inflater, parent)
                BaseItem.SEARCH_ITEM -> SearchItemViewHolder(inflater, parent)
                BaseItem.BOOKMARK_ITEM -> BookmarkItemViewHolder(inflater, parent)
                BaseItem.NOTE_ITEM -> NoteItemViewHolder(inflater, parent)
                BaseItem.TRANSLATION_ITEM -> TranslationItemViewHolder(inflater, parent)
                BaseItem.READING_PROGRESS_SUMMARY_ITEM -> ReadingProgressSummaryItemViewHolder(inflater, parent)
                BaseItem.READING_PROGRESS_DETAIL_ITEM -> ReadingProgressDetailItemViewHolder(inflater, parent)
                BaseItem.SIMPLE_VERSE_ITEM -> SimpleVerseItemViewHolder(inflater, parent)
                else -> throw IllegalStateException("Unknown view type - $viewType")
            } as BaseViewHolder<BaseItem>

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int) {
        holder.bindData(settings!!, items[position])
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseItem>, position: Int, payloads: MutableList<Any>) {
        holder.bindData(settings!!, items[position], payloads)
    }
}

abstract class BaseRecyclerView : RecyclerView, BaseSettingsView {
    private val adapter: CommonAdapter = CommonAdapter(context).apply { setAdapter(this) }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }

    override fun onSettingsUpdated(settings: Settings) {
        adapter.setSettings(settings)
    }

    protected fun setItems(items: Collection<BaseItem>) {
        adapter.setItems(items)
    }
}
