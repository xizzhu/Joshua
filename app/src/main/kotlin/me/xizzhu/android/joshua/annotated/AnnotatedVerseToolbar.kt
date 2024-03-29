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

package me.xizzhu.android.joshua.annotated

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Spinner
import com.google.android.material.appbar.MaterialToolbar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.databinding.SpinnerDropDownBinding
import me.xizzhu.android.joshua.databinding.SpinnerSelectedBinding

class AnnotatedVerseToolbar : MaterialToolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflateMenu(R.menu.menu_annotated)
    }

    fun initialize(sortOrderUpdated: (Int) -> Unit) {
        (menu.findItem(R.id.action_sort).actionView as Spinner).apply {
            val sortOrderSpinnerAdapter = SortOrderSpinnerAdapter(context)
            adapter = sortOrderSpinnerAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    sortOrderSpinnerAdapter.sortOrder = position
                    sortOrderUpdated.invoke(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }
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
        val viewBinding = convertView?.let { SpinnerSelectedBinding.bind(it) }
            ?: SpinnerSelectedBinding.inflate(inflater, parent, false)
        viewBinding.root.text = getItem(position)
        return viewBinding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewBinding = convertView?.let { it.tag as SpinnerDropDownBinding }
            ?: SpinnerDropDownBinding.inflate(inflater, parent, false)
                .apply {
                    root.tag = this
                    checkbox.isEnabled = false
                }
        viewBinding.title.text = getItem(position)
        viewBinding.checkbox.isChecked = position == sortOrder
        return viewBinding.root
    }
}
