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
import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.utils.BaseActivity
import me.xizzhu.android.joshua.utils.MVPView
import javax.inject.Inject

interface ReadingView : MVPView {
    fun onCurrentVerseIndexLoaded(currentVerse: VerseIndex)

    fun onCurrentVerseIndexLoadFailed()

    fun onCurrentTranslationLoaded(currentTranslation: String)

    fun onNoCurrentTranslation()

    fun onCurrentTranslationLoadFailed()

    fun onBookNamesLoaded(bookNames: List<String>)

    fun onBookNamesLoadFailed()
}

class ReadingActivity : BaseActivity(), ReadingView {
    @Inject
    lateinit var presenter: ReadingPresenter

    private lateinit var toolbar: ReadingToolbar

    private val bookNames = ArrayList<String>()
    private var currentTranslation = ""
    private var currentVerse = VerseIndex.INVALID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading)
        toolbar = findViewById(R.id.toolbar)
    }

    override fun onStart() {
        super.onStart()
        presenter.takeView(this)
        presenter.loadCurrentTranslation()
        presenter.loadCurrentVerseIndex()
    }

    override fun onStop() {
        presenter.dropView()
        super.onStop()
    }

    override fun onCurrentVerseIndexLoaded(currentVerse: VerseIndex) {
        this.currentVerse = currentVerse
        toolbar.setVerseIndex(currentVerse)
    }

    override fun onCurrentVerseIndexLoadFailed() {
        // TODO
    }

    override fun onCurrentTranslationLoaded(currentTranslation: String) {
        this.currentTranslation = currentTranslation
        presenter.loadBookNames(currentTranslation)
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
        toolbar.setBookNames(bookNames)
    }

    override fun onBookNamesLoadFailed() {
        // TODO
    }
}
