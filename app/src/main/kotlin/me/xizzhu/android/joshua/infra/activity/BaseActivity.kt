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

package me.xizzhu.android.joshua.infra.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewModel
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.logger.Log

abstract class BaseActivity : AppCompatActivity() {
    protected val tag: String = javaClass.simpleName
    protected val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        Log.i(tag, "onCreate()")
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        Log.i(tag, "onStart()")
        getViewModel().start()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume()")
    }

    @CallSuper
    override fun onPause() {
        Log.i(tag, "onPause()")
        super.onPause()
    }

    @CallSuper
    override fun onStop() {
        Log.i(tag, "onStop()")
        getViewModel().stop()
        super.onStop()
    }

    @CallSuper
    override fun onDestroy() {
        Log.i(tag, "onDestroy()")
        getViewPresenters().forEach { it.unbind() }
        coroutineScope.cancel()
        super.onDestroy()
    }

    protected abstract fun getViewModel(): ViewModel

    protected abstract fun getViewPresenters(): List<ViewPresenter<out ViewHolder, out Interactor>>
}
