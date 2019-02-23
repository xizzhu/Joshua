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
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.translations.TranslationManagementActivity
import me.xizzhu.android.joshua.utils.MVPView
import java.lang.StringBuilder

interface ToolbarView : MVPView {
    fun onDownloadedTranslationsLoaded(translations: List<TranslationInfo>)

    fun onCurrentTranslationUpdated(translationShortName: String)

    fun onCurrentVerseIndexUpdated(verseIndex: VerseIndex)

    fun onBookNamesUpdated(bookNames: List<String>)

    fun onError(e: Exception)
}

class ReadingToolbar : Toolbar, ToolbarView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setTitle(R.string.app_name)
        inflateMenu(R.menu.menu_bible_reading)
    }

    private lateinit var presenter: ToolbarPresenter

    private val titleBuilder = StringBuilder()
    private val downloadedTranslations = ArrayList<TranslationInfo>()
    private val bookNames = ArrayList<String>()
    private var currentTranslation = ""
    private var verseIndex = VerseIndex.INVALID

    fun setPresenter(presenter: ToolbarPresenter) {
        this.presenter = presenter
    }

    override fun onDownloadedTranslationsLoaded(translations: List<TranslationInfo>) {
        downloadedTranslations.clear()
        downloadedTranslations.addAll(translations)

        updateTranslationList()
    }

    private fun updateTranslationList() {
        if (downloadedTranslations.isEmpty() || currentTranslation.isEmpty()) {
            return
        }

        val names = ArrayList<String>(downloadedTranslations.size + 1)
        var selected = 0
        for (i in 0 until downloadedTranslations.size) {
            val translation = downloadedTranslations[i]
            if (currentTranslation == translation.shortName) {
                selected = i
            }
            names.add(translation.shortName)
        }
        names.add(resources.getString(R.string.more_translation)) // amends "More" to the end of the list

        val translationSpinner = menu.findItem(R.id.action_translations).actionView as Spinner
        translationSpinner.adapter = TranslationSpinnerAdapter(context, names)
        translationSpinner.setSelection(selected)
        translationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == names.size - 1) {
                    context.startActivity(TranslationManagementActivity.newStartIntent(context))
                    return
                }

                val selectedTranslation = names[position]
                if (currentTranslation != selectedTranslation) {
                    presenter.updateCurrentTranslation(selectedTranslation)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }
    }

    override fun onCurrentTranslationUpdated(translationShortName: String) {
        currentTranslation = translationShortName

        updateTranslationList()
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

    override fun onError(e: Exception) {
        // TODO
    }
}
