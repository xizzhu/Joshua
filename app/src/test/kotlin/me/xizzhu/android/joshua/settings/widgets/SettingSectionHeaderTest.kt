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

package me.xizzhu.android.joshua.settings.widgets

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingSectionHeaderTest : BaseUnitTest() {
    private lateinit var context: Context

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
    }

    @Test
    fun `test constructor, without attrs`() {
        val header = SettingSectionHeader(context)
        assertTrue(header.findViewById<TextView>(R.id.title).text.isEmpty())
    }

    @Test
    fun `test constructor, with title`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.settingSectionHeaderTitle, "title").build()
        val header = SettingSectionHeader(context, attrs)
        assertEquals("title", header.findViewById<TextView>(R.id.title).text.toString())
    }
}
