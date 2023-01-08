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

package me.xizzhu.android.joshua.progress

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
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityReadingProgressBinding
import me.xizzhu.android.joshua.tests.BaseActivityTest
import me.xizzhu.android.joshua.tests.getProperty
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@UninstallModules(NavigationModule::class)
class ReadingProgressActivityTest : BaseActivityTest() {
    @Module
    @InstallIn(SingletonComponent::class)
    object MockNavigationModule {
        val mockNavigator: Navigator = mockk()

        @Provides
        @Singleton
        fun provideNavigator(): Navigator = mockNavigator
    }

    @BindValue
    val viewModel: ReadingProgressViewModel = mockk(relaxed = true)

    @BeforeTest
    override fun setup() {
        super.setup()
        clearMocks(MockNavigationModule.mockNavigator)
    }

    @Test
    fun `test adapter viewEvent, ExpandOrCollapseBook`() {
        withActivity<ReadingProgressActivity> { activity ->
            val adapter: ReadingProgressAdapter = activity.getProperty("adapter")
            val onViewEvent: (ReadingProgressAdapter.ViewEvent) -> Unit = adapter.getProperty("onViewEvent")
            onViewEvent(ReadingProgressAdapter.ViewEvent.ExpandOrCollapseBook(bookIndex = 1))

            verify(exactly = 1) { viewModel.expandOrCollapseBook(bookIndex = 1) }
        }
    }

    @Test
    fun `test adapter viewEvent, OpenVerse`() {
        withActivity<ReadingProgressActivity> { activity ->
            val adapter: ReadingProgressAdapter = activity.getProperty("adapter")
            val onViewEvent: (ReadingProgressAdapter.ViewEvent) -> Unit = adapter.getProperty("onViewEvent")
            onViewEvent(ReadingProgressAdapter.ViewEvent.OpenVerse(VerseIndex(1, 2, 3)))

            verify(exactly = 1) { viewModel.openVerse(VerseIndex(1, 2, 3)) }
        }
    }

    @Test
    fun `test onViewActionEmitted(), OpenReadingScreen`() {
        every { viewModel.viewAction() } returns flowOf(ReadingProgressViewModel.ViewAction.OpenReadingScreen)
        every { MockNavigationModule.mockNavigator.navigate(any(), Navigator.SCREEN_READING) } returns Unit

        withActivity<ReadingProgressActivity> { activity ->
            verify(exactly = 1) { MockNavigationModule.mockNavigator.navigate(activity, Navigator.SCREEN_READING) }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null error`() {
        every { viewModel.viewState() } returns flowOf(ReadingProgressViewModel.ViewState(
            loading = true,
            items = emptyList(),
            error = null,
        ))

        withActivity<ReadingProgressActivity> { activity ->
            val viewBinding: ActivityReadingProgressBinding = activity.getProperty("viewBinding")
            assertTrue(viewBinding.loadingSpinner.isVisible)
            assertFalse(viewBinding.readingProgressList.isVisible)
            assertEquals(0, viewBinding.readingProgressList.adapter!!.itemCount)
        }
    }

    @Test
    fun `test onViewStateUpdated(), with ReadingProgressLoadingError error`() {
        every { viewModel.viewState() } returns flowOf(ReadingProgressViewModel.ViewState(
            loading = false,
            items = listOf(ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0)),
            error = ReadingProgressViewModel.ViewState.Error.ReadingProgressLoadingError,
        ))

        withActivity<ReadingProgressActivity> { activity ->
            val viewBinding: ActivityReadingProgressBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.readingProgressList.isVisible)
            assertEquals(1, viewBinding.readingProgressList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content and click events
        }
    }

    @Test
    fun `test onViewStateUpdated(), with VerseOpeningError error`() {
        every { viewModel.viewState() } returns flowOf(ReadingProgressViewModel.ViewState(
            loading = false,
            items = listOf(ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0)),
            error = ReadingProgressViewModel.ViewState.Error.VerseOpeningError(VerseIndex(1, 2, 3)),
        ))

        withActivity<ReadingProgressActivity> { activity ->
            val viewBinding: ActivityReadingProgressBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.readingProgressList.isVisible)
            assertEquals(1, viewBinding.readingProgressList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content and click events
        }
    }
}
