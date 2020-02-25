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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope

interface ViewHolder

abstract class ViewPresenter<VH : ViewHolder, VM : ViewModel, A : AppCompatActivity>(
        protected val viewModel: VM, protected val activity: A, protected val coroutineScope: CoroutineScope
) : LifecycleObserver {
    protected val tag: String = javaClass.simpleName

    protected lateinit var viewHolder: VH

    @UiThread
    fun bind(viewHolder: VH) {
        this.viewHolder = viewHolder
        onBind()
        activity.lifecycle.addObserver(this)
    }

    @UiThread
    @CallSuper
    open fun onBind() {
    }
}
