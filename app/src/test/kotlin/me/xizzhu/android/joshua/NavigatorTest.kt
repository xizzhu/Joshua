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

package me.xizzhu.android.joshua

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarksActivity
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.annotated.notes.NotesActivity
import me.xizzhu.android.joshua.progress.ReadingProgressActivity
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.settings.SettingsActivity
import me.xizzhu.android.joshua.strongnumber.StrongNumberActivity
import me.xizzhu.android.joshua.tests.BaseActivityTest
import me.xizzhu.android.joshua.translations.TranslationsActivity
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
class NavigatorTest : BaseActivityTest() {
    private lateinit var navigator: Navigator

    @BeforeTest
    override fun setup() {
        super.setup()
        navigator = Navigator()
    }

    @Test
    fun `test navigate(), internal activities`() {
        testNavigate<ReadingActivity>(Navigator.SCREEN_READING)
        testNavigate<SearchActivity>(Navigator.SCREEN_SEARCH)
        testNavigate<TranslationsActivity>(Navigator.SCREEN_TRANSLATIONS)
        testNavigate<ReadingProgressActivity>(Navigator.SCREEN_READING_PROGRESS)
        testNavigate<BookmarksActivity>(Navigator.SCREEN_BOOKMARKS)
        testNavigate<HighlightsActivity>(Navigator.SCREEN_HIGHLIGHTS)
        testNavigate<NotesActivity>(Navigator.SCREEN_NOTES)
        testNavigate<SettingsActivity>(Navigator.SCREEN_SETTINGS)
        testNavigate<StrongNumberActivity>(Navigator.SCREEN_STRONG_NUMBER)
    }

    private inline fun <reified A : Activity> testNavigate(@Navigator.Companion.Screen screen: Int) {
        withActivity<ReadingActivity> { activity ->
            navigator.navigate(activity, screen)

            val nextStartedIntent = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
            assertEquals(A::class.java.name, nextStartedIntent.component?.className)
        }
    }

    @Test
    fun `test navigate(), SCREEN_NOTES`() {
        withActivity<ReadingActivity> { activity ->
            navigator.navigate(activity, Navigator.SCREEN_NOTES, Bundle().apply { putString("test_key", "value") })

            val nextStartedIntent = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
            assertEquals(NotesActivity::class.java.name, nextStartedIntent.component?.className)
            assertEquals(1, nextStartedIntent.extras?.size())
            assertEquals("value", nextStartedIntent.getStringExtra("test_key"))
        }
    }

    @Test
    fun `test navigate(), SCREEN_RATE_ME`() {
        withActivity<ReadingActivity> { activity ->
            navigator.navigate(activity, Navigator.SCREEN_RATE_ME)

            val nextStartedIntent = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, nextStartedIntent.action)
            assertEquals(Uri.parse("market://details?id=me.xizzhu.android.joshua"), nextStartedIntent.data)
        }
    }

    @Test
    fun `test navigate(), SCREEN_WEBSITE`() {
        withActivity<ReadingActivity> { activity ->
            navigator.navigate(activity, Navigator.SCREEN_WEBSITE)

            val nextStartedIntent = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, nextStartedIntent.action)
            assertEquals(Uri.parse("https://xizzhu.me/pages/about-joshua/"), nextStartedIntent.data)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test navigate(), with unknown screen`() {
        withActivity<ReadingActivity> { activity ->
            navigator.navigate(activity, -1)
        }
    }

    @Test
    fun `test goBack()`() {
        withActivity<ReadingActivity> { activity ->
            navigator.goBack(activity)
            assertTrue(activity.isFinishing)
        }
    }
}
