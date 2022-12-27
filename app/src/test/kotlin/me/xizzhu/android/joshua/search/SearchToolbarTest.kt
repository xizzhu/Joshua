/*
 * Copyright (C) 2022 Xizhi Zhu
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

import android.content.Context
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.getProperty
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchToolbarTest : BaseUnitTest() {
    private lateinit var searchToolbar: SearchToolbar

    @BeforeTest
    override fun setup() {
        super.setup()

        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(R.style.AppTheme)
        searchToolbar = SearchToolbar(context)
    }

    @Test
    fun `test initialize()`() {
        var onIncludeOldTestamentChangedCalled = 0
        var onIncludeNewTestamentChangedCalled = 0
        var onIncludeBookmarksChangedCalled = 0
        var onIncludeHighlightsChangedCalled = 0
        var onIncludeNotesChangedCalled = 0
        var clearHistoryCalled = 0

        val queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        }
        searchToolbar.initialize(
            onIncludeOldTestamentChanged = {
                assertTrue(it)
                onIncludeOldTestamentChangedCalled++
            },
            onIncludeNewTestamentChanged = {
                assertTrue(it)
                onIncludeNewTestamentChangedCalled++
            },
            onIncludeBookmarksChanged = {
                assertTrue(it)
                onIncludeBookmarksChangedCalled++
            },
            onIncludeHighlightsChanged = {
                assertTrue(it)
                onIncludeHighlightsChangedCalled++
            },
            onIncludeNotesChanged = {
                assertTrue(it)
                onIncludeNotesChangedCalled++
            },
            onQueryTextListener = queryTextListener,
            clearHistory = { clearHistoryCalled++ },
        )

        assertEquals(
            queryTextListener,
            (searchToolbar.menu.findItem(R.id.action_search).actionView as SearchView).getProperty<SearchView, SearchView.OnQueryTextListener>("mOnQueryChangeListener")
        )

        val onMenuItemClickListener: Toolbar.OnMenuItemClickListener = (searchToolbar as Toolbar).getProperty("mOnMenuItemClickListener")
        assertTrue(onMenuItemClickListener.onMenuItemClick(searchToolbar.menu.findItem(R.id.action_search_include_old_testament)))
        assertEquals(1, onIncludeOldTestamentChangedCalled)
        assertEquals(0, onIncludeNewTestamentChangedCalled)
        assertEquals(0, onIncludeBookmarksChangedCalled)
        assertEquals(0, onIncludeHighlightsChangedCalled)
        assertEquals(0, onIncludeNotesChangedCalled)
        assertEquals(0, clearHistoryCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(searchToolbar.menu.findItem(R.id.action_search_include_new_testament)))
        assertEquals(1, onIncludeOldTestamentChangedCalled)
        assertEquals(1, onIncludeNewTestamentChangedCalled)
        assertEquals(0, onIncludeBookmarksChangedCalled)
        assertEquals(0, onIncludeHighlightsChangedCalled)
        assertEquals(0, onIncludeNotesChangedCalled)
        assertEquals(0, clearHistoryCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(searchToolbar.menu.findItem(R.id.action_search_include_bookmarks)))
        assertEquals(1, onIncludeOldTestamentChangedCalled)
        assertEquals(1, onIncludeNewTestamentChangedCalled)
        assertEquals(1, onIncludeBookmarksChangedCalled)
        assertEquals(0, onIncludeHighlightsChangedCalled)
        assertEquals(0, onIncludeNotesChangedCalled)
        assertEquals(0, clearHistoryCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(searchToolbar.menu.findItem(R.id.action_search_include_highlights)))
        assertEquals(1, onIncludeOldTestamentChangedCalled)
        assertEquals(1, onIncludeNewTestamentChangedCalled)
        assertEquals(1, onIncludeBookmarksChangedCalled)
        assertEquals(1, onIncludeHighlightsChangedCalled)
        assertEquals(0, onIncludeNotesChangedCalled)
        assertEquals(0, clearHistoryCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(searchToolbar.menu.findItem(R.id.action_search_include_notes)))
        assertEquals(1, onIncludeOldTestamentChangedCalled)
        assertEquals(1, onIncludeNewTestamentChangedCalled)
        assertEquals(1, onIncludeBookmarksChangedCalled)
        assertEquals(1, onIncludeHighlightsChangedCalled)
        assertEquals(1, onIncludeNotesChangedCalled)
        assertEquals(0, clearHistoryCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(searchToolbar.menu.findItem(R.id.action_clear_search_history)))
        assertEquals(1, onIncludeOldTestamentChangedCalled)
        assertEquals(1, onIncludeNewTestamentChangedCalled)
        assertEquals(1, onIncludeBookmarksChangedCalled)
        assertEquals(1, onIncludeHighlightsChangedCalled)
        assertEquals(1, onIncludeNotesChangedCalled)
        assertEquals(1, clearHistoryCalled)
    }

    @Test
    fun `test setSearchConfiguration()`() {
        searchToolbar.setSearchConfiguration(
            includeOldTestament = true,
            includeNewTestament = true,
            includeBookmarks = true,
            includeHighlights = true,
            includeNotes = true,
        )
        assertTrue(searchToolbar.menu.findItem(R.id.action_search_include_old_testament).isChecked)
        assertTrue(searchToolbar.menu.findItem(R.id.action_search_include_new_testament).isChecked)
        assertTrue(searchToolbar.menu.findItem(R.id.action_search_include_bookmarks).isChecked)
        assertTrue(searchToolbar.menu.findItem(R.id.action_search_include_highlights).isChecked)
        assertTrue(searchToolbar.menu.findItem(R.id.action_search_include_notes).isChecked)

        searchToolbar.setSearchConfiguration(
            includeOldTestament = false,
            includeNewTestament = false,
            includeBookmarks = false,
            includeHighlights = false,
            includeNotes = false,
        )
        assertFalse(searchToolbar.menu.findItem(R.id.action_search_include_old_testament).isChecked)
        assertFalse(searchToolbar.menu.findItem(R.id.action_search_include_new_testament).isChecked)
        assertFalse(searchToolbar.menu.findItem(R.id.action_search_include_bookmarks).isChecked)
        assertFalse(searchToolbar.menu.findItem(R.id.action_search_include_highlights).isChecked)
        assertFalse(searchToolbar.menu.findItem(R.id.action_search_include_notes).isChecked)
    }
}
