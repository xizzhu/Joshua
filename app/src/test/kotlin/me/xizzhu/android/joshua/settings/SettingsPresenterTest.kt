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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SettingsPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsManager: SettingsManager
    @Mock
    private lateinit var backupManager: BackupManager
    @Mock
    private lateinit var settingsView: SettingsView

    private lateinit var currentSettings: BroadcastChannel<Settings>
    private lateinit var settingsPresenter: SettingsPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        currentSettings = ConflatedBroadcastChannel()
        `when`(settingsManager.observeSettings()).thenReturn(currentSettings.asFlow())

        settingsPresenter = SettingsPresenter(settingsManager, backupManager)
        settingsPresenter.attachView(settingsView)
    }

    @AfterTest
    override fun tearDown() {
        settingsPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testBackup() {
        runBlocking {
            val backup = "random backup string"
            `when`(backupManager.prepareForBackup()).thenReturn(backup)

            settingsPresenter.backup()
            with(inOrder(settingsView)) {
                verify(settingsView, times(1)).onBackupStarted()
                verify(settingsView, times(1)).onBackupReady(backup)
            }
            verify(settingsView, never()).onBackupFailed()
        }
    }

    @Test
    fun testBackupWithException() {
        runBlocking {
            `when`(backupManager.prepareForBackup()).thenThrow(RuntimeException("Random exception"))

            settingsPresenter.backup()
            with(inOrder(settingsView)) {
                verify(settingsView, times(1)).onBackupStarted()
                verify(settingsView, times(1)).onBackupFailed()
            }
            verify(settingsView, never()).onBackupReady(anyString())
        }
    }

    @Test
    fun testRestore() {
        runBlocking {
            val content = "random content for restore"
            settingsPresenter.restore(content)
            with(inOrder(settingsView, backupManager)) {
                verify(settingsView, times(1)).onRestoreStarted()
                verify(backupManager, times(1)).restore(content)
                verify(settingsView, times(1)).onRestored()
            }
            verify(settingsView, never()).onRestoreFailed()
        }
    }

    @Test
    fun testRestoreWithException() {
        runBlocking {
            val content = "random content for restore"
            `when`(backupManager.restore(content)).thenThrow(RuntimeException("Random exception"))

            settingsPresenter.restore(content)
            with(inOrder(settingsView, backupManager)) {
                verify(settingsView, times(1)).onRestoreStarted()
                verify(backupManager, times(1)).restore(content)
                verify(settingsView, times(1)).onRestoreFailed()
            }
            verify(settingsView, never()).onRestored()
        }
    }

    @Test
    fun testObserveSettings() {
        runBlocking {
            verify(settingsView, never()).onSettingsUpdated(any())

            currentSettings.send(Settings.DEFAULT)
            verify(settingsView, times(1)).onSettingsUpdated(Settings.DEFAULT)
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
    fun testSetFontSizeScale() {
        runBlocking {
            currentSettings.send(Settings.DEFAULT)

            settingsPresenter.setFontSizeScale(2)
            verify(settingsManager, never()).saveSettings(any())

            settingsPresenter.setFontSizeScale(4)
            verify(settingsManager, times(1))
                    .saveSettings(Settings.DEFAULT.copy(fontSizeScale = 4))
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
                    .saveSettings(Settings.DEFAULT.copy(keepScreenOn = false))
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
                    .saveSettings(Settings.DEFAULT.copy(nightModeOn = true))
        }
    }

    @Test
    fun testSetSimpleReadingModeOn() {
        runBlocking {
            currentSettings.send(Settings.DEFAULT)

            settingsPresenter.setSimpleReadingModeOn(false)
            verify(settingsManager, never()).saveSettings(any())

            settingsPresenter.setSimpleReadingModeOn(true)
            verify(settingsManager, times(1))
                    .saveSettings(Settings.DEFAULT.copy(simpleReadingModeOn = true))
        }
    }
}
