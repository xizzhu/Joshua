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

import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.logger.Log
import java.lang.StringBuilder

data class ReadingToolbarViewHolder(val readingToolbar: ReadingToolbar) : ViewHolder

class ReadingToolbarPresenter(private val readingActivity: ReadingActivity,
                              private val navigator: Navigator,
                              readingToolbarInteractor: ReadingToolbarInteractor,
                              dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<ReadingToolbarViewHolder, ReadingToolbarInteractor>(readingToolbarInteractor, dispatcher) {
    private val translationComparator = TranslationInfoComparator(
            TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)

    private val titleBuilder = StringBuilder()
    private val downloadedTranslations = ArrayList<TranslationInfo>()
    private val bookShortNames = ArrayList<String>()
    private var currentTranslation = ""
    private var verseIndex = VerseIndex.INVALID


    private val translationSpinnerAdapter = TranslationSpinnerAdapter(context = readingActivity,
            onParallelTranslationRequested = { interactor.requestParallelTranslation(it) },
            onParallelTranslationRemoved = { interactor.removeParallelTranslation(it) })

    @UiThread
    override fun onBind(viewHolder: ReadingToolbarViewHolder) {
        super.onBind(viewHolder)

        viewHolder.readingToolbar.initializeSpinner(translationSpinnerAdapter,
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position == translationSpinnerAdapter.count - 1) {
                            startTranslationManagementActivity()
                            return
                        }

                        val selectedTranslation = downloadedTranslations[position].shortName
                        if (currentTranslation != selectedTranslation) {
                            updateCurrentTranslation(selectedTranslation)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // do nothing
                    }
                })

        viewHolder.readingToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_reading_progress -> {
                    startActivity(Navigator.SCREEN_READING_PROGRESS, R.string.dialog_navigate_to_reading_progress_error)
                    true
                }
                R.id.action_bookmarks -> {
                    startActivity(Navigator.SCREEN_BOOKMARKS, R.string.dialog_navigate_to_bookmarks_error)
                    true
                }
                R.id.action_highlights -> {
                    startActivity(Navigator.SCREEN_HIGHLIGHTS, R.string.dialog_navigate_to_highlights_error)
                    true
                }
                R.id.action_notes -> {
                    startActivity(Navigator.SCREEN_NOTES, R.string.dialog_navigate_to_notes_error)
                    true
                }
                R.id.action_settings -> {
                    startActivity(Navigator.SCREEN_SETTINGS, R.string.dialog_navigate_to_settings_error)
                    true
                }
                else -> false
            }
        }
    }

    private fun startTranslationManagementActivity() {
        startActivity(Navigator.SCREEN_TRANSLATION_MANAGEMENT, R.string.dialog_navigate_to_translation_error)
    }

    private fun startActivity(@Navigator.Companion.Screen screen: Int, @StringRes errorMessage: Int) {
        try {
            navigator.navigate(readingActivity, screen)
        } catch (e: Exception) {
            Log.e(tag, "Failed to open activity", e)
            DialogHelper.showDialog(readingActivity, true, errorMessage,
                    DialogInterface.OnClickListener { _, _ -> startActivity(screen, errorMessage) })
        }
    }

    private fun updateCurrentTranslation(translationShortName: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                interactor.saveCurrentTranslation(translationShortName)
                try {
                    interactor.clearParallelTranslation()
                } catch (e: Exception) {
                    Log.w(tag, "Failed to clear parallel translation", e)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to update current translation", e)
                DialogHelper.showDialog(readingActivity, true, R.string.dialog_update_translation_error,
                        DialogInterface.OnClickListener { _, _ -> updateCurrentTranslation(translationShortName) })
            }
        }
    }

    @UiThread
    override fun onStart() {
        super.onStart()

        coroutineScope.launch {
            interactor.downloadedTranslations().run {
                if (isEmpty()) {
                    DialogHelper.showDialog(readingActivity, false, R.string.dialog_no_translation_downloaded,
                            DialogInterface.OnClickListener { _, _ -> startTranslationManagementActivity() },
                            DialogInterface.OnClickListener { _, _ -> readingActivity.finish() })
                } else {
                    onDownloadedTranslationsLoaded(sortedWith(translationComparator))
                }
            }
        }
        coroutineScope.launch {
            interactor.currentTranslation().filter { it.isNotEmpty() }
                    .collect { onCurrentTranslationUpdated(it) }
        }
        coroutineScope.launch {
            interactor.currentVerseIndex().filter { it.isValid() }
                    .collect {
                        verseIndex = it
                        updateTitle()
                    }
        }
        coroutineScope.launch {
            interactor.parallelTranslations().collect { translationSpinnerAdapter.setParallelTranslations(it) }
        }
    }

    private fun onDownloadedTranslationsLoaded(translations: List<TranslationInfo>) {
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
        names.add(readingActivity.getString(R.string.menu_more_translation)) // amends "More" to the end of the list

        translationSpinnerAdapter.setTranslationShortNames(names)

        viewHolder?.readingToolbar?.setSpinnerSelection(selected)
    }

    private suspend fun onCurrentTranslationUpdated(translationShortName: String) {
        currentTranslation = translationShortName
        translationSpinnerAdapter.setCurrentTranslation(currentTranslation)

        var selected = 0
        for (i in 0 until downloadedTranslations.size) {
            val translation = downloadedTranslations[i]
            if (currentTranslation == translation.shortName) {
                selected = i
            }
        }
        viewHolder?.readingToolbar?.setSpinnerSelection(selected)

        bookShortNames.clear()
        bookShortNames.addAll(interactor.readBookShortNames(translationShortName))
        updateTitle()
    }

    private fun updateTitle() {
        if (bookShortNames.isEmpty() || !verseIndex.isValid()) {
            return
        }

        viewHolder?.readingToolbar?.title = with(titleBuilder) {
            setLength(0)
            append(bookShortNames[verseIndex.bookIndex]).append(", ").append(verseIndex.chapterIndex + 1)
            return@with toString()
        }
    }
}
