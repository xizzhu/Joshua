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

import android.app.SearchableInfo
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import me.xizzhu.android.joshua.R

class SearchToolbar : Toolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setLogo(R.drawable.ic_toolbar)

        inflateMenu(R.menu.menu_search)
        menu.findItem(R.id.action_search).expandActionView()
        with(searchView()) {
            isQueryRefinementEnabled = true
            isIconified = false
        }
    }

    private fun searchView(): SearchView = menu.findItem(R.id.action_search).actionView as SearchView

    fun setOnQueryTextListener(listener: SearchView.OnQueryTextListener?) {
        searchView().setOnQueryTextListener(listener)
    }

    fun setSearchableInfo(searchable: SearchableInfo) {
        searchView().setSearchableInfo(searchable)
    }
}
