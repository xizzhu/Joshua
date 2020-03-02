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

package me.xizzhu.android.joshua.reading.search

import android.content.DialogInterface
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.logger.Log

data class SearchButtonViewHolder(val searchButton: SearchFloatingActionButton) : ViewHolder

class SearchButtonPresenter(
        private val navigator: Navigator, readingViewModel: ReadingViewModel, readingActivity: ReadingActivity,
        coroutineScope: CoroutineScope = readingActivity.lifecycleScope
) : BaseSettingsPresenter<SearchButtonViewHolder, ReadingViewModel, ReadingActivity>(readingViewModel, readingActivity, coroutineScope) {
    @UiThread
    override fun onBind() {
        super.onBind()

        viewHolder.searchButton.setOnClickListener { openSearch() }
    }

    private fun openSearch() {
        try {
            navigator.navigate(activity, Navigator.SCREEN_SEARCH)
        } catch (e: Exception) {
            Log.e(tag, "Failed to open search activity", e)
            activity.dialog(true, R.string.dialog_navigation_error,
                    DialogInterface.OnClickListener { _, _ -> openSearch() })
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun observeSettings() {
        viewModel.settings().onEach { settings ->
            if (settings.hideSearchButton) {
                viewHolder.searchButton.hide()
            } else {
                viewHolder.searchButton.show()
            }
        }.launchIn(coroutineScope)
    }
}
