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

package me.xizzhu.android.joshua.settings

import android.app.Application
import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import java.io.InputStream
import java.io.OutputStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsViewModelTest : BaseUnitTest() {
    private lateinit var uri: Uri
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var resolver: ContentResolver
    private lateinit var backupManager: BackupManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var resources: Resources
    private lateinit var application: Application
    private lateinit var settingsViewModel: SettingsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        uri = mockk()
        inputStream = mockk<InputStream>().apply { every { close() } returns Unit }
        outputStream = mockk<OutputStream>().apply { every { close() } returns Unit }
        resolver = mockk<ContentResolver>().apply {
            every { openInputStream(uri) } returns inputStream
            every { openOutputStream(uri) } returns outputStream
        }
        backupManager = mockk<BackupManager>().apply {
            coEvery { backup(outputStream) } returns Unit
            coEvery { restore(inputStream) } returns Unit
        }
        settingsManager = mockk()
        every { settingsManager.settings() } returns emptyFlow()
        resources = mockk<Resources>().apply { every { getDimension(any()) } returns 12.0F }
        application = mockk<Application>().apply {
            every { contentResolver } returns resolver
            every { resources } returns this@SettingsViewModelTest.resources
        }

        settingsViewModel = SettingsViewModel(backupManager, settingsManager, application)
    }

    @Test
    fun `test settingsViewData`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        settingsViewModel = SettingsViewModel(backupManager, settingsManager, application)

        assertEquals(
                SettingsViewData(
                        currentFontSizeScale = 1.0F,
                        bodyTextSizeInPixel = 12.0F,
                        captionTextSizeInPixel = 12.0F,
                        keepScreenOn = true,
                        nightMode = SettingsViewData.NightMode.OFF,
                        simpleReadingModeOn = false,
                        hideSearchButton = false,
                        consolidateVersesForSharing = false,
                        defaultHighlightColor = SettingsViewData.HighlightColor.NONE,
                        version = ""
                ),
                (settingsViewModel.settingsViewData().take(1).first() as BaseViewModel.ViewData.Success).data
        )
    }

    @Test
    fun `test saveFontSizeScale`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(fontSizeScale = 7.0F)) } returns Unit

        with(settingsViewModel.saveFontSizeScale(Settings.DEFAULT.fontSizeScale).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(settingsViewModel.saveFontSizeScale(7.0F).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(fontSizeScale = 7.0F)) }
    }

    @Test
    fun `test saveKeepScreenOn`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(keepScreenOn = false)) } returns Unit

        with(settingsViewModel.saveKeepScreenOn(Settings.DEFAULT.keepScreenOn).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(settingsViewModel.saveKeepScreenOn(false).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(keepScreenOn = false)) }
    }

    @Test
    fun `test saveNightMode`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(nightMode = SettingsViewData.NightMode.ON.nightMode)) } returns Unit

        with(settingsViewModel.saveNightMode(SettingsViewData.NightMode.fromNightMode(Settings.DEFAULT.nightMode)).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(settingsViewModel.saveNightMode(SettingsViewData.NightMode.ON).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(nightMode = SettingsViewData.NightMode.ON.nightMode)) }
    }

    @Test
    fun `test saveSimpleReadingModeOn`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(simpleReadingModeOn = true)) } returns Unit

        with(settingsViewModel.saveSimpleReadingModeOn(Settings.DEFAULT.simpleReadingModeOn).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(settingsViewModel.saveSimpleReadingModeOn(true).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(simpleReadingModeOn = true)) }
    }

    @Test
    fun `test saveHideSearchButton`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(hideSearchButton = true)) } returns Unit

        with(settingsViewModel.saveHideSearchButton(Settings.DEFAULT.hideSearchButton).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(settingsViewModel.saveHideSearchButton(true).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(hideSearchButton = true)) }
    }

    @Test
    fun `test saveConsolidateVersesForSharing`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(consolidateVersesForSharing = true)) } returns Unit

        with(settingsViewModel.saveConsolidateVersesForSharing(Settings.DEFAULT.consolidateVersesForSharing).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(settingsViewModel.saveConsolidateVersesForSharing(true).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(consolidateVersesForSharing = true)) }
    }

    @Test
    fun `test saveDefaultHighlightColor`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_BLUE)) } returns Unit

        with(settingsViewModel.saveDefaultHighlightColor(SettingsViewData.HighlightColor.fromHighlightColor(Settings.DEFAULT.defaultHighlightColor)).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(settingsViewModel.saveDefaultHighlightColor(SettingsViewData.HighlightColor.BLUE).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_BLUE)) }
    }

    @Test
    fun `test backup with null uri`() = runTest {
        val actual = settingsViewModel.backup(null).toList()
        assertEquals(1, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test backup with error`() = runTest {
        val ex = RuntimeException("Random")
        every { application.contentResolver } throws ex

        val actual = settingsViewModel.backup(mockk()).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test backup with error from interactor`() = runTest {
        val ex = RuntimeException("Random")
        coEvery { backupManager.backup(outputStream) } throws ex

        val actual = settingsViewModel.backup(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test backup`() = runTest {
        val actual = settingsViewModel.backup(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(R.string.toast_backed_up, (actual[1] as BaseViewModel.ViewData.Success<Int>).data)
    }

    @Test
    fun `test restore with null uri`() = runTest {
        val actual = settingsViewModel.restore(null).toList()
        assertEquals(1, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test restore with error`() = runTest {
        val ex = RuntimeException("Random")
        every { application.contentResolver } throws ex

        val actual = settingsViewModel.restore(mockk()).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test restore with error from interactor`() = runTest {
        val ex = RuntimeException("Random")
        coEvery { backupManager.restore(inputStream) } throws ex

        val actual = settingsViewModel.restore(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test restore`() = runTest {
        val actual = settingsViewModel.restore(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(R.string.toast_restored, (actual[1] as BaseViewModel.ViewData.Success<Int>).data)
    }
}
