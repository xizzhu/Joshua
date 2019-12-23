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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.infra.interactors.BaseLoadingAwareInteractor
import me.xizzhu.android.joshua.ui.fadeIn

class LoadingSpinnerInteractor(dispatcher: CoroutineDispatcher = Dispatchers.Default) : BaseLoadingAwareInteractor(dispatcher)

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
    override fun onStart() {
        super.onStart()

        interactor.loadingState().onEach { loadingState ->
            when (loadingState.status) {
                ViewData.STATUS_SUCCESS -> viewHolder?.loadingSpinner?.visibility = View.GONE
                ViewData.STATUS_ERROR -> viewHolder?.loadingSpinner?.visibility = View.GONE
                ViewData.STATUS_LOADING -> viewHolder?.loadingSpinner?.fadeIn()
            }
        }.launchIn(coroutineScope)
    }
}
