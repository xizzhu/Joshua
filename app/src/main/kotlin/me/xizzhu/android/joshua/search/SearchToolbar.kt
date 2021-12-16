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

package me.xizzhu.android.joshua.search

import android.app.SearchableInfo
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SearchView
import com.google.android.material.appbar.MaterialToolbar
import me.xizzhu.android.joshua.R

class SearchToolbar : MaterialToolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflateMenu(R.menu.menu_search)
        menu.findItem(R.id.action_search).expandActionView()
        with(searchView()) {
            isQueryRefinementEnabled = true
            isIconified = false
        }
    }

    private fun searchView(): SearchView = menu.findItem(R.id.action_search).actionView as SearchView

    fun initialize(
            includeOldTestament: Boolean, onIncludeOldTestamentChanged: (Boolean) -> Unit,
            includeNewTestament: Boolean, onIncludeNewTestamentChanged: (Boolean) -> Unit,
            includeBookmarks: Boolean, onIncludeBookmarksChanged: (Boolean) -> Unit,
            includeHighlights: Boolean, onIncludeHighlightsChanged: (Boolean) -> Unit,
            includeNotes: Boolean, onIncludeNotesChanged: (Boolean) -> Unit,
            onQueryTextListener: SearchView.OnQueryTextListener, clearHistory: () -> Unit
    ) {
        menu.findItem(R.id.action_search_include_old_testament).isChecked = includeOldTestament
        menu.findItem(R.id.action_search_include_new_testament).isChecked = includeNewTestament
        menu.findItem(R.id.action_search_include_bookmarks).isChecked = includeBookmarks
        menu.findItem(R.id.action_search_include_highlights).isChecked = includeHighlights
        menu.findItem(R.id.action_search_include_notes).isChecked = includeNotes

        searchView().setOnQueryTextListener(onQueryTextListener)

        setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search_include_old_testament -> {
                    menuItem.isChecked = !menuItem.isChecked
                    onIncludeOldTestamentChanged(menuItem.isChecked)
                    true
                }
                R.id.action_search_include_new_testament -> {
                    menuItem.isChecked = !menuItem.isChecked
                    onIncludeNewTestamentChanged(menuItem.isChecked)
                    true
                }
                R.id.action_search_include_bookmarks -> {
                    menuItem.isChecked = !menuItem.isChecked
                    onIncludeBookmarksChanged(menuItem.isChecked)
                    true
                }
                R.id.action_search_include_highlights -> {
                    menuItem.isChecked = !menuItem.isChecked
                    onIncludeHighlightsChanged(menuItem.isChecked)
                    true
                }
                R.id.action_search_include_notes -> {
                    menuItem.isChecked = !menuItem.isChecked
                    onIncludeNotesChanged(menuItem.isChecked)
                    true
                }
                R.id.action_clear_search_history -> {
                    clearHistory()
                    true
                }
                else -> false
            }
        }
    }

    fun setSearchableInfo(searchable: SearchableInfo) {
        searchView().setSearchableInfo(searchable)
    }
}
