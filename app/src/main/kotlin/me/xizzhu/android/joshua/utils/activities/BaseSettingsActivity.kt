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

package me.xizzhu.android.joshua.utils.activities

import android.os.Bundle
import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.ui.getBackgroundColor
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.MVPView

abstract class BaseSettingsActivity : BaseActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coroutineScope.launch(Dispatchers.Main) {
            getBaseSettingsInteractor().observeSettings().consumeEach { settings ->
                with(window.decorView) {
                    keepScreenOn = settings.keepScreenOn
                    setBackgroundColor(settings.getBackgroundColor())
                }
            }
        }
    }

    protected abstract fun getBaseSettingsInteractor(): BaseSettingsInteractor
}

abstract class BaseSettingsInteractor(private val settingsManager: SettingsManager) {
    suspend fun observeSettings(): ReceiveChannel<Settings> = settingsManager.observeSettings()
}

interface BaseSettingsView : MVPView {
    fun onSettingsUpdated(settings: Settings)
}

abstract class BaseSettingsPresenter<V : BaseSettingsView>(private val baseSettingsInteractor: BaseSettingsInteractor)
    : MVPPresenter<V>() {
    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            baseSettingsInteractor.observeSettings().consumeEach { view?.onSettingsUpdated(it) }
        }
    }
}
