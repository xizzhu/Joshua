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

package me.xizzhu.android.joshua.annotated

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.appbar.MaterialToolbar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants

class AnnotatedVersesToolbar : MaterialToolbar {
    var sortOrderUpdated: ((Int) -> Unit)? = null
    private val sortOrderSpinnerItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (sortOrderUpdated == null) throw IllegalStateException("Sort order update listener not set yet")
            if (position >= Constants.SORT_ORDER_COUNT) throw IllegalArgumentException("Unsupported sort order, position = $position")

            sortOrderSpinnerAdapter.sortOrder = position
            sortOrderUpdated!!.invoke(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }
    }
    private val sortOrderSpinnerAdapter = SortOrderSpinnerAdapter(context)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflateMenu(R.menu.menu_annotated)
        (menu.findItem(R.id.action_sort).actionView as Spinner).apply {
            onItemSelectedListener = sortOrderSpinnerItemSelectedListener
            adapter = sortOrderSpinnerAdapter
        }
    }

    fun setSortOrder(@Constants.SortOrder sortOrder: Int) {
        (menu.findItem(R.id.action_sort).actionView as Spinner).setSelection(sortOrder)
    }
}

private class SortOrderSpinnerAdapter(context: Context) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    private val items: Array<String> = context.resources.getStringArray(R.array.text_sort_order)

    @Constants.SortOrder
    var sortOrder = Constants.DEFAULT_SORT_ORDER

    override fun getCount(): Int = Constants.SORT_ORDER_COUNT

    override fun getItem(position: Int): String = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = (convertView
                ?: inflater.inflate(R.layout.spinner_selected, parent, false)) as TextView
        textView.text = getItem(position)
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (position >= Constants.SORT_ORDER_COUNT) {
            throw IllegalArgumentException("Unsupported sort order, position = $position")
        }

        val viewHolder = convertView?.let { it.tag as DropDownViewHolder }
                ?: DropDownViewHolder(inflater.inflate(R.layout.spinner_drop_down, parent, false))
                        .apply {
                            rootView.tag = this
                            checkBox.isEnabled = false
                        }
        viewHolder.title.text = getItem(position)
        viewHolder.checkBox.isChecked = position == sortOrder
        return viewHolder.rootView
    }
}

private class DropDownViewHolder(val rootView: View) {
    val title: TextView = rootView.findViewById(R.id.title)
    val checkBox: CheckBox = rootView.findViewById(R.id.checkbox)
}
