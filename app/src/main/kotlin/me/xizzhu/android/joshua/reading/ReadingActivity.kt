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

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.reading.chapter.ChapterListPresenter
import me.xizzhu.android.joshua.reading.chapter.ChapterListView
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbar
import me.xizzhu.android.joshua.reading.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.reading.verse.VersePresenter
import me.xizzhu.android.joshua.reading.verse.VerseViewPager
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.onNext
import javax.inject.Inject

class ReadingActivity : BaseActivity() {
    @Inject
    lateinit var readingManager: ReadingManager

    @Inject
    lateinit var toolbarPresenter: ToolbarPresenter

    @Inject
    lateinit var chapterListPresenter: ChapterListPresenter

    @Inject
    lateinit var versePresenter: VersePresenter

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: ReadingToolbar
    private lateinit var chapterListView: ChapterListView
    private lateinit var verseViewPager: VerseViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reading)
        drawerLayout = findViewById(R.id.drawer_layout)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setPresenter(toolbarPresenter)

        chapterListView = findViewById(R.id.chapter_list_view)
        chapterListView.setPresenter(chapterListPresenter)

        verseViewPager = findViewById(R.id.verse_view_pager)
        verseViewPager.setPresenter(versePresenter)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(drawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onStart() {
        super.onStart()

        toolbarPresenter.attachView(toolbar)
        chapterListPresenter.attachView(chapterListView)
        versePresenter.attachView(verseViewPager)

        launch(Dispatchers.Main) {
            receiveChannels.add(readingManager.observeDownloadedTranslations().onNext {
                if (it.isEmpty()) {
                    DialogHelper.showDialog(this@ReadingActivity, false, R.string.no_translation_downloaded,
                            DialogInterface.OnClickListener { _, _ ->
                                startActivity(TranslationManagementActivity.newStartIntent(this@ReadingActivity))
                            },
                            DialogInterface.OnClickListener { _, _ ->
                                finish()
                            })
                }
            })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(readingManager.observeCurrentVerseIndex().onNext {
                if (it.isValid()) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            })
        }
    }

    override fun onStop() {
        toolbarPresenter.detachView()
        chapterListPresenter.detachView()
        versePresenter.detachView()

        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }
}
