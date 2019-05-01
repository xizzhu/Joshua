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

package me.xizzhu.android.joshua.search.result

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.CommonAdapter
import me.xizzhu.android.joshua.ui.recyclerview.SearchItem
import me.xizzhu.android.joshua.ui.recyclerview.SearchItemViewHolder
import me.xizzhu.android.joshua.utils.BaseSettingsView

interface SearchResultView : BaseSettingsView {
    fun onSearchStarted()

    fun onSearchCompleted()

    fun onSearchResultUpdated(searchItems: List<SearchItem>)

    fun onVerseSelectionFailed(verseToSelect: VerseIndex)
}

class SearchResultListView : RecyclerView, SearchResultView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var presenter: SearchResultPresenter

    private val onClickListener = OnClickListener { view ->
        ((getChildViewHolder(view) as SearchItemViewHolder).item)?.let {
            presenter.selectVerse(it.verseIndex)
        }
    }
    private val adapter: CommonAdapter = CommonAdapter(context)
    private var hasSearchStarted = false

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(adapter)
    }

    fun setPresenter(presenter: SearchResultPresenter) {
        this.presenter = presenter
    }

    override fun onChildAttachedToWindow(child: View) {
        super.onChildAttachedToWindow(child)
        child.setOnClickListener(onClickListener)
    }

    override fun onChildDetachedFromWindow(child: View) {
        super.onChildDetachedFromWindow(child)
        child.setOnClickListener(null)
    }

    override fun onSettingsUpdated(settings: Settings) {
        adapter.setSettings(settings)
    }

    override fun onSearchStarted() {
        visibility = GONE
        hasSearchStarted = true
    }

    override fun onSearchCompleted() {
        fadeIn()
    }

    override fun onSearchResultUpdated(searchItems: List<SearchItem>) {
        if (hasSearchStarted) {
            hasSearchStarted = false
            Toast.makeText(context, resources.getString(R.string.toast_verses_searched, searchItems.size),
                    Toast.LENGTH_SHORT).show()
        }
        adapter.setItems(searchItems)
        scrollToPosition(0)
    }

    override fun onVerseSelectionFailed(verseToSelect: VerseIndex) {
        DialogHelper.showDialog(context, true, R.string.dialog_verse_selection_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.selectVerse(verseToSelect)
                })
    }
}
