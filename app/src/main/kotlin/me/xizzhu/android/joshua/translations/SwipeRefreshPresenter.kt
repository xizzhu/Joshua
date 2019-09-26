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

package me.xizzhu.android.joshua.translations

import androidx.annotation.UiThread
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter

data class SwipeRefreshViewHolder(val swipeRefreshLayout: SwipeRefreshLayout) : ViewHolder

class SwipeRefreshPresenter(swipeRefreshInteractor: SwipeRefreshInteractor,
                            dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<SwipeRefreshViewHolder, SwipeRefreshInteractor>(swipeRefreshInteractor, dispatcher) {
    @UiThread
    override fun onBind(viewHolder: SwipeRefreshViewHolder) {
        super.onBind(viewHolder)

        with(viewHolder.swipeRefreshLayout) {
            setColorSchemeResources(R.color.primary_dark, R.color.primary, R.color.dark_cyan, R.color.dark_lime)
            setOnRefreshListener { interactor.requestRefresh() }
        }

        coroutineScope.launch(Dispatchers.Main) {
            interactor.loadingState().collect {
                viewHolder.swipeRefreshLayout.isRefreshing = ViewData.STATUS_LOADING == it.status
            }
        }
    }
}
