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

package me.xizzhu.android.joshua.infra.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.ui.fadeIn

class LoadingSpinnerInteractor(dispatcher: CoroutineDispatcher = Dispatchers.Default) : Interactor(dispatcher) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val loadingState: BroadcastChannel<ViewData<Unit>> = ConflatedBroadcastChannel()

    fun loadingState(): Flow<ViewData<Unit>> = loadingState.asFlow()

    fun updateLoadingState(state: ViewData<Unit>) {
        loadingState.offer(state)
    }
}

class LoadingSpinner : ProgressBar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
}

data class LoadingSpinnerViewHolder(val loadingSpinner: LoadingSpinner) : ViewHolder

class LoadingSpinnerPresenter(interactor: LoadingSpinnerInteractor,
                              dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<LoadingSpinnerViewHolder, LoadingSpinnerInteractor>(interactor, dispatcher) {
    @UiThread
    override fun onBind(viewHolder: LoadingSpinnerViewHolder) {
        super.onBind(viewHolder)

        coroutineScope.launch(Dispatchers.Main) {
            interactor.loadingState().collect {
                when (it.status) {
                    ViewData.STATUS_SUCCESS -> viewHolder.loadingSpinner.visibility = View.GONE
                    ViewData.STATUS_ERROR -> viewHolder.loadingSpinner.visibility = View.GONE
                    ViewData.STATUS_LOADING -> viewHolder.loadingSpinner.fadeIn()
                }
            }
        }
    }
}
