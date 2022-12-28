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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class TranslationViewHolderTest : BaseUnitTest() {
    private lateinit var context: Context

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
    }

    @Test
    fun `test Header`() {
        val viewHolder = TranslationViewHolder.Header(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        )
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.title).text.isEmpty())

        // hide divider
        viewHolder.bindData(
            item = TranslationItem.Header(
                settings = Settings.DEFAULT,
                title = "my title",
                hideDivider = true,
            )
        )
        assertEquals("my title", viewHolder.itemView.findViewById<TextView>(R.id.title).text.toString())
        assertFalse(viewHolder.itemView.findViewById<View>(R.id.divider).isVisible)

        // show divider
        viewHolder.bindData(
            item = TranslationItem.Header(
                settings = Settings.DEFAULT,
                title = "another title",
                hideDivider = false,
            )
        )
        assertEquals("another title", viewHolder.itemView.findViewById<TextView>(R.id.title).text.toString())
        assertTrue(viewHolder.itemView.findViewById<View>(R.id.divider).isVisible)
    }

    @Test
    fun `test Translation`() {
        var downloadTranslationCalled = 0
        var removeTranslationCalled = 0
        var selectTranslationCalled = 0
        val viewHolder = TranslationViewHolder.Translation(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is TranslationAdapter.ViewEvent.DownloadTranslation -> {
                    assertEquals(MockContents.kjvTranslationInfo, viewEvent.translationToDownload)
                    downloadTranslationCalled++
                }
                is TranslationAdapter.ViewEvent.RemoveTranslation -> {
                    assertEquals(MockContents.kjvDownloadedTranslationInfo, viewEvent.translationToRemove)
                    removeTranslationCalled++
                }
                is TranslationAdapter.ViewEvent.SelectTranslation -> {
                    assertEquals(MockContents.kjvDownloadedTranslationInfo, viewEvent.translationToSelect)
                    selectTranslationCalled++
                }
            }
        }
        assertTrue((viewHolder.itemView as TextView).text.isEmpty())

        viewHolder.itemView.performClick()
        viewHolder.itemView.performLongClick()
        assertEquals(0, downloadTranslationCalled)
        assertEquals(0, removeTranslationCalled)
        assertEquals(0, selectTranslationCalled)

        // available translation
        viewHolder.bindData(
            item = TranslationItem.Translation(
                settings = Settings.DEFAULT,
                translationInfo = MockContents.kjvTranslationInfo,
                isCurrentTranslation = false,
            )
        )
        assertEquals("Authorized King James", (viewHolder.itemView as TextView).text.toString())

        viewHolder.itemView.performClick()
        assertEquals(1, downloadTranslationCalled)
        assertEquals(0, removeTranslationCalled)
        assertEquals(0, selectTranslationCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(2, downloadTranslationCalled)
        assertEquals(0, removeTranslationCalled)
        assertEquals(0, selectTranslationCalled)

        // downloaded, but not current translation
        viewHolder.bindData(
            item = TranslationItem.Translation(
                settings = Settings.DEFAULT,
                translationInfo = MockContents.kjvDownloadedTranslationInfo,
                isCurrentTranslation = false,
            )
        )
        assertEquals("Authorized King James", (viewHolder.itemView as TextView).text.toString())

        viewHolder.itemView.performClick()
        assertEquals(2, downloadTranslationCalled)
        assertEquals(0, removeTranslationCalled)
        assertEquals(1, selectTranslationCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(2, downloadTranslationCalled)
        assertEquals(1, removeTranslationCalled)
        assertEquals(1, selectTranslationCalled)

        // downloaded, and current translation
        viewHolder.bindData(
            item = TranslationItem.Translation(
                settings = Settings.DEFAULT,
                translationInfo = MockContents.kjvDownloadedTranslationInfo,
                isCurrentTranslation = true,
            )
        )
        assertEquals("Authorized King James", (viewHolder.itemView as TextView).text.toString())

        viewHolder.itemView.performClick()
        assertEquals(2, downloadTranslationCalled)
        assertEquals(1, removeTranslationCalled)
        assertEquals(2, selectTranslationCalled)

        viewHolder.itemView.performLongClick()
        assertEquals(2, downloadTranslationCalled)
        assertEquals(1, removeTranslationCalled)
        assertEquals(2, selectTranslationCalled)
    }
}
