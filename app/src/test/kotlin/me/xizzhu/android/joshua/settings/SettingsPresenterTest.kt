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

package me.xizzhu.android.joshua.settings

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.App
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class SettingsPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var app: App
    @Mock
    private lateinit var settingsManager: SettingsManager
    @Mock
    private lateinit var settingsView: SettingsView

    private lateinit var currentSettings: BroadcastChannel<Settings>
    private lateinit var settingsPresenter: SettingsPresenter

    @Before
    override fun setup() {
        super.setup()

        currentSettings = ConflatedBroadcastChannel()
        `when`(settingsManager.observeSettings()).thenReturn(currentSettings.openSubscription())

        settingsPresenter = SettingsPresenter(app, settingsManager)
        settingsPresenter.attachView(settingsView)
    }

    @After
    override fun tearDown() {
        settingsPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testObserveSettings() {
        runBlocking {
            verify(settingsView, never()).onSettingsLoaded(any())

            currentSettings.send(Settings.DEFAULT)
            verify(settingsView, times(1)).onSettingsLoaded(Settings.DEFAULT)
        }
    }

    @Test
    fun testSaveSettingsWithException() {
        runBlocking {
            `when`(settingsManager.saveSettings(any())).thenThrow(RuntimeException("Random exception"))

            settingsPresenter.saveSettings(Settings.DEFAULT)
            verify(settingsView, times(1)).onSettingsUpdateFailed(Settings.DEFAULT)
        }
    }

    @Test
    fun testSetKeepScreenOn() {
        runBlocking {
            currentSettings.send(Settings.DEFAULT)

            settingsPresenter.setKeepScreenOn(true)
            verify(settingsManager, never()).saveSettings(any())

            settingsPresenter.setKeepScreenOn(false)
            verify(settingsManager, times(1))
                    .saveSettings(Settings.DEFAULT.toBuilder().keepScreenOn(false).build())
        }
    }

    @Test
    fun testSetNightModeOn() {
        runBlocking {
            currentSettings.send(Settings.DEFAULT)

            settingsPresenter.setNightModeOn(false)
            verify(settingsManager, never()).saveSettings(any())

            settingsPresenter.setNightModeOn(true)
            verify(settingsManager, times(1))
                    .saveSettings(Settings.DEFAULT.toBuilder().nightModeOn(true).build())
        }
    }
}
