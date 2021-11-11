/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.infra

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.ui.getBackgroundColor
import me.xizzhu.android.logger.Log
import javax.inject.Inject

abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {
    protected val tag: String = javaClass.simpleName

    @Inject
    protected lateinit var navigator: Navigator

    protected lateinit var viewBinding: VB

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tag, "onCreate()")

        viewBinding = inflateViewBinding()
        setContentView(viewBinding.root)

        observeSettings()
    }

    protected abstract fun inflateViewBinding(): VB

    protected abstract fun viewModel(): VM

    private fun observeSettings() {
        viewModel().settings()
                .onEach { settings ->
                    with(window.decorView) {
                        keepScreenOn = settings.keepScreenOn
                        setBackgroundColor(settings.getBackgroundColor())
                    }
                }
                .launchIn(lifecycleScope)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        Log.i(tag, "onStart()")
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume()")
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        Log.i(tag, "onPause()")
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        Log.i(tag, "onStop()")
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(tag, "onSaveInstanceState()")
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy()")
    }

    @CallSuper
    override fun onLowMemory() {
        super.onLowMemory()
        Log.i(tag, "onLowMemory()")
    }

    @CallSuper
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.i(tag, "onTrimMemory(): level - $level")
    }
}
