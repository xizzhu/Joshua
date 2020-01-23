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

package me.xizzhu.android.joshua.infra.arch

import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import kotlinx.coroutines.*

interface ViewHolder

abstract class ViewPresenter<V : ViewHolder, I : Interactor>(protected val interactor: I, dispatcher: CoroutineDispatcher) {
    protected val tag: String = javaClass.simpleName
    protected val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

    protected var viewHolder: V? = null

    @UiThread
    fun create(viewHolder: V) {
        this.viewHolder = viewHolder
        onCreate(viewHolder)
    }

    @CallSuper
    @UiThread
    protected open fun onCreate(viewHolder: V) {
    }

    @UiThread
    fun start() {
        onStart()
    }

    @CallSuper
    @UiThread
    protected open fun onStart() {
    }

    @UiThread
    fun resume() {
        onResume()
    }

    @CallSuper
    @UiThread
    protected open fun onResume() {
    }

    @UiThread
    fun pause() {
        onPause()
    }

    @CallSuper
    @UiThread
    protected open fun onPause() {
    }

    @UiThread
    fun stop() {
        onStop()
    }

    @CallSuper
    @UiThread
    protected open fun onStop() {
    }

    @UiThread
    fun destroy() {
        onDestroy()
        viewHolder = null
        coroutineScope.cancel()
    }

    @CallSuper
    @UiThread
    protected open fun onDestroy() {
    }
}
