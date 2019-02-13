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
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.toolbar.ReadingToolbar
import me.xizzhu.android.joshua.reading.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.MVPView
import javax.inject.Inject

interface ReadingView : MVPView {
    fun onCurrentVerseIndexLoaded(verseIndex: VerseIndex)

    fun onCurrentVerseIndexLoadFailed()

    fun onCurrentVerseIndexUpdateFailed()

    fun onCurrentTranslationLoaded(currentTranslation: String)

    fun onNoCurrentTranslation()

    fun onCurrentTranslationLoadFailed()

    fun onBookNamesLoaded(bookNames: List<String>)

    fun onBookNamesLoadFailed()

    fun onVersesLoaded(bookIndex: Int, chapterIndex: Int, verses: List<Verse>)

    fun onVersesLoadFailed()
}

class ReadingActivity : BaseActivity(), ReadingView, ChapterSelectionView.Listener, VerseViewPager.Listener {
    @Inject
    lateinit var presenter: ReadingPresenter

    @Inject
    lateinit var toolbarPresenter: ToolbarPresenter

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: ReadingToolbar
    private lateinit var chapterSelectionView: ChapterSelectionView
    private lateinit var verseViewPager: VerseViewPager

    private val bookNames = ArrayList<String>()
    private var currentTranslation = ""
    private var currentVerse = VerseIndex.INVALID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reading)
        drawerLayout = findViewById(R.id.drawer_layout)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setPresenter(toolbarPresenter)
        lifecycle.addObserver(toolbar)

        chapterSelectionView = findViewById(R.id.chapter_selection_view)
        chapterSelectionView.setListener(this)
        verseViewPager = findViewById(R.id.verse_view_pager)
        verseViewPager.setListener(this)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(drawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onStart() {
        super.onStart()
        presenter.takeView(this)
        presenter.loadCurrentReadingProgress()
    }

    override fun onStop() {
        presenter.dropView()
        super.onStop()
    }

    override fun onDestroy() {
        lifecycle.removeObserver(toolbar)
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCurrentVerseIndexLoaded(verseIndex: VerseIndex) {
        if (currentVerse == verseIndex) {
            return
        }
        currentVerse = verseIndex
        chapterSelectionView.setCurrentVerseIndex(verseIndex)
        verseViewPager.setCurrentVerseIndex(verseIndex)
    }

    override fun onCurrentVerseIndexLoadFailed() {
        // TODO
    }

    override fun onCurrentVerseIndexUpdateFailed() {
        // TODO
    }

    override fun onCurrentTranslationLoaded(currentTranslation: String) {
        this.currentTranslation = currentTranslation
    }

    override fun onNoCurrentTranslation() {
        DialogHelper.showDialog(this, false, R.string.no_translation_downloaded,
                DialogInterface.OnClickListener { _, _ ->
                    startActivity(TranslationManagementActivity.newStartIntent(this))
                },
                DialogInterface.OnClickListener { _, _ ->
                    finish()
                })
    }

    override fun onCurrentTranslationLoadFailed() {
        // TODO
    }

    override fun onBookNamesLoaded(bookNames: List<String>) {
        this.bookNames.clear()
        this.bookNames.addAll(bookNames)
        chapterSelectionView.setBookNames(bookNames)
    }

    override fun onBookNamesLoadFailed() {
        // TODO
    }

    override fun onVersesLoaded(bookIndex: Int, chapterIndex: Int, verses: List<Verse>) {
        verseViewPager.setVerses(bookIndex, chapterIndex, verses)
    }

    override fun onVersesLoadFailed() {
        // TODO
    }

    override fun onChapterSelected(currentVerseIndex: VerseIndex) {
        if (currentVerse == currentVerseIndex) {
            return
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        presenter.updateCurrentVerseIndex(currentVerseIndex)
    }

    override fun onChapterRequested(bookIndex: Int, chapterIndex: Int) {
        presenter.loadVerses(currentTranslation, bookIndex, chapterIndex)
    }
}
