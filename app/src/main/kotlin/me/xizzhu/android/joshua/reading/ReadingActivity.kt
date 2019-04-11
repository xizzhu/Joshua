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

package me.xizzhu.android.joshua.reading

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.reading.chapter.ChapterListPresenter
import me.xizzhu.android.joshua.reading.chapter.ChapterListView
import me.xizzhu.android.joshua.reading.detail.VerseDetailPresenter
import me.xizzhu.android.joshua.reading.detail.VerseDetailViewLayout
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbar
import me.xizzhu.android.joshua.reading.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.reading.verse.VersePresenter
import me.xizzhu.android.joshua.reading.verse.VerseViewPager
import me.xizzhu.android.joshua.utils.BaseSettingsActivity
import javax.inject.Inject

class ReadingActivity : BaseSettingsActivity() {
    @Inject
    lateinit var readingInteractor: ReadingInteractor

    @Inject
    lateinit var readingDrawerPresenter: ReadingDrawerPresenter

    @Inject
    lateinit var toolbarPresenter: ToolbarPresenter

    @Inject
    lateinit var chapterListPresenter: ChapterListPresenter

    @Inject
    lateinit var versePresenter: VersePresenter

    @Inject
    lateinit var verseDetailPresenter: VerseDetailPresenter

    @Inject
    lateinit var searchButtonPresenter: SearchButtonPresenter

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: ReadingDrawerLayout
    private lateinit var toolbar: ReadingToolbar
    private lateinit var chapterListView: ChapterListView
    private lateinit var verseViewPager: VerseViewPager
    private lateinit var verseDetailView: VerseDetailViewLayout
    private lateinit var search: SearchFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reading)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById<ReadingToolbar>(R.id.toolbar).apply { setPresenter(toolbarPresenter) }
        chapterListView = findViewById<ChapterListView>(R.id.chapter_list_view).apply { setPresenter(chapterListPresenter) }
        verseViewPager = findViewById<VerseViewPager>(R.id.verse_view_pager).apply { setPresenter(versePresenter) }
        verseDetailView = findViewById<VerseDetailViewLayout>(R.id.verse_detail_view).apply { setPresenter(verseDetailPresenter) }
        search = findViewById<SearchFloatingActionButton>(R.id.search).apply { setPresenter(searchButtonPresenter) }

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(drawerToggle)

        observeSettings(readingInteractor)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onStart() {
        super.onStart()

        readingDrawerPresenter.attachView(drawerLayout)
        toolbarPresenter.attachView(toolbar)
        chapterListPresenter.attachView(chapterListView)
        versePresenter.attachView(verseViewPager)
        verseDetailPresenter.attachView(verseDetailView)
        searchButtonPresenter.attachView(search)
    }

    override fun onResume() {
        super.onResume()
        launch(Dispatchers.Main) { readingInteractor.startTrackingReadingProgress() }
    }

    override fun onPause() {
        launch(Dispatchers.Unconfined) { readingInteractor.stopTrackingReadingProgress() }
        super.onPause()
    }

    override fun onStop() {
        readingDrawerPresenter.detachView()
        toolbarPresenter.detachView()
        chapterListPresenter.detachView()
        versePresenter.detachView()
        verseDetailPresenter.detachView()
        searchButtonPresenter.detachView()

        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        launch(Dispatchers.Main) {
            if (!readingInteractor.closeVerseDetail()) {
                super.onBackPressed()
            }
        }
    }
}
