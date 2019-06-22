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
import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.MVPView

enum class LoadingSpinnerState { IS_LOADING, NOT_LOADING }

class LoadingSpinnerPresenter(loadingState: ReceiveChannel<LoadingSpinnerState>) : MVPPresenter<LoadingSpinnerView>() {
    init {
        coroutineScope.launch(Dispatchers.Main) {
            loadingState.consumeEach { state ->
                when (state) {
                    LoadingSpinnerState.IS_LOADING -> view?.show()
                    LoadingSpinnerState.NOT_LOADING -> view?.hide()
                }
            }
        }
    }
}

interface LoadingSpinnerView : MVPView {
    fun show()

    fun hide()
}

class LoadingSpinner : ProgressBar, LoadingSpinnerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun show() {
        visibility = View.VISIBLE
    }

    override fun hide() {
        fadeOut()
    }
}
