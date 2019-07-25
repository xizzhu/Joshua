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

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class BaseSettingsPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var baseSettingsInteractor: BaseSettingsInteractor
    @Mock
    private lateinit var baseSettingsView: BaseSettingsView

    @Test
    fun testObserveSettingsEmpty() {
        runBlocking {
            `when`(baseSettingsInteractor.observeSettings()).thenReturn(emptyFlow())

            val baseSettingsPresenter = object : BaseSettingsPresenter<BaseSettingsView>(baseSettingsInteractor) {}
            baseSettingsPresenter.attachView(baseSettingsView)
            baseSettingsPresenter.detachView()

            verify(baseSettingsView, never()).onSettingsUpdated(any())
        }
    }

    @Test
    fun testObserveSettings() {
        runBlocking {
            `when`(baseSettingsInteractor.observeSettings()).thenReturn(flowOf(Settings.DEFAULT))

            val baseSettingsPresenter = object : BaseSettingsPresenter<BaseSettingsView>(baseSettingsInteractor) {}
            baseSettingsPresenter.attachView(baseSettingsView)
            baseSettingsPresenter.detachView()

            verify(baseSettingsView, times(1)).onSettingsUpdated(Settings.DEFAULT)
        }
    }
}
