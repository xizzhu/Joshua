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

import android.view.LayoutInflater
import android.view.ViewGroup
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.reading.detail.VerseDetail
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView

class VersesPage(inflater: LayoutInflater, container: ViewGroup)
    : VerseDetailPage(inflater.inflate(R.layout.page_verse_detail_verses, container, false)) {
    private val verseTextListView: CommonRecyclerView = itemView.findViewById<CommonRecyclerView>(R.id.verse_text_list)
            .apply { isNestedScrollingEnabled = false }

    override fun bind(verseDetail: VerseDetail, settings: Settings) {
        with(verseTextListView) {
            setItems(verseDetail.verseTextItems)
            setSettings(settings)
        }
    }
}
