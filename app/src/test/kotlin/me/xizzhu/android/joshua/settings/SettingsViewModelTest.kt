/*
 * Copyright (C) 2022 Xizhi Zhu
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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.ui.getPrimaryTextSize
import me.xizzhu.android.joshua.ui.getSecondaryTextSize
import me.xizzhu.android.joshua.utils.application
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest : BaseUnitTest() {
    private lateinit var uri: Uri
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var resolver: ContentResolver
    private lateinit var backupManager: BackupManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var resources: Resources
    private lateinit var app: Application
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
        resources = mockk<Resources>().apply {
            every { getDimension(R.dimen.text_primary) } returns 18.0F
            every { getDimension(R.dimen.text_secondary) } returns 16.0F
        }
        app = mockk<Application>().apply {
            every { application } returns this
            every { contentResolver } returns resolver
            every { resources } returns this@SettingsViewModelTest.resources
        }

        settingsViewModel = SettingsViewModel(backupManager, settingsManager, app, testCoroutineDispatcherProvider)
    }

    @Test
    fun `test viewState(), from constructor`() = runTest {
        val packageInfo = PackageInfo().apply { versionName = "version_name" }
        val packageManager = mockk<PackageManager>().apply {
            every { getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
            every { getPackageInfo(any<String>(), any<PackageManager.PackageInfoFlags>()) } returns packageInfo
        }
        every { app.packageManager } returns packageManager
        every { app.packageName } returns "package_name"

        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        settingsViewModel = SettingsViewModel(backupManager, settingsManager, app, testCoroutineDispatcherProvider)

        assertEquals(createDefaultViewState().copy(version = "version_name"), settingsViewModel.viewState().first())
    }

    private fun createDefaultViewState(): SettingsViewModel.ViewState = SettingsViewModel.ViewState(
        fontSizeScale = Settings.DEFAULT.fontSizeScale,
        bodyTextSizePx = Settings.DEFAULT.getPrimaryTextSize(app.resources).roundToInt(),
        captionTextSizePx = Settings.DEFAULT.getSecondaryTextSize(app.resources).roundToInt(),
        keepScreenOn = Settings.DEFAULT.keepScreenOn,
        nightModeStringRes = R.string.settings_text_night_mode_off,
        simpleReadingModeOn = Settings.DEFAULT.simpleReadingModeOn,
        hideSearchButton = Settings.DEFAULT.hideSearchButton,
        consolidateVersesForSharing = Settings.DEFAULT.consolidateVersesForSharing,
        defaultHighlightColorStringRes = R.string.text_highlight_color_none,
        backupState = SettingsViewModel.ViewState.BackupRestoreState.Idle,
        restoreState = SettingsViewModel.ViewState.BackupRestoreState.Idle,
        version = "",
        fontSizeScaleSelection = null,
        nightModeSelection = null,
        highlightColorSelection = null,
        error = null,
    )

    @Test
    fun `test saveFontSizeScale()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(fontSizeScale = 2.0F)) } returns Unit

        settingsViewModel.saveFontSizeScale(1.0F)
        settingsViewModel.saveFontSizeScale(2.0F)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(fontSizeScale = 2.0F)) }
        assertEquals(createDefaultViewState().copy(fontSizeScale = 2.0F), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(fontSizeScale = 3.0F)) } throws RuntimeException("random exception")

        settingsViewModel.saveFontSizeScale(3.0F)

        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.SettingsUpdatingError),
            settingsViewModel.viewState().first()
        )
    }

    @Test
    fun `test saveKeepScreenOn()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(keepScreenOn = false)) } returns Unit

        settingsViewModel.saveKeepScreenOn(keepScreenOn = true)
        settingsViewModel.saveKeepScreenOn(keepScreenOn = false)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(keepScreenOn = false)) }
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(keepScreenOn = false)) } throws RuntimeException("random exception")

        settingsViewModel.saveKeepScreenOn(keepScreenOn = false)

        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.SettingsUpdatingError),
            settingsViewModel.viewState().first()
        )
    }

    @Test
    fun `test saveNightMode()`() = runTest {
        mockkStatic(AppCompatDelegate::class)
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(nightMode = Settings.NIGHT_MODE_ON)) } returns Unit

        settingsViewModel.saveNightMode(nightMode = Settings.NIGHT_MODE_OFF)
        settingsViewModel.saveNightMode(nightMode = Settings.NIGHT_MODE_ON)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(nightMode = Settings.NIGHT_MODE_ON)) }
        verify(exactly = 1) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(nightMode = Settings.NIGHT_MODE_FOLLOW_SYSTEM)) } returns Unit
        settingsViewModel.saveNightMode(nightMode = Settings.NIGHT_MODE_FOLLOW_SYSTEM)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(nightMode = Settings.NIGHT_MODE_FOLLOW_SYSTEM)) }
        verify(exactly = 1) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(nightMode = -1)) } returns Unit
        settingsViewModel.saveNightMode(nightMode = -1)

        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.SettingsUpdatingError),
            settingsViewModel.viewState().first()
        )
    }

    @Test
    fun `test saveSimpleReadingModeOn()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(simpleReadingModeOn = true)) } returns Unit

        settingsViewModel.saveSimpleReadingModeOn(simpleReadingModeOn = false)
        settingsViewModel.saveSimpleReadingModeOn(simpleReadingModeOn = true)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(simpleReadingModeOn = true)) }
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(simpleReadingModeOn = true)) } throws RuntimeException("random exception")

        settingsViewModel.saveSimpleReadingModeOn(simpleReadingModeOn = true)

        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.SettingsUpdatingError),
            settingsViewModel.viewState().first()
        )
    }

    @Test
    fun `test saveHideSearchButton()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(hideSearchButton = true)) } returns Unit

        settingsViewModel.saveHideSearchButton(hideSearchButton = false)
        settingsViewModel.saveHideSearchButton(hideSearchButton = true)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(hideSearchButton = true)) }
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(hideSearchButton = true)) } throws RuntimeException("random exception")

        settingsViewModel.saveHideSearchButton(hideSearchButton = true)

        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.SettingsUpdatingError),
            settingsViewModel.viewState().first()
        )
    }

    @Test
    fun `test saveConsolidateVersesForSharing()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(consolidateVersesForSharing = true)) } returns Unit

        settingsViewModel.saveConsolidateVersesForSharing(consolidateVersesForSharing = false)
        settingsViewModel.saveConsolidateVersesForSharing(consolidateVersesForSharing = true)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(consolidateVersesForSharing = true)) }
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(consolidateVersesForSharing = true)) } throws RuntimeException("random exception")

        settingsViewModel.saveConsolidateVersesForSharing(consolidateVersesForSharing = true)

        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.SettingsUpdatingError),
            settingsViewModel.viewState().first()
        )
    }

    @Test
    fun `test saveDefaultHighlightColor()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_BLUE)) } returns Unit

        settingsViewModel.saveDefaultHighlightColor(defaultHighlightColor = Highlight.COLOR_NONE)
        settingsViewModel.saveDefaultHighlightColor(defaultHighlightColor = Highlight.COLOR_BLUE)

        coVerify(exactly = 1) { settingsManager.saveSettings(Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_BLUE)) }
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())

        coEvery { settingsManager.saveSettings(Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_BLUE)) } throws RuntimeException("random exception")

        settingsViewModel.saveDefaultHighlightColor(defaultHighlightColor = Highlight.COLOR_BLUE)

        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.SettingsUpdatingError),
            settingsViewModel.viewState().first()
        )
    }

    @Test
    fun `test selectFontSizeScale()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        settingsViewModel.selectFontSizeScale()
        assertEquals(
            createDefaultViewState().copy(
                fontSizeScaleSelection = SettingsViewModel.ViewState.FontSizeScaleSelection(
                    currentBodyTextSizePx = 18,
                    currentCaptionTextSizePx = 16,
                    currentScale = 1.0F,
                    minScale = 0.5F,
                    maxScale = 3.0F,
                )
            ),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markFontSizeSelectionAsDismissed()
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test selectNightMode()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        settingsViewModel.selectNightMode()
        assertEquals(
            createDefaultViewState().copy(
                nightModeSelection = SettingsViewModel.ViewState.NightModeSelection(
                    currentPosition = 1,
                    availableModes = listOf(
                        SettingsViewModel.ViewState.NightModeSelection.NightMode(Settings.NIGHT_MODE_ON, R.string.settings_text_night_mode_on),
                        SettingsViewModel.ViewState.NightModeSelection.NightMode(Settings.NIGHT_MODE_OFF, R.string.settings_text_night_mode_off),
                        SettingsViewModel.ViewState.NightModeSelection.NightMode(Settings.NIGHT_MODE_FOLLOW_SYSTEM, R.string.settings_text_night_mode_system),
                    ),
                )
            ),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markNightModeSelectionAsDismissed()
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test selectHighlightColor()`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        settingsViewModel.selectHighlightColor()
        assertEquals(
            createDefaultViewState().copy(
                highlightColorSelection = SettingsViewModel.ViewState.HighlightColorSelection(
                    currentPosition = 0,
                    availableColors = listOf(
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_NONE, R.string.text_highlight_color_none),
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_YELLOW, R.string.text_highlight_color_yellow),
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_PINK, R.string.text_highlight_color_pink),
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_ORANGE, R.string.text_highlight_color_orange),
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_PURPLE, R.string.text_highlight_color_purple),
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_RED, R.string.text_highlight_color_red),
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_GREEN, R.string.text_highlight_color_green),
                        SettingsViewModel.ViewState.HighlightColorSelection.HighlightColor(Highlight.COLOR_BLUE, R.string.text_highlight_color_blue),
                    ),
                )
            ),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markHighlightColorSelectionAsDismissed()
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test backup()`() = runTest {
        val viewAction = async(Dispatchers.Unconfined) { settingsViewModel.viewAction().first() }

        settingsViewModel.backup()

        assertEquals(SettingsViewModel.ViewAction.RequestUriForBackup, viewAction.await())
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test backup(), with null uri`() = runTest {
        settingsViewModel.backup(null)
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.BackupError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.BackupError)
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test backup(), with exception`() = runTest {
        every { app.contentResolver } throws RuntimeException("random exception")

        settingsViewModel.backup(Uri.EMPTY)
        assertEquals(
            createDefaultViewState().copy(
                backupState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = false),
                error = SettingsViewModel.ViewState.Error.BackupError
            ),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markBackupStateAsIdle()
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.BackupError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.RestoreError)
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.BackupError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.BackupError)
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test backup(), with openOutputStream returning null`() = runTest {
        every { resolver.openOutputStream(Uri.EMPTY) } returns null

        settingsViewModel.backup(Uri.EMPTY)
        assertEquals(
            createDefaultViewState().copy(
                backupState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = false),
                error = SettingsViewModel.ViewState.Error.BackupError
            ),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markBackupStateAsIdle()
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.BackupError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.BackupError)
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test backup(), success`() = runTest {
        coEvery { backupManager.backup(outputStream) } returns Unit

        settingsViewModel.backup(uri)
        assertEquals(
            createDefaultViewState().copy(backupState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = true)),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markBackupStateAsIdle()
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test restore()`() = runTest {
        val viewAction = async(Dispatchers.Unconfined) { settingsViewModel.viewAction().first() }

        settingsViewModel.restore()
        assertEquals(SettingsViewModel.ViewAction.RequestUriForRestore, viewAction.await())
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test restore(), with null uri`() = runTest {
        settingsViewModel.restore(null)
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.RestoreError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.RestoreError)
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test restore(), with exception`() = runTest {
        every { app.contentResolver } throws RuntimeException("random exception")

        settingsViewModel.restore(Uri.EMPTY)
        assertEquals(
            createDefaultViewState().copy(
                restoreState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = false),
                error = SettingsViewModel.ViewState.Error.RestoreError
            ),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markRestoreStateAsIdle()
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.RestoreError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.BackupError)
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.RestoreError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.RestoreError)
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test restore(), with openInputStream returning null`() = runTest {
        every { resolver.openInputStream(Uri.EMPTY) } returns null

        settingsViewModel.restore(Uri.EMPTY)
        assertEquals(
            createDefaultViewState().copy(
                restoreState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = false),
                error = SettingsViewModel.ViewState.Error.RestoreError
            ),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markRestoreStateAsIdle()
        assertEquals(
            createDefaultViewState().copy(error = SettingsViewModel.ViewState.Error.RestoreError),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markErrorAsShown(SettingsViewModel.ViewState.Error.RestoreError)
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test restore(), success`() = runTest {
        coEvery { backupManager.restore(inputStream) } returns Unit

        settingsViewModel.restore(uri)
        assertEquals(
            createDefaultViewState().copy(restoreState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = true)),
            settingsViewModel.viewState().first()
        )

        settingsViewModel.markRestoreStateAsIdle()
        assertEquals(createDefaultViewState(), settingsViewModel.viewState().first())
    }

    @Test
    fun `test openRateMe()`() = runTest {
        val viewAction = async(Dispatchers.Unconfined) { settingsViewModel.viewAction().first() }

        settingsViewModel.openRateMe()
        assertEquals(SettingsViewModel.ViewAction.OpenRateMe, viewAction.await())
    }

    @Test
    fun `test openWebsite()`() = runTest {
        val viewAction = async(Dispatchers.Unconfined) { settingsViewModel.viewAction().first() }

        settingsViewModel.openWebsite()
        assertEquals(SettingsViewModel.ViewAction.OpenWebsite, viewAction.await())
    }
}
