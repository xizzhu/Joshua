/*
 * Copyright (C) 2023 Xizhi Zhu
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.logger.Log
import javax.inject.Inject
import kotlinx.coroutines.launch

abstract class BaseFragment<VB : ViewBinding, ViewAction, ViewState, VM : BaseViewModelV2<ViewAction, ViewState>> : Fragment() {
    protected val loggingTag: String = javaClass.simpleName

    @Inject
    protected lateinit var navigator: Navigator

    protected abstract val viewModel: VM

    protected abstract val viewBinding: VB

    protected abstract fun initializeView()

    protected abstract fun onViewActionEmitted(viewAction: ViewAction)

    protected abstract fun onViewStateUpdated(viewState: ViewState)

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(loggingTag, "onCreate()")
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i(loggingTag, "onCreateView()")
        return viewBinding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(loggingTag, "onViewCreated()")

        initializeView()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewAction().collect(::onViewActionEmitted)
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState().collect(::onViewStateUpdated)
            }
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        Log.i(loggingTag, "onStart()")
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        Log.i(loggingTag, "onResume()")
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        Log.i(loggingTag, "onPause()")
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        Log.i(loggingTag, "onStop()")
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(loggingTag, "onSaveInstanceState()")
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        Log.i(loggingTag, "onDestroy()")
    }
}
