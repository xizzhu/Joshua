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
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor
import me.xizzhu.android.joshua.utils.BaseSettingsView

interface SearchResultView : BaseSettingsView {
    fun onSearchStarted()

    fun onSearchCompleted()

    fun onSearchResultUpdated(searchResult: SearchResult)

    fun onVerseSelectionFailed(verseToSelect: VerseIndex)
}

class SearchResultListView : RecyclerView, SearchResultView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var presenter: SearchResultPresenter

    private val listener = object : OnSearchResultClickedListener {
        override fun onSearchResultClicked(verseIndex: VerseIndex) {
            presenter.selectVerse(verseIndex)
        }
    }
    private val adapter: SearchResultListAdapter = SearchResultListAdapter(context, listener)
    private var hasSearchStarted = false

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(adapter)
    }

    fun setPresenter(presenter: SearchResultPresenter) {
        this.presenter = presenter
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

    override fun onSearchResultUpdated(searchResult: SearchResult) {
        if (hasSearchStarted) {
            hasSearchStarted = false
            Toast.makeText(context, resources.getString(R.string.toast_verses_searched, searchResult.size),
                    Toast.LENGTH_SHORT).show()
        }
        adapter.setSearchResult(searchResult)
        scrollToPosition(0)
    }

    override fun onVerseSelectionFailed(verseToSelect: VerseIndex) {
        DialogHelper.showDialog(context, true, R.string.dialog_verse_selection_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.selectVerse(verseToSelect)
                })
    }
}

private interface OnSearchResultClickedListener {
    fun onSearchResultClicked(verseIndex: VerseIndex)
}

private class SearchResultListAdapter(context: Context, private val listener: OnSearchResultClickedListener)
    : RecyclerView.Adapter<SearchResultViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val resources: Resources = context.resources

    private var searchResult: SearchResult? = null
    private var settings: Settings? = null

    fun setSearchResult(searchResult: SearchResult) {
        this.searchResult = searchResult
        notifyDataSetChanged()
    }

    fun setSettings(settings: Settings) {
        this.settings = settings
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (settings != null && searchResult != null) searchResult!!.size else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder =
            SearchResultViewHolder(inflater, parent, resources, listener)

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(searchResult!![position], settings!!)
    }
}

private class SearchResultViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                     private val resources: Resources,
                                     private val listener: OnSearchResultClickedListener)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_search_result, parent, false)),
        View.OnClickListener {
    private val text = itemView as TextView

    private var currentVerse: SearchedVerse? = null

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(verse: SearchedVerse, settings: Settings) {
        currentVerse = verse
        with(text) {
            text = verse.getTextForDisplay()
            setTextColor(settings.getPrimaryTextColor(resources))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources).toFloat())
        }
    }

    override fun onClick(v: View) {
        currentVerse?.let { verse -> listener.onSearchResultClicked(verse.verseIndex) }
    }
}
