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

package me.xizzhu.android.joshua.bookmarks

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor

class BookmarksListAdapter(context: Context, private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface Listener {
        fun onBookmarkClicked(verseIndex: VerseIndex)
    }

    private val inflater = LayoutInflater.from(context)
    private val resources = context.resources

    private val bookmarks: ArrayList<BookmarkForDisplay> = ArrayList()
    private var settings: Settings? = null

    fun setBookmarks(bookmarks: List<BookmarkForDisplay>) {
        this.bookmarks.clear()
        this.bookmarks.addAll(bookmarks)
        notifyDataSetChanged()
    }

    fun setSettings(settings: Settings) {
        this.settings = settings
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (settings == null) 0 else bookmarks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            BookmarkItemViewHolder(inflater, parent, resources, listener)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BookmarkItemViewHolder).bind(bookmarks[position], settings!!)
    }
}

private class BookmarkItemViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                     private val resources: Resources, private val listener: BookmarksListAdapter.Listener)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_bookmarks, parent, false)), View.OnClickListener {
    private val text: TextView = itemView.findViewById(R.id.text)
    private var currentBookmark: BookmarkForDisplay? = null

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(bookmark: BookmarkForDisplay, settings: Settings) {
        currentBookmark = bookmark

        text.setTextColor(settings.getPrimaryTextColor(resources))
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources).toFloat())
        text.text = bookmark.getTextForDisplay()
    }

    override fun onClick(v: View) {
        currentBookmark?.let { bookmark -> listener.onBookmarkClicked(bookmark.verseIndex) }
    }
}
