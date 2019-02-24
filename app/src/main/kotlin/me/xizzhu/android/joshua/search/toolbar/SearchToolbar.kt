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

package me.xizzhu.android.joshua.search.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.utils.MVPView

interface ToolbarView : MVPView

class SearchToolbar : Toolbar, SearchView.OnQueryTextListener, ToolbarView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setLogo(R.drawable.ic_toolbar)

        inflateMenu(R.menu.menu_search)
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.expandActionView()

        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.isQueryRefinementEnabled = true
        searchView.isIconified = false
    }

    private lateinit var presenter: ToolbarPresenter

    fun setPresenter(presenter: ToolbarPresenter) {
        this.presenter = presenter
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        val handled = presenter.search(query)
        if (handled) {
            if (hasFocus()) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
        return handled
    }

    override fun onQueryTextChange(newText: String): Boolean = false
}
