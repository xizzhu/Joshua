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

package me.xizzhu.android.joshua.reading.search

import android.content.DialogInterface
import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.infra.arch.onEachSuccess
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.logger.Log

data class SearchButtonViewHolder(val searchButton: SearchFloatingActionButton) : ViewHolder

class SearchButtonPresenter(private val readingActivity: ReadingActivity,
                            private val navigator: Navigator,
                            searchButtonInteractor: SearchButtonInteractor,
                            dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<SearchButtonViewHolder, SearchButtonInteractor>(searchButtonInteractor, dispatcher) {
    @UiThread
    override fun onBind(viewHolder: SearchButtonViewHolder) {
        super.onBind(viewHolder)

        viewHolder.searchButton.setOnClickListener { openSearch() }
    }

    private fun openSearch() {
        try {
            navigator.navigate(readingActivity, Navigator.SCREEN_SEARCH)
        } catch (e: Exception) {
            Log.e(tag, "Failed to open search activity", e)
            DialogHelper.showDialog(readingActivity, true, R.string.dialog_navigate_to_search_error,
                    DialogInterface.OnClickListener { _, _ -> openSearch() })
        }
    }

    @UiThread
    override fun onStart() {
        super.onStart()

        interactor.settings().onEachSuccess { settings ->
            if (settings.hideSearchButton) {
                viewHolder?.searchButton?.hide()
            } else {
                viewHolder?.searchButton?.show()
            }
        }.launchIn(coroutineScope)
    }
}
