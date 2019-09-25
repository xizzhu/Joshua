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

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class SettingsInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsManager: SettingsManager
    @Mock
    private lateinit var backupManager: BackupManager

    private lateinit var settingsInteractor: SettingsInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(settingsManager.observeSettings()).thenReturn(flowOf(Settings.DEFAULT))
        settingsInteractor = SettingsInteractor(settingsManager, backupManager, testDispatcher)
    }

    @Test
    fun testSaveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true)
        settingsInteractor.saveSettings(settings)
        verify(settingsManager, times(1)).saveSettings(settings)
    }

    @Test
    fun testSaveSettingsWithException() = testDispatcher.runBlockingTest {
        `when`(settingsManager.saveSettings(any())).thenThrow(RuntimeException("Random exception"))
        settingsInteractor.saveSettings(Settings.DEFAULT)
    }

    @Test
    fun testSetFontSizeScale() = testDispatcher.runBlockingTest {
        settingsInteractor.setFontSizeScale(Settings.DEFAULT.fontSizeScale)
        verify(settingsManager, never()).saveSettings(any())

        settingsInteractor.setFontSizeScale(4)
        verify(settingsManager, times(1)).saveSettings(Settings.DEFAULT.copy(fontSizeScale = 4))
    }

    @Test
    fun testSetKeepScreenOn() = testDispatcher.runBlockingTest {
        settingsInteractor.setKeepScreenOn(Settings.DEFAULT.keepScreenOn)
        verify(settingsManager, never()).saveSettings(any())

        settingsInteractor.setKeepScreenOn(false)
        verify(settingsManager, times(1)).saveSettings(Settings.DEFAULT.copy(keepScreenOn = false))
    }

    @Test
    fun testSetNightModeOn() = testDispatcher.runBlockingTest {
        settingsInteractor.setNightModeOn(Settings.DEFAULT.nightModeOn)
        verify(settingsManager, never()).saveSettings(any())

        settingsInteractor.setNightModeOn(true)
        verify(settingsManager, times(1)).saveSettings(Settings.DEFAULT.copy(nightModeOn = true))
    }

    @Test
    fun testSetSimpleReadingModeOn() = testDispatcher.runBlockingTest {
        settingsInteractor.setSimpleReadingModeOn(Settings.DEFAULT.simpleReadingModeOn)
        verify(settingsManager, never()).saveSettings(any())

        settingsInteractor.setSimpleReadingModeOn(true)
        verify(settingsManager, times(1)).saveSettings(Settings.DEFAULT.copy(simpleReadingModeOn = true))
    }

    @Test
    fun testPrepareBackupData() = testDispatcher.runBlockingTest {
        settingsInteractor.prepareBackupData()
        verify(backupManager, times(1)).prepareForBackup()
    }

    @Test
    fun testPrepareBackupDataWithException() = testDispatcher.runBlockingTest {
        `when`(backupManager.prepareForBackup()).thenThrow(RuntimeException("Random exception"))
        settingsInteractor.prepareBackupData()
    }

    @Test
    fun testRestore() = testDispatcher.runBlockingTest {
        val data = "random data"
        settingsInteractor.restore(data)
        verify(backupManager, times(1)).restore(data)
    }

    @Test
    fun testRestoreWithException() = testDispatcher.runBlockingTest {
        `when`(backupManager.restore(anyString())).thenThrow(RuntimeException("Random exception"))
        settingsInteractor.restore("")
    }
}
