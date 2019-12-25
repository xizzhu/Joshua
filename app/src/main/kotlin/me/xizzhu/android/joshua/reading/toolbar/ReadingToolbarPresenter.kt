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
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.logger.Log

data class ReadingToolbarViewHolder(val readingToolbar: ReadingToolbar) : ViewHolder

class ReadingToolbarPresenter(private val readingActivity: ReadingActivity,
                              private val navigator: Navigator,
                              readingToolbarInteractor: ReadingToolbarInteractor,
                              dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<ReadingToolbarViewHolder, ReadingToolbarInteractor>(readingToolbarInteractor, dispatcher) {
    private val translationComparator = TranslationInfoComparator(
            TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)

    private val downloadedTranslations = ArrayList<TranslationInfo>()
    private var currentTranslation = ""

    @UiThread
    override fun onCreate(viewHolder: ReadingToolbarViewHolder) {
        super.onCreate(viewHolder)

        viewHolder.readingToolbar.initialize(
                onParallelTranslationRequested = { requestParallelTranslation(it) },
                onParallelTranslationRemoved = { removeParallelTranslation(it) },
                onSpinnerItemSelected = { translationShortName ->
                    var isDownloadedTranslation = false
                    for (translation in downloadedTranslations) {
                        if (translation.shortName == translationShortName) {
                            if (currentTranslation != translationShortName) {
                                updateCurrentTranslation(translationShortName)
                            }
                            isDownloadedTranslation = true
                            break
                        }
                    }

                    if (!isDownloadedTranslation) startTranslationManagementActivity()
                }
        )

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
                R.id.action_search -> {
                    startActivity(Navigator.SCREEN_SEARCH, R.string.dialog_navigate_to_search_error)
                    true
                }
                R.id.action_settings -> {
                    startActivity(Navigator.SCREEN_SETTINGS, R.string.dialog_navigate_to_settings_error)
                    true
                }
                else -> false
            }
        }

        observeDownloadedTranslations()
        observeCurrentTranslation()
        observeParallelTranslations()
        observeBookNames()
    }

    private fun requestParallelTranslation(translationShortName: String) {
        coroutineScope.launch {
            try {
                interactor.requestParallelTranslation(translationShortName)
            } catch (e: Exception) {
                Log.e(tag, "Failed to request parallel translation", e)
                // TODO
            }
        }
    }

    private fun removeParallelTranslation(translationShortName: String) {
        coroutineScope.launch {
            try {
                interactor.removeParallelTranslation(translationShortName)
            } catch (e: Exception) {
                Log.e(tag, "Failed to remove parallel translation", e)
                // TODO
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
        coroutineScope.launch {
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

    private fun observeDownloadedTranslations() {
        interactor.downloadedTranslations().onEachSuccess { translations ->
            if (translations.isEmpty()) {
                DialogHelper.showDialog(readingActivity, false, R.string.dialog_no_translation_downloaded,
                        DialogInterface.OnClickListener { _, _ -> startTranslationManagementActivity() },
                        DialogInterface.OnClickListener { _, _ -> readingActivity.finish() })
            } else {
                downloadedTranslations.clear()
                downloadedTranslations.addAll(translations.sortedWith(translationComparator))

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

                viewHolder?.readingToolbar?.run {
                    setTranslationShortNames(names)
                    setSpinnerSelection(selected)
                }
            }
        }.launchIn(coroutineScope)
    }

    private fun observeCurrentTranslation() {
        interactor.currentTranslation().onEachSuccess { translationShortName ->
            currentTranslation = translationShortName

            var selected = 0
            for (i in 0 until downloadedTranslations.size) {
                if (currentTranslation == downloadedTranslations[i].shortName) {
                    selected = i
                    break
                }
            }

            viewHolder?.readingToolbar?.run {
                setCurrentTranslation(currentTranslation)
                setSpinnerSelection(selected)
            }
        }.launchIn(coroutineScope)
    }

    private fun observeParallelTranslations() {
        interactor.parallelTranslations()
                .onEachSuccess { viewHolder?.readingToolbar?.setParallelTranslations(it) }
                .launchIn(coroutineScope)
    }

    private fun observeBookNames() {
        interactor.currentVerseIndex()
                .combineOnSuccess(interactor.bookShortNames()) { currentVerseIndex, bookShortNames ->
                    viewHolder?.readingToolbar?.title =
                            "${bookShortNames[currentVerseIndex.bookIndex]}, ${currentVerseIndex.chapterIndex + 1}"
                }.launchIn(coroutineScope)
    }
}
