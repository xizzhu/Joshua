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

package me.xizzhu.android.joshua.strongnumber

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
import me.xizzhu.android.joshua.databinding.ActivityStrongNumberBinding
import me.xizzhu.android.joshua.preview.Preview
import me.xizzhu.android.joshua.preview.PreviewItem
import me.xizzhu.android.joshua.tests.BaseActivityTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.getProperty
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@UninstallModules(NavigationModule::class)
class StrongNumberActivityTest : BaseActivityTest() {
    @Module
    @InstallIn(SingletonComponent::class)
    object MockNavigationModule {
        val mockNavigator: Navigator = mockk()

        @Provides
        @Singleton
        fun provideNavigator(): Navigator = mockNavigator
    }

    @BindValue
    val viewModel: StrongNumberViewModel = mockk(relaxed = true)

    @BeforeTest
    override fun setup() {
        super.setup()
        clearMocks(MockNavigationModule.mockNavigator)
    }

    @Test
    fun `test adapter viewEvent, OpenVerse`() {
        withActivity<StrongNumberActivity> { activity ->
            val adapter: StrongNumberAdapter = activity.getProperty("adapter")
            val onViewEvent: (StrongNumberAdapter.ViewEvent) -> Unit = adapter.getProperty("onViewEvent")
            onViewEvent(StrongNumberAdapter.ViewEvent.OpenVerse(VerseIndex(1, 2, 3)))

            verify(exactly = 1) { viewModel.openVerse(VerseIndex(1, 2, 3)) }
        }
    }

    @Test
    fun `test adapter viewEvent, ShowPreview`() {
        withActivity<StrongNumberActivity> { activity ->
            val adapter: StrongNumberAdapter = activity.getProperty("adapter")
            val onViewEvent: (StrongNumberAdapter.ViewEvent) -> Unit = adapter.getProperty("onViewEvent")
            onViewEvent(StrongNumberAdapter.ViewEvent.ShowPreview(VerseIndex(1, 2, 3)))

            verify(exactly = 1) { viewModel.loadPreview(VerseIndex(1, 2, 3)) }
        }
    }

    @Test
    fun `test onViewActionEmitted(), OpenReadingScreen`() {
        every { viewModel.viewAction() } returns flowOf(StrongNumberViewModel.ViewAction.OpenReadingScreen)
        every { MockNavigationModule.mockNavigator.navigate(any(), Navigator.SCREEN_READING) } returns Unit

        withActivity<StrongNumberActivity> { activity ->
            verify(exactly = 1) { MockNavigationModule.mockNavigator.navigate(activity, Navigator.SCREEN_READING) }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and null error`() {
        every { viewModel.viewState() } returns flowOf(StrongNumberViewModel.ViewState(
            loading = true,
            items = emptyList(),
            preview = null,
            error = null,
        ))

        withActivity<StrongNumberActivity> { activity ->
            val viewBinding: ActivityStrongNumberBinding = activity.getProperty("viewBinding")
            assertTrue(viewBinding.loadingSpinner.isVisible)
            assertFalse(viewBinding.strongNumberList.isVisible)
            assertEquals(0, viewBinding.strongNumberList.adapter!!.itemCount)
        }
    }

    @Test
    fun `test onViewStateUpdated(), with preview, and null error`() {
        every { viewModel.viewState() } returns flowOf(StrongNumberViewModel.ViewState(
            loading = false,
            items = listOf(
                StrongNumberItem.BookName(settings = Settings.DEFAULT, bookName = MockContents.kjvBookNames[0])
            ),
            preview = Preview(
                title = "preview_title",
                items = listOf(PreviewItem.Verse(
                    settings = Settings.DEFAULT,
                    verseIndex = VerseIndex(0, 0, 0),
                    verseText = MockContents.kjvVerses[0].text.text,
                    followingEmptyVerseCount = 0,
                )),
                currentPosition = 0,
            ),
            error = null,
        ))

        withActivity<StrongNumberActivity> { activity ->
            val viewBinding: ActivityStrongNumberBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.strongNumberList.isVisible)
            assertEquals(1, viewBinding.strongNumberList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and PreviewLoadingError error`() {
        every { viewModel.viewState() } returns flowOf(StrongNumberViewModel.ViewState(
            loading = true,
            items = emptyList(),
            preview = null,
            error = StrongNumberViewModel.ViewState.Error.PreviewLoadingError(VerseIndex(1, 2, 3)),
        ))

        withActivity<StrongNumberActivity> { activity ->
            val viewBinding: ActivityStrongNumberBinding = activity.getProperty("viewBinding")
            assertTrue(viewBinding.loadingSpinner.isVisible)
            assertFalse(viewBinding.strongNumberList.isVisible)
            assertEquals(0, viewBinding.strongNumberList.adapter!!.itemCount)

            verify(exactly = 1) {
                viewModel.markErrorAsShown(StrongNumberViewModel.ViewState.Error.PreviewLoadingError(VerseIndex(1, 2, 3)))
                viewModel.openVerse(VerseIndex(1, 2, 3))
            }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and StrongNumberLoadingError error`() {
        every { viewModel.viewState() } returns flowOf(StrongNumberViewModel.ViewState(
            loading = true,
            items = emptyList(),
            preview = null,
            error = StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError,
        ))

        withActivity<StrongNumberActivity> { activity ->
            val viewBinding: ActivityStrongNumberBinding = activity.getProperty("viewBinding")
            assertTrue(viewBinding.loadingSpinner.isVisible)
            assertFalse(viewBinding.strongNumberList.isVisible)
            assertEquals(0, viewBinding.strongNumberList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content and click events
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and VerseOpeningError error`() {
        every { viewModel.viewState() } returns flowOf(StrongNumberViewModel.ViewState(
            loading = true,
            items = emptyList(),
            preview = null,
            error = StrongNumberViewModel.ViewState.Error.VerseOpeningError(VerseIndex(1, 2, 3)),
        ))

        withActivity<StrongNumberActivity> { activity ->
            val viewBinding: ActivityStrongNumberBinding = activity.getProperty("viewBinding")
            assertTrue(viewBinding.loadingSpinner.isVisible)
            assertFalse(viewBinding.strongNumberList.isVisible)
            assertEquals(0, viewBinding.strongNumberList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content and click events
        }
    }
}
