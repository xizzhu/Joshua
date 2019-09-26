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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.ui.getBackgroundColor

abstract class BaseSettingsAwareViewModel(private val settingsManager: SettingsManager, interactors: Set<Interactor>,
                                          dispatcher: CoroutineDispatcher) : ViewModel(interactors, dispatcher) {
    fun settings(): Flow<ViewData<Settings>> = settingsManager.observeSettings().map { ViewData.success(it) }
}

abstract class BaseSettingsAwareActivity : BaseActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coroutineScope.launch {
            getBaseSettingsAwareViewModel().settings().collect { settings ->
                with(window.decorView) {
                    keepScreenOn = settings.data.keepScreenOn
                    setBackgroundColor(settings.data.getBackgroundColor())
                }
            }
        }
    }

    override fun getViewModel(): ViewModel = getBaseSettingsAwareViewModel()

    abstract fun getBaseSettingsAwareViewModel(): BaseSettingsAwareViewModel
}

abstract class BaseSettingsAwareInteractor(protected val settingsManager: SettingsManager,
                                           dispatcher: CoroutineDispatcher) : Interactor(dispatcher) {
    fun settings(): Flow<ViewData<Settings>> = settingsManager.observeSettings().map { ViewData.success(it) }
}

abstract class BaseSettingsAwarePresenter<V : ViewHolder, I : BaseSettingsAwareInteractor>(
        interactor: I, dispatcher: CoroutineDispatcher) : ViewPresenter<V, I>(interactor, dispatcher)
