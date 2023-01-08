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

package me.xizzhu.android.joshua.settings

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.Ordering
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.inject.Singleton
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.NavigationModule
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.databinding.ActivitySettingsBinding
import me.xizzhu.android.joshua.databinding.InnerSettingButtonBinding
import me.xizzhu.android.joshua.settings.widgets.SettingButton
import me.xizzhu.android.joshua.tests.BaseActivityTest
import me.xizzhu.android.joshua.tests.getProperty
import me.xizzhu.android.joshua.tests.performClickPressed
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@UninstallModules(NavigationModule::class)
class SettingsActivityTest : BaseActivityTest() {
    @Module
    @InstallIn(SingletonComponent::class)
    object MockNavigationModule {
        val mockNavigator: Navigator = mockk()

        @Provides
        @Singleton
        fun provideNavigator(): Navigator = mockNavigator
    }

    @BindValue
    val viewModel: SettingsViewModel = mockk(relaxed = true)

    @BeforeTest
    override fun setup() {
        super.setup()
        clearMocks(MockNavigationModule.mockNavigator)
    }

    @Test
    fun `test initializeView()`() {
        withActivity<SettingsActivity> { activity ->
            val viewBinding: ActivitySettingsBinding = activity.getProperty("viewBinding")
            viewBinding.keepScreenOn.performClickPressed()
            viewBinding.simpleReadingMode.performClickPressed()
            viewBinding.hideSearchButton.performClickPressed()
            viewBinding.consolidatedSharing.performClickPressed()
            viewBinding.fontSize.performClick()
            viewBinding.nightMode.performClick()
            viewBinding.defaultHighlightColor.performClick()
            viewBinding.backup.performClick()
            viewBinding.restore.performClick()
            viewBinding.rate.performClick()
            viewBinding.website.performClick()

            verify(ordering = Ordering.ORDERED) {
                viewModel.saveKeepScreenOn(true)
                viewModel.saveSimpleReadingModeOn(true)
                viewModel.saveHideSearchButton(true)
                viewModel.saveConsolidateVersesForSharing(true)
                viewModel.selectFontSizeScale()
                viewModel.selectNightMode()
                viewModel.selectHighlightColor()
                viewModel.backup()
                viewModel.restore()
                viewModel.openRateMe()
                viewModel.openWebsite()
            }
        }
    }

    @Test
    fun `test onViewActionEmitted(), OpenRateMe`() {
        every { viewModel.viewAction() } returns flowOf(SettingsViewModel.ViewAction.OpenRateMe)
        every { MockNavigationModule.mockNavigator.navigate(any(), Navigator.SCREEN_RATE_ME) } returns Unit

        withActivity<SettingsActivity> { activity ->
            verify(exactly = 1) { MockNavigationModule.mockNavigator.navigate(activity, Navigator.SCREEN_RATE_ME) }
        }
    }

    @Test
    fun `test onViewActionEmitted(), OpenWebsite`() {
        every { viewModel.viewAction() } returns flowOf(SettingsViewModel.ViewAction.OpenWebsite)
        every { MockNavigationModule.mockNavigator.navigate(any(), Navigator.SCREEN_WEBSITE) } returns Unit

        withActivity<SettingsActivity> { activity ->
            verify(exactly = 1) { MockNavigationModule.mockNavigator.navigate(activity, Navigator.SCREEN_WEBSITE) }
        }
    }

    @Test
    fun `test onViewActionEmitted(), RequestUriForBackup`() {
        every { viewModel.viewAction() } returns flowOf(SettingsViewModel.ViewAction.RequestUriForBackup)

        withActivity<SettingsActivity> {
            with(Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity) {
                assertEquals(Intent.ACTION_CREATE_DOCUMENT, action)
                assertEquals("application/json", type)
                assertEquals(setOf(Intent.CATEGORY_OPENABLE), categories)
            }
        }
    }

