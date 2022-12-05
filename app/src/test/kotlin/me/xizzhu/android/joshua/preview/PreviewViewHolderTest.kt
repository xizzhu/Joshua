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

package me.xizzhu.android.joshua.preview

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreviewViewHolderTest : BaseUnitTest() {
    private lateinit var context: Context

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
    }

    @Test
    fun `test Verse`() {
        var openVerseCalled = 0
        val viewHolder = PreviewViewHolder.Verse(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is PreviewAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
            }
        }

        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.verse).text.isEmpty())
        assertEquals(0, openVerseCalled)

        viewHolder.itemView.performClick()
        assertEquals(0, openVerseCalled)

        viewHolder.bindData(
            PreviewItem.Verse(
                settings = Settings.DEFAULT,
                verseIndex = VerseIndex(0, 0, 0),
                verseText = MockContents.kjvVerses[0].text.text,
                followingEmptyVerseCount = 0
            )
        )
        assertEquals(
            "1:1 In the beginning God created the heaven and the earth.",
            viewHolder.itemView.findViewById<TextView>(R.id.verse).text.toString()
        )
        assertEquals(0, openVerseCalled)

        viewHolder.itemView.performClick()
        assertEquals(1, openVerseCalled)
    }

    @Test
    fun `test VerseWithQuery`() {
        var openVerseCalled = 0
        val viewHolder = PreviewViewHolder.VerseWithQuery(
            inflater = LayoutInflater.from(context),
            parent = FrameLayout(context)
        ) { viewEvent ->
            when (viewEvent) {
                is PreviewAdapter.ViewEvent.OpenVerse -> {
                    assertEquals(VerseIndex(0, 0, 0), viewEvent.verseToOpen)
                    openVerseCalled++
                }
            }
        }

        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.verse).text.isEmpty())
        assertEquals(0, openVerseCalled)

        viewHolder.itemView.performClick()
        assertEquals(0, openVerseCalled)

        viewHolder.bindData(
            PreviewItem.VerseWithQuery(
                settings = Settings.DEFAULT,
                verseIndex = VerseIndex(0, 0, 0),
                verseText = MockContents.kjvVerses[0].text.text,
                query = "query",
                followingEmptyVerseCount = 0
            )
        )
        assertEquals(
            "1:1 In the beginning God created the heaven and the earth.",
            viewHolder.itemView.findViewById<TextView>(R.id.verse).text.toString()
        )
        assertEquals(0, openVerseCalled)

        viewHolder.itemView.performClick()
        assertEquals(1, openVerseCalled)
    }
}
