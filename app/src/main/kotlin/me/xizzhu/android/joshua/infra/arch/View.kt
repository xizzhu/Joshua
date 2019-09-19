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

package me.xizzhu.android.joshua.infra.arch

import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import kotlinx.coroutines.*

interface ViewHolder

abstract class Interactor {
    protected val tag: String = javaClass.simpleName
    protected val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    @UiThread
    fun start() {
        onStarted()
    }

    @CallSuper
    @UiThread
    protected open fun onStarted() {
    }

    @UiThread
    fun stop() {
        onStopped()
        coroutineScope.coroutineContext[Job]?.cancelChildren()
    }

    @CallSuper
    @UiThread
    protected open fun onStopped() {
    }
}

abstract class ViewPresenter<V : ViewHolder, I : Interactor>(protected val interactor: I) {
    protected val tag: String = javaClass.simpleName
    protected lateinit var coroutineScope: CoroutineScope

    protected var viewHolder: V? = null

    @UiThread
    fun bind(viewHolder: V) {
        coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        this.viewHolder = viewHolder
        onBind(viewHolder)
    }

    @CallSuper
    @UiThread
    protected open fun onBind(viewHolder: V) {
    }

    @UiThread
    fun unbind() {
        onUnbind()
        coroutineScope.cancel()
        this.viewHolder = null
    }

    @CallSuper
    @UiThread
    protected open fun onUnbind() {
    }
}
