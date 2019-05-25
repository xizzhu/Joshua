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

package me.xizzhu.android.joshua.bookmarks.toolbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import me.xizzhu.android.joshua.R

class BookmarksSpinnerAdapter(context: Context) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    private val items: Array<String>

    var selected = ToolbarPresenter.SORT_BY_DATE

    init {
        val resources = context.resources
        items = arrayOf(resources.getString(R.string.bookmark_spinner_sort_by_date),
                resources.getString(R.string.bookmark_spinner_sort_by_book))
    }

    override fun getCount(): Int = ToolbarPresenter.SORT_COUNT

    override fun getItem(position: Int): String = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = (convertView
                ?: inflater.inflate(R.layout.spinner_selected, parent, false)) as TextView
        textView.text = getItem(position)
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder = convertView?.let { it.tag as DropDownViewHolder }
                ?: DropDownViewHolder(inflater.inflate(R.layout.spinner_drop_down, parent, false))
                        .apply {
                            rootView.tag = this
                            checkBox.isEnabled = false
                        }
        viewHolder.title.text = getItem(position)
        viewHolder.checkBox.isChecked = position == selected
        return viewHolder.rootView
    }
}

private class DropDownViewHolder(val rootView: View) {
    val title: TextView = rootView.findViewById(R.id.title)
    val checkBox: CheckBox = rootView.findViewById(R.id.checkbox)
}