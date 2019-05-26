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
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.utils.MVPView

interface ToolbarView : MVPView {
    fun onBookmarkSortOrderLoaded(@Constants.SortOrder sortOrder: Int)
}

class BookmarksToolbar : Toolbar, ToolbarView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var presenter: ToolbarPresenter

    private val bookmarksSpinnerItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position >= Constants.SORT_ORDER_COUNT) {
                throw IllegalArgumentException("Unsupported sort order, position = $position")
            }

            translationSpinnerAdapter.sortOrder = position
            presenter.updateBookmarksSortOrder(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }
    }
    private val translationSpinnerAdapter = BookmarksSpinnerAdapter(context)

    init {
        setTitle(R.string.title_bookmarks)

        inflateMenu(R.menu.menu_bookmarks)
        (menu.findItem(R.id.action_sort).actionView as Spinner).apply {
            onItemSelectedListener = bookmarksSpinnerItemSelectedListener
        }
    }

    fun setPresenter(presenter: ToolbarPresenter) {
        this.presenter = presenter
    }


    override fun onBookmarkSortOrderLoaded(@Constants.SortOrder sortOrder: Int) {
        (menu.findItem(R.id.action_sort).actionView as Spinner).apply {
            adapter = translationSpinnerAdapter
            setSelection(sortOrder)
        }
    }
}
