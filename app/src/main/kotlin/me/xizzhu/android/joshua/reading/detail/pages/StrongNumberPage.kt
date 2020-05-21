/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading.detail.pages

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.reading.detail.StrongNumberItem
import me.xizzhu.android.joshua.reading.detail.VerseDetail
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView

class StrongNumberPage(inflater: LayoutInflater, container: ViewGroup, onNoStrongNumberClicked: () -> Unit)
    : VerseDetailPage(inflater.inflate(R.layout.page_verse_detail_strong_number, container, false)) {
    private val emptyStrongNumberList: TextView = itemView.findViewById<TextView>(R.id.empty_strong_number_list)
            .apply { setOnClickListener { onNoStrongNumberClicked() } }
    private val strongNumberListView: CommonRecyclerView = itemView.findViewById<CommonRecyclerView>(R.id.strong_number_list)
            .apply { isNestedScrollingEnabled = false }

    override fun bind(verseDetail: VerseDetail, settings: Settings) {
        if (verseDetail.strongNumberItems.isEmpty()) {
            bindNoStrongNumberView(settings)
        } else {
            bindStrongNumberItems(verseDetail.strongNumberItems, settings)
        }
    }

    private fun bindNoStrongNumberView(settings: Settings) {
        with(emptyStrongNumberList) {
            visibility = View.VISIBLE
            setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))
            setTextColor(settings.getPrimaryTextColor(resources))
        }

        strongNumberListView.visibility = View.GONE
    }

    private fun bindStrongNumberItems(strongNumberItems: List<StrongNumberItem>, settings: Settings) {
        emptyStrongNumberList.visibility = View.GONE

        with(strongNumberListView) {
            visibility = View.VISIBLE
            setItems(strongNumberItems)
            setSettings(settings)
        }
    }
}
