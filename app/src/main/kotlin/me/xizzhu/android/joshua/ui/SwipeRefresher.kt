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
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.MVPView

enum class SwipeRefresherState { IS_REFRESHING, NOT_REFRESHING }

class SwipeRefresherPresenter(refresherState: ReceiveChannel<SwipeRefresherState>,
                              private val refreshRequest: SendChannel<Unit>) : MVPPresenter<SwipeRefresherView>() {
    init {
        coroutineScope.launch(Dispatchers.Main) {
            refresherState.consumeEach { state ->
                when (state) {
                    SwipeRefresherState.IS_REFRESHING -> view?.show()
                    SwipeRefresherState.NOT_REFRESHING -> view?.hide()
                }
            }
        }
    }

    fun refresh() {
        coroutineScope.launch(Dispatchers.Main) { refreshRequest.send(Unit) }
    }
}

interface SwipeRefresherView : MVPView {
    fun show()

    fun hide()
}

class SwipeRefresher : SwipeRefreshLayout, SwipeRefresherView {
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
