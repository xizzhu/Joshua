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

package me.xizzhu.android.joshua.progress

import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwareViewModel
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerInteractor

class ReadingProgressViewModel(settingsManager: SettingsManager,
                               private val loadingSpinnerInteractor: LoadingSpinnerInteractor,
                               private val readingProgressInteractor: ReadingProgressInteractor,
                               dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAwareViewModel(settingsManager, listOf(loadingSpinnerInteractor, readingProgressInteractor), dispatcher) {
    @UiThread
    override fun onCreate() {
        super.onCreate()

        readingProgressInteractor.loadingState().onEach { loadingSpinnerInteractor.updateLoadingState(it) }.launchIn(coroutineScope)
    }
}
