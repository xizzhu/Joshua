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

package me.xizzhu.android.joshua.ui

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.SettingsManager

abstract class BaseSwipeRefresherInteractor(settingsManager: SettingsManager,
                                            @Companion.LoadingState initialLoadingState: Int)
    : BaseLoadingAwareInteractor(settingsManager, initialLoadingState) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val refreshRequest: BroadcastChannel<Unit> = ConflatedBroadcastChannel()

    fun observeRefreshRequest(): Flow<Unit> = refreshRequest.asFlow()

    fun notifyRefreshRequested() {
        refreshRequest.offer(Unit)
    }
}

class SwipeRefresherPresenter(loadingState: Flow<Int>,
                              private val baseSwipeRefresherInteractor: BaseSwipeRefresherInteractor)
    : LoadingAwarePresenter(loadingState) {
    fun refresh() {
        baseSwipeRefresherInteractor.notifyRefreshRequested()
    }
}

class SwipeRefresher : SwipeRefreshLayout, LoadingAwareView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val onRefreshListener = OnRefreshListener { presenter.refresh() }

    private lateinit var presenter: SwipeRefresherPresenter

    init {
        setColorSchemeResources(R.color.primary_dark, R.color.primary, R.color.dark_cyan, R.color.dark_lime)
        setOnRefreshListener(onRefreshListener)
    }

    fun setPresenter(presenter: SwipeRefresherPresenter) {
        this.presenter = presenter
    }

    override fun show() {
        isRefreshing = true
    }

    override fun hide() {
        isRefreshing = false
    }
}
