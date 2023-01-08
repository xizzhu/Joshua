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

package me.xizzhu.android.joshua.annotated

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
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarksActivity
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarksViewModel
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityAnnotatedBinding
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
class AnnotatedVerseActivityTest : BaseActivityTest() {
    @Module
    @InstallIn(SingletonComponent::class)
    object MockNavigationModule {
        val mockNavigator: Navigator = mockk()

        @Provides
        @Singleton
        fun provideNavigator(): Navigator = mockNavigator
    }

    @BindValue
    val viewModel: BookmarksViewModel = mockk(relaxed = true)

    @BeforeTest
    override fun setup() {
        super.setup()
        clearMocks(MockNavigationModule.mockNavigator)
    }

    @Test
    fun `test adapter viewEvent, OpenVerse`() {
        withActivity<BookmarksActivity> { activity ->
            val adapter: AnnotatedVerseAdapter = (activity as AnnotatedVerseActivity<*, *>).getProperty("adapter")
            val onViewEvent: (AnnotatedVerseAdapter.ViewEvent) -> Unit = adapter.getProperty("onViewEvent")
            onViewEvent(AnnotatedVerseAdapter.ViewEvent.OpenVerse(VerseIndex(1, 2, 3)))

            verify(exactly = 1) { viewModel.openVerse(VerseIndex(1, 2, 3)) }
        }
    }

    @Test
    fun `test adapter viewEvent, ShowPreview`() {
        withActivity<BookmarksActivity> { activity ->
            val adapter: AnnotatedVerseAdapter = (activity as AnnotatedVerseActivity<*, *>).getProperty("adapter")
            val onViewEvent: (AnnotatedVerseAdapter.ViewEvent) -> Unit = adapter.getProperty("onViewEvent")
            onViewEvent(AnnotatedVerseAdapter.ViewEvent.ShowPreview(VerseIndex(1, 2, 3)))

            verify(exactly = 1) { viewModel.loadPreview(VerseIndex(1, 2, 3)) }
        }
    }

    @Test
    fun `test onViewActionEmitted(), OpenReadingScreen`() {
        every { viewModel.viewAction() } returns flowOf(AnnotatedVerseViewModel.ViewAction.OpenReadingScreen)
        every { MockNavigationModule.mockNavigator.navigate(any(), Navigator.SCREEN_READING) } returns Unit

        withActivity<BookmarksActivity> { activity ->
            verify(exactly = 1) { MockNavigationModule.mockNavigator.navigate(activity, Navigator.SCREEN_READING) }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and null error`() {
        every { viewModel.viewState() } returns flowOf(AnnotatedVerseViewModel.ViewState(
            loading = true,
            sortOrder = Constants.SORT_BY_DATE,
            items = emptyList(),
            preview = null,
            error = null,
        ))

        withActivity<BookmarksActivity> { activity ->
            val viewBinding: ActivityAnnotatedBinding = activity.getProperty("viewBinding")
            assertTrue(viewBinding.loadingSpinner.isVisible)
            assertFalse(viewBinding.verseList.isVisible)
            assertEquals(0, viewBinding.verseList.adapter!!.itemCount)
        }
    }

    @Test
    fun `test onViewStateUpdated(), with preview, and null error`() {
        every { viewModel.viewState() } returns flowOf(AnnotatedVerseViewModel.ViewState(
            loading = false,
            sortOrder = Constants.SORT_BY_DATE,
            items = listOf(AnnotatedVerseItem.Header(Settings.DEFAULT, "text", false)),
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

        withActivity<BookmarksActivity> { activity ->
            val viewBinding: ActivityAnnotatedBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.verseList.isVisible)
            assertEquals(1, viewBinding.verseList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and AnnotatedVersesLoadingError error`() {
        every { viewModel.viewState() } returns flowOf(AnnotatedVerseViewModel.ViewState(
            loading = false,
            sortOrder = Constants.SORT_BY_DATE,
            items = emptyList(),
            preview = null,
            error = AnnotatedVerseViewModel.ViewState.Error.AnnotatedVersesLoadingError,
        ))

        withActivity<BookmarksActivity> { activity ->
            val viewBinding: ActivityAnnotatedBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.verseList.isVisible)
            assertEquals(0, viewBinding.verseList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and PreviewLoadingError error`() {
        every { viewModel.viewState() } returns flowOf(AnnotatedVerseViewModel.ViewState(
            loading = false,
            sortOrder = Constants.SORT_BY_DATE,
            items = emptyList(),
            preview = null,
            error = AnnotatedVerseViewModel.ViewState.Error.PreviewLoadingError(VerseIndex(1, 2, 3)),
        ))

        withActivity<BookmarksActivity> { activity ->
            val viewBinding: ActivityAnnotatedBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.verseList.isVisible)
            assertEquals(0, viewBinding.verseList.adapter!!.itemCount)

            verify(exactly = 1) {
                viewModel.markErrorAsShown(AnnotatedVerseViewModel.ViewState.Error.PreviewLoadingError(VerseIndex(1, 2, 3)))
                viewModel.openVerse(VerseIndex(1, 2, 3))
            }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and SortOrderSavingError error`() {
        every { viewModel.viewState() } returns flowOf(AnnotatedVerseViewModel.ViewState(
            loading = false,
            sortOrder = Constants.SORT_BY_DATE,
            items = emptyList(),
            preview = null,
            error = AnnotatedVerseViewModel.ViewState.Error.SortOrderSavingError(Constants.SORT_BY_BOOK),
        ))

        withActivity<BookmarksActivity> { activity ->
            val viewBinding: ActivityAnnotatedBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.verseList.isVisible)
            assertEquals(0, viewBinding.verseList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null preview, and VerseOpeningError error`() {
        every { viewModel.viewState() } returns flowOf(AnnotatedVerseViewModel.ViewState(
            loading = false,
            sortOrder = Constants.SORT_BY_DATE,
            items = emptyList(),
            preview = null,
            error = AnnotatedVerseViewModel.ViewState.Error.VerseOpeningError(VerseIndex(1, 2, 3)),
        ))

        withActivity<BookmarksActivity> { activity ->
            val viewBinding: ActivityAnnotatedBinding = activity.getProperty("viewBinding")
            assertFalse(viewBinding.loadingSpinner.isVisible)
            assertTrue(viewBinding.verseList.isVisible)
            assertEquals(0, viewBinding.verseList.adapter!!.itemCount)

            assertTrue(ShadowDialog.getLatestDialog().isShowing) // TODO test dialog content
        }
    }
}
