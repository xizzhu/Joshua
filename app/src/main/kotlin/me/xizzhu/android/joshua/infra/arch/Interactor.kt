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

abstract class Interactor(dispatcher: CoroutineDispatcher) {
    protected val tag: String = javaClass.simpleName
    protected val coroutineScope: CoroutineScope = CoroutineScope(Job() + dispatcher)

    @UiThread
    fun create() {
        onCreate()
    }

    @CallSuper
    @UiThread
    protected open fun onCreate() {
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
        coroutineScope.cancel()
    }

    @CallSuper
    @UiThread
    protected open fun onDestroy() {
    }
}
