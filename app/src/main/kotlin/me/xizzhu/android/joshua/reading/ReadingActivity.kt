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

package me.xizzhu.android.joshua.reading

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.activity.BaseSettingsActivity
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.reading.chapter.ChapterListPresenter
import me.xizzhu.android.joshua.reading.chapter.ChapterListViewHolder
import me.xizzhu.android.joshua.reading.chapter.ReadingDrawerLayout
import me.xizzhu.android.joshua.reading.detail.VerseDetailPresenter
import me.xizzhu.android.joshua.reading.detail.VerseDetailViewHolder
import me.xizzhu.android.joshua.reading.search.SearchButtonPresenter
import me.xizzhu.android.joshua.reading.search.SearchButtonViewHolder
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbar
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbarPresenter
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbarViewHolder
import me.xizzhu.android.joshua.reading.verse.VersePresenter
import me.xizzhu.android.joshua.reading.verse.VerseViewHolder
import javax.inject.Inject

class ReadingActivity : BaseSettingsActivity() {
    companion object {
        private const val KEY_OPEN_NOTE = "me.xizzhu.android.joshua.KEY_OPEN_NOTE"

        fun bundleForOpenNote(): Bundle = Bundle().apply { putBoolean(KEY_OPEN_NOTE, true) }
    }

    @Inject
    lateinit var readingViewModel: ReadingViewModel

    @Inject
    lateinit var readingToolbarPresenter: ReadingToolbarPresenter

    @Inject
    lateinit var chapterListPresenter: ChapterListPresenter

    @Inject
    lateinit var searchButtonPresenter: SearchButtonPresenter

    @Inject
    lateinit var versePresenter: VersePresenter

    @Inject
    lateinit var verseDetailPresenter: VerseDetailPresenter

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var readingDrawerLayout: ReadingDrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reading)

        val toolbar: ReadingToolbar = findViewById(R.id.toolbar)
        readingDrawerLayout = findViewById(R.id.drawer_layout)
        drawerToggle = ActionBarDrawerToggle(this, readingDrawerLayout, toolbar, 0, 0)
        readingDrawerLayout.addDrawerListener(drawerToggle)

        readingToolbarPresenter.bind(ReadingToolbarViewHolder(toolbar))
        chapterListPresenter.bind(ChapterListViewHolder(readingDrawerLayout, findViewById(R.id.chapter_list_view)))
        searchButtonPresenter.bind(SearchButtonViewHolder(findViewById(R.id.search)))
        versePresenter.bind(VerseViewHolder(findViewById(R.id.verse_view_pager)))
        verseDetailPresenter.bind(VerseDetailViewHolder(findViewById(R.id.verse_detail_view)))

        if (intent.getBooleanExtra(KEY_OPEN_NOTE, false)) readingViewModel.showNoteInVerseDetail()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onResume() {
        super.onResume()
        readingViewModel.startTracking()
    }

    override fun onPause() {
        readingViewModel.stopTracking()
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (!verseDetailPresenter.close() && !readingDrawerLayout.hide()) {
            super.onBackPressed()
        }
    }

    override fun getBaseSettingsViewModel(): BaseSettingsViewModel = readingViewModel
}
