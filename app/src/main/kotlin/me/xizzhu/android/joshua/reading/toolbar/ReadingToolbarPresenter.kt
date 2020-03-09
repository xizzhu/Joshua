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

package me.xizzhu.android.joshua.reading.toolbar

import android.content.DialogInterface
import androidx.annotation.UiThread
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.logger.Log

data class ReadingToolbarViewHolder(val readingToolbar: ReadingToolbar) : ViewHolder

class ReadingToolbarPresenter(
        private val navigator: Navigator, readingViewModel: ReadingViewModel, readingActivity: ReadingActivity,
        coroutineScope: CoroutineScope = readingActivity.lifecycleScope
) : BaseSettingsPresenter<ReadingToolbarViewHolder, ReadingViewModel, ReadingActivity>(readingViewModel, readingActivity, coroutineScope) {
    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)

    private val downloadedTranslations = ArrayList<TranslationInfo>()
    private var currentTranslation = ""

    @UiThread
    override fun onBind() {
        super.onBind()

        viewHolder.readingToolbar.initialize(
                onParallelTranslationRequested = ::requestParallelTranslation,
                onParallelTranslationRemoved = ::removeParallelTranslation,
                onTranslationSelected = ::selectTranslation,
                onScreenRequested = ::startActivity
        )
    }

    private fun requestParallelTranslation(translationShortName: String) {
        coroutineScope.launch {
            try {
                viewModel.requestParallelTranslation(translationShortName)
            } catch (e: Exception) {
                Log.e(tag, "Failed to request parallel translation", e)
                // TODO
            }
        }
    }

    private fun removeParallelTranslation(translationShortName: String) {
        coroutineScope.launch {
            try {
                viewModel.removeParallelTranslation(translationShortName)
            } catch (e: Exception) {
                Log.e(tag, "Failed to remove parallel translation", e)
                // TODO
            }
        }
    }

    private fun selectTranslation(translationShortName: String) {
        if (currentTranslation == translationShortName) return

        downloadedTranslations.firstOrNull { it.shortName == translationShortName }?.let {
            // the selected was one of the downloaded translations, update now
            // currentTranslation is updated by the listener
            updateCurrentTranslation(translationShortName)
        } ?: run {
            // selected "More", re-select the current translation,
            // and start translations management activity
            val index = downloadedTranslations.indexOfFirst { it.shortName == currentTranslation }
            if (index >= 0) viewHolder.readingToolbar.setSpinnerSelection(index)

            startActivity(Navigator.SCREEN_TRANSLATIONS)
        }
    }

    private fun updateCurrentTranslation(translationShortName: String) {
        coroutineScope.launch {
            try {
                viewModel.saveCurrentTranslation(translationShortName)
                try {
                    viewModel.clearParallelTranslation()
                } catch (e: Exception) {
                    Log.w(tag, "Failed to clear parallel translation", e)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to update current translation", e)
                activity.dialog(true, R.string.dialog_update_translation_error,
                        DialogInterface.OnClickListener { _, _ -> updateCurrentTranslation(translationShortName) })
            }
        }
    }

    private fun startActivity(@Navigator.Companion.Screen screen: Int) {
        try {
            navigator.navigate(activity, screen)
        } catch (e: Exception) {
            Log.e(tag, "Failed to open activity", e)
            activity.dialog(true, R.string.dialog_navigation_error,
                    DialogInterface.OnClickListener { _, _ -> startActivity(screen) })
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        observeDownloadedTranslations()
        observeCurrentTranslation()
        observeCurrentVerseIndex()
    }

    private fun observeDownloadedTranslations() {
        viewModel.downloadedTranslations().onEach { translations ->
            if (translations.isEmpty()) return@onEach

            downloadedTranslations.clear()
            downloadedTranslations.addAll(translations.sortedWith(translationComparator))

            val names = ArrayList<String>(downloadedTranslations.size + 1)
                    .apply {
                        addAll(downloadedTranslations.map { it.shortName })

                        // amends "More" to the end of the list
                        add(activity.getString(R.string.menu_more_translation))
                    }
            viewHolder.readingToolbar.setTranslationShortNames(names)

            downloadedTranslations.indexOfFirst { it.shortName == currentTranslation }
                    .let { if (it >= 0) viewHolder.readingToolbar.setSpinnerSelection(it) }
        }.launchIn(coroutineScope)
    }

    private fun observeCurrentTranslation() {
        viewModel.currentTranslationViewData().onEach { viewData ->
            currentTranslation = viewData.currentTranslation
            viewHolder.readingToolbar.setCurrentTranslation(currentTranslation)
            viewHolder.readingToolbar.setParallelTranslations(viewData.parallelTranslations)

            downloadedTranslations.indexOfFirst { it.shortName == currentTranslation }
                    .let { if (it >= 0) viewHolder.readingToolbar.setSpinnerSelection(it) }
        }.launchIn(coroutineScope)
    }

    private fun observeCurrentVerseIndex() {
        viewModel.currentVerseIndexViewData()
                .onEach { toolbarViewData ->
                    viewHolder.readingToolbar.title =
                            "${toolbarViewData.bookShortName}, ${toolbarViewData.verseIndex.chapterIndex + 1}"
                }.launchIn(coroutineScope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun checkHasDownloadedTranslation() {
        coroutineScope.launch {
            if (viewModel.hasDownloadedTranslation()) return@launch

            activity.dialog(false, R.string.dialog_no_translation_downloaded,
                    DialogInterface.OnClickListener { _, _ -> startActivity(Navigator.SCREEN_TRANSLATIONS) },
                    DialogInterface.OnClickListener { _, _ -> activity.finish() })
        }
    }
}
