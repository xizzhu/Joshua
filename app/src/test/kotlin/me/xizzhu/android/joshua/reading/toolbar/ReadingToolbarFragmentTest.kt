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

package me.xizzhu.android.joshua.reading.toolbar

import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.inject.Singleton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import me.xizzhu.android.joshua.NavigationModule
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.databinding.FragmentReadingToolbarBinding
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.chapter.ChapterSelectionViewModel
import org.junit.runner.RunWith
import me.xizzhu.android.joshua.tests.BaseFragmentTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.getProperty

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(NavigationModule::class)
class ReadingToolbarFragmentTest : BaseFragmentTest() {
    @Module
    @InstallIn(SingletonComponent::class)
    object MockNavigationModule {
        val mockNavigator: Navigator = mockk()

        @Provides
        @Singleton
        fun provideNavigator(): Navigator = mockNavigator
    }

    @BindValue
    val readingToolbarViewModel: ReadingToolbarViewModel = mockk(relaxed = true)

    @BindValue
    val chapterSelectionViewModel: ChapterSelectionViewModel = mockk(relaxed = true)

    @Test
    fun `test toolbar ViewEvent`() {
        `verify toolbar ViewEvent`(R.id.action_bookmarks, Navigator.SCREEN_BOOKMARKS)
        `verify toolbar ViewEvent`(R.id.action_highlights, Navigator.SCREEN_HIGHLIGHTS)
        `verify toolbar ViewEvent`(R.id.action_notes, Navigator.SCREEN_NOTES)
        `verify toolbar ViewEvent`(R.id.action_reading_progress, Navigator.SCREEN_READING_PROGRESS)
        `verify toolbar ViewEvent`(R.id.action_search, Navigator.SCREEN_SEARCH)
        `verify toolbar ViewEvent`(R.id.action_settings, Navigator.SCREEN_SETTINGS)
    }

    private fun `verify toolbar ViewEvent`(@IdRes menuItemId: Int, @Navigator.Companion.Screen screen: Int) {
        every { MockNavigationModule.mockNavigator.navigate(any(), screen) } returns Unit

        withFragment<ReadingToolbarFragment, ReadingActivity> { fragment ->
            val viewBinding: FragmentReadingToolbarBinding = fragment.getProperty("viewBinding")
            val onMenuItemClickListener: Toolbar.OnMenuItemClickListener = (viewBinding.toolbar as Toolbar).getProperty("mOnMenuItemClickListener")
            assertTrue(onMenuItemClickListener.onMenuItemClick(viewBinding.toolbar.menu.findItem(menuItemId)))
            verify(exactly = 1) { MockNavigationModule.mockNavigator.navigate(any(), screen) }
        }
    }

    @Test
    fun `test onViewStateUpdated(), with null error`() {
        every { readingToolbarViewModel.viewState() } returns flowOf(ReadingToolbarViewModel.ViewState(
            title = "my title",
            translationItems = emptyList(),
            error = null
        ))
        withFragment<ReadingToolbarFragment, ReadingActivity> { fragment ->
            val viewBinding: FragmentReadingToolbarBinding = fragment.getProperty("viewBinding")
            assertEquals("my title", viewBinding.toolbar.title.toString())
        }
    }

    @Test
    fun `test onViewStateUpdated(), with ParallelTranslationRequestingError error`() {
        every { readingToolbarViewModel.viewState() } returns flowOf(ReadingToolbarViewModel.ViewState(
            title = "my title",
            translationItems = emptyList(),
            error = ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRequestingError(MockContents.kjvShortName)
        ))
        withFragment<ReadingToolbarFragment, ReadingActivity> { fragment ->
            val viewBinding: FragmentReadingToolbarBinding = fragment.getProperty("viewBinding")
            assertEquals("my title", viewBinding.toolbar.title.toString())
            verify { readingToolbarViewModel.markErrorAsShown(ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRequestingError("KJV")) }
        }
    }
}
