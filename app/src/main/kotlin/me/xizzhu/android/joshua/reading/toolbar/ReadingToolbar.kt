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

package me.xizzhu.android.joshua.reading.toolbar

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.utils.MVPView
import java.lang.StringBuilder

interface ToolbarView : MVPView {
    fun onNoTranslationsDownloaded()

    fun onDownloadedTranslationsLoaded(translations: List<TranslationInfo>)

    fun onCurrentTranslationUpdated(translationShortName: String)

    fun onCurrentTranslationUpdateFailed(translationShortName: String)

    fun onParallelTranslationsUpdated(parallelTranslations: List<String>)

    fun onCurrentVerseIndexUpdated(verseIndex: VerseIndex)

    fun onBookNamesUpdated(bookNames: List<String>)

    fun onFailedToNavigateToTranslationManagement()

    fun onFailedToNavigateToReadingProgress()
}

class ReadingToolbar : Toolbar, ToolbarView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var presenter: ToolbarPresenter

    private val translationSpinnerItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position == translationSpinnerAdapter.count - 1) {
                presenter.openTranslationManagement()
                return
            }

            val selectedTranslation = downloadedTranslations[position].shortName
            if (currentTranslation != selectedTranslation) {
                presenter.updateCurrentTranslation(selectedTranslation)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }
    }
    private val translationSpinnerAdapterListener = object : TranslationSpinnerAdapter.Listener {
        override fun onParallelTranslationRequested(translationShortName: String) {
            presenter.requestParallelTranslation(translationShortName)
        }

        override fun onParallelTranslationRemoved(translationShortName: String) {
            presenter.removeParallelTranslation(translationShortName)
        }
    }
    private val translationSpinnerAdapter = TranslationSpinnerAdapter(context, translationSpinnerAdapterListener)

    private val onMenuItemClickListener = OnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.action_reading_progress -> {
                presenter.openReadingProgress()
                return@OnMenuItemClickListener true
            }
            else -> return@OnMenuItemClickListener false
        }
    }

    init {
        setTitle(R.string.app_name)

        inflateMenu(R.menu.menu_bible_reading)
        setOnMenuItemClickListener(onMenuItemClickListener)

        val translationSpinner = menu.findItem(R.id.action_translations).actionView as Spinner
        translationSpinner.adapter = translationSpinnerAdapter
        translationSpinner.onItemSelectedListener = translationSpinnerItemSelectedListener
    }

    private val titleBuilder = StringBuilder()
    private val downloadedTranslations = ArrayList<TranslationInfo>()
    private val bookNames = ArrayList<String>()
    private var currentTranslation = ""
    private var verseIndex = VerseIndex.INVALID

    fun setPresenter(presenter: ToolbarPresenter) {
        this.presenter = presenter
    }

    override fun onNoTranslationsDownloaded() {
        DialogHelper.showDialog(context, false, R.string.dialog_no_translation_downloaded,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.openTranslationManagement()
                },
                DialogInterface.OnClickListener { _, _ ->
                    presenter.finish()
                })
    }

    override fun onDownloadedTranslationsLoaded(translations: List<TranslationInfo>) {
        downloadedTranslations.clear()
        downloadedTranslations.addAll(translations)

        val names = ArrayList<String>(downloadedTranslations.size + 1)
        var selected = 0
        for (i in 0 until downloadedTranslations.size) {
            val translation = downloadedTranslations[i]
            if (currentTranslation == translation.shortName) {
                selected = i
            }
            names.add(translation.shortName)
        }
        names.add(resources.getString(R.string.menu_more_translation)) // amends "More" to the end of the list

        translationSpinnerAdapter.setTranslationShortNames(names)

        (menu.findItem(R.id.action_translations).actionView as Spinner).setSelection(selected)
    }

    override fun onCurrentTranslationUpdated(translationShortName: String) {
        currentTranslation = translationShortName
        translationSpinnerAdapter.setCurrentTranslation(currentTranslation)

        var selected = 0
        for (i in 0 until downloadedTranslations.size) {
            val translation = downloadedTranslations[i]
            if (currentTranslation == translation.shortName) {
                selected = i
            }
        }
        (menu.findItem(R.id.action_translations).actionView as Spinner).setSelection(selected)
    }

    override fun onCurrentTranslationUpdateFailed(translationShortName: String) {
        DialogHelper.showDialog(context, true, R.string.dialog_update_translation_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.updateCurrentTranslation(translationShortName)
                })
    }

    override fun onParallelTranslationsUpdated(parallelTranslations: List<String>) {
        translationSpinnerAdapter.setParallelTranslations(parallelTranslations)
    }

    override fun onCurrentVerseIndexUpdated(verseIndex: VerseIndex) {
        this.verseIndex = verseIndex
        updateTitle()
    }

    private fun updateTitle() {
        if (bookNames.isEmpty() || !verseIndex.isValid()) {
            return
        }

        titleBuilder.setLength(0)
        titleBuilder.append(bookNames[verseIndex.bookIndex]).append(", ").append(verseIndex.chapterIndex + 1)
        title = titleBuilder.toString()
    }

    override fun onBookNamesUpdated(bookNames: List<String>) {
        this.bookNames.clear()
        this.bookNames.addAll(bookNames)
        updateTitle()
    }

    override fun onFailedToNavigateToTranslationManagement() {
        DialogHelper.showDialog(context, true, R.string.dialog_navigate_to_translation_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.openTranslationManagement()
                })
    }

    override fun onFailedToNavigateToReadingProgress() {
        DialogHelper.showDialog(context, true, R.string.dialog_navigate_to_reading_progress_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.openReadingProgress()
                })
    }
}