    @Test
    fun `test onViewActionEmitted(), RequestUriForRestore`() {
        every { viewModel.viewAction() } returns flowOf(SettingsViewModel.ViewAction.RequestUriForRestore)

        withActivity<SettingsActivity> {
            with(Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity) {
                assertEquals(Intent.ACTION_CHOOSER, action)

                val intent = getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                assertEquals(Intent.ACTION_GET_CONTENT, intent?.action)
                assertEquals("*/*", intent?.type)
                assertEquals(setOf(Intent.CATEGORY_OPENABLE), intent?.categories)
            }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with completed backup state, null font size selection, null night mode selection, null highlight color selection, and null error`() {
        every { viewModel.viewState() } returns flowOf(SettingsViewModel.ViewState(
            fontSizeScale = 1.0F,
            bodyTextSizePx = 2,
            captionTextSizePx = 3,
            keepScreenOn = true,
            nightModeStringRes = R.string.settings_text_night_mode_off,
            simpleReadingModeOn = true,
            hideSearchButton = true,
            consolidateVersesForSharing = true,
            defaultHighlightColorStringRes = R.string.text_highlight_color_none,
            backupState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = true),
            restoreState = SettingsViewModel.ViewState.BackupRestoreState.Idle,
            version = "app_version",
            fontSizeScaleSelection = null,
            nightModeSelection = null,
            highlightColorSelection = null,
            error = null,
        ))

        withActivity<SettingsActivity> { activity ->
            val viewBinding: ActivitySettingsBinding = activity.getProperty("viewBinding")
            assertEquals("1.0x", viewBinding.fontSize.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            assertTrue(viewBinding.keepScreenOn.isChecked)
            assertTrue(viewBinding.simpleReadingMode.isChecked)
            assertTrue(viewBinding.hideSearchButton.isChecked)
            assertTrue(viewBinding.consolidatedSharing.isChecked)
            assertEquals("Off", viewBinding.nightMode.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            assertEquals("None", viewBinding.defaultHighlightColor.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            assertEquals("app_version", viewBinding.version.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            verify(exactly = 1) { viewModel.markBackupStateAsIdle() }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with completed restore state, null font size selection, null night mode selection, null highlight color selection, and null error`() {
        every { viewModel.viewState() } returns flowOf(SettingsViewModel.ViewState(
            fontSizeScale = 1.0F,
            bodyTextSizePx = 2,
            captionTextSizePx = 3,
            keepScreenOn = true,
            nightModeStringRes = R.string.settings_text_night_mode_off,
            simpleReadingModeOn = true,
            hideSearchButton = true,
            consolidateVersesForSharing = true,
            defaultHighlightColorStringRes = R.string.text_highlight_color_none,
            backupState = SettingsViewModel.ViewState.BackupRestoreState.Idle,
            restoreState = SettingsViewModel.ViewState.BackupRestoreState.Completed(successful = false),
            version = "app_version",
            fontSizeScaleSelection = null,
            nightModeSelection = null,
            highlightColorSelection = null,
            error = null,
        ))

        withActivity<SettingsActivity> { activity ->
            val viewBinding: ActivitySettingsBinding = activity.getProperty("viewBinding")
            assertEquals("1.0x", viewBinding.fontSize.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            assertTrue(viewBinding.keepScreenOn.isChecked)
            assertTrue(viewBinding.simpleReadingMode.isChecked)
            assertTrue(viewBinding.hideSearchButton.isChecked)
            assertTrue(viewBinding.consolidatedSharing.isChecked)
            assertEquals("Off", viewBinding.nightMode.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            assertEquals("None", viewBinding.defaultHighlightColor.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            assertEquals("app_version", viewBinding.version.getProperty<SettingButton, InnerSettingButtonBinding>("viewBinding").description.text)
            verify(exactly = 1) { viewModel.markRestoreStateAsIdle() }
        }
    }
}
