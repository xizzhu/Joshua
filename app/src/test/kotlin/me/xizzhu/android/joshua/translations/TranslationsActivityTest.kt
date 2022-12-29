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

package me.xizzhu.android.joshua.translations

import androidx.core.view.isVisible
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.inject.Singleton
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import me.xizzhu.android.joshua.NavigationModule
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.databinding.ActivityTranslationsBinding
import me.xizzhu.android.joshua.tests.BaseActivityTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.getProperty
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@UninstallModules(NavigationModule::class)
class TranslationsActivityTest : BaseActivityTest() {
    @Module
    @InstallIn(SingletonComponent::class)
    object MockNavigationModule {
        val mockNavigator: Navigator = mockk()

        @Provides
        @Singleton
        fun provideNavigator(): Navigator = mockNavigator
    }

    @BindValue
    val viewModel: TranslationsViewModel = mockk(relaxed = true)

    @BeforeTest
    override fun setup() {
        super.setup()
        clearMocks(MockNavigationModule.mockNavigator)
    }

    @Test
    fun `test adapter viewEvent, SelectTranslation`() {
        withActivity(TranslationsActivity::class.java) { activity ->
            val adapter: TranslationsAdapter = activity.getProperty("adapter")
            val onViewEvent: (TranslationsAdapter.ViewEvent) -> Unit = adapter.getProperty("onViewEvent")
            onViewEvent(TranslationsAdapter.ViewEvent.SelectTranslation(MockContents.kjvDownloadedTranslationInfo))

            verify(exactly = 1) { viewModel.selectTranslation(MockContents.kjvDownloadedTranslationInfo) }
        }
    }

    @Test
    fun `test onViewActionEmitted(), GoBack`() {
        every { viewModel.viewAction() } returns flowOf(TranslationsViewModel.ViewAction.GoBack)
        every { MockNavigationModule.mockNavigator.goBack(any()) } returns Unit

        withActivity(TranslationsActivity::class.java) {
            verify(exactly = 1) { MockNavigationModule.mockNavigator.goBack(any()) }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null error`() {
        every { viewModel.viewState() } returns flowOf(TranslationsViewModel.ViewState(
            loading = true,
            items = emptyList(),
            translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
            translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
            error = null,
        ))

        withActivity(TranslationsActivity::class.java) { activity ->
            val viewBinding: ActivityTranslationsBinding = activity.getProperty("viewBinding")
            assertTrue(viewBinding.swipeRefresher.isRefreshing)
            assertFalse(viewBinding.translationList.isVisible)
            assertEquals(0, viewBinding.translationList.adapter!!.itemCount)
        }
    }

    @Test
    fun `test onViewStateUpdated(), with completed downloading state, and TranslationDownloadingError error`() {
        every { viewModel.viewState() } returns flowOf(TranslationsViewModel.ViewState(
            loading = false,
            items = listOf(
                TranslationsItem.Header(Settings.DEFAULT, "title", false)
            ),
            translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Completed(successful = false),
            translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Idle,
            error = TranslationsViewModel.ViewState.Error.TranslationDownloadingError(MockContents.kjvTranslationInfo),
        ))

        withActivity(TranslationsActivity::class.java) { activity ->
            val viewBinding: ActivityTranslationsBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.swipeRefresher.isRefreshing)
            assertTrue(viewBinding.translationList.isVisible)
            assertEquals(1, viewBinding.translationList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content

            verify(exactly = 1) { viewModel.markTranslationDownloadingStateAsIdle() }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with completed removal state, and TranslationRemovalError error`() {
        every { viewModel.viewState() } returns flowOf(TranslationsViewModel.ViewState(
            loading = false,
            items = listOf(
                TranslationsItem.Header(Settings.DEFAULT, "title", false)
            ),
            translationDownloadingState = TranslationsViewModel.ViewState.TranslationDownloadingState.Idle,
            translationRemovalState = TranslationsViewModel.ViewState.TranslationRemovalState.Completed(successful = false),
            error = TranslationsViewModel.ViewState.Error.TranslationRemovalError(MockContents.kjvDownloadedTranslationInfo),
        ))

        withActivity(TranslationsActivity::class.java) { activity ->
            val viewBinding: ActivityTranslationsBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.swipeRefresher.isRefreshing)
            assertTrue(viewBinding.translationList.isVisible)
            assertEquals(1, viewBinding.translationList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content

            verify(exactly = 1) { viewModel.markTranslationRemovalStateAsIdle() }
        }
    }
}
