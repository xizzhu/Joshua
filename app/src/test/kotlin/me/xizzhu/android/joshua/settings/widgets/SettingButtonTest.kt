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
class SettingButtonTest : BaseUnitTest() {
    private lateinit var context: Context

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
    }

    @Test
    fun `test constructor, without attrs`() {
        val button = SettingButton(context)
        assertTrue(button.findViewById<TextView>(R.id.title).text.isEmpty())
        assertTrue(button.findViewById<TextView>(R.id.description).text.isEmpty())
    }

    @Test
    fun `test constructor, with only title`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.settingButtonTitle, "title").build()
        val button = SettingButton(context, attrs)
        assertEquals("title", button.findViewById<TextView>(R.id.title).text.toString())
        assertTrue(button.findViewById<TextView>(R.id.description).text.isEmpty())
    }

    @Test
    fun `test constructor, with only description`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.settingButtonDescription, "description").build()
        val button = SettingButton(context, attrs)
        assertTrue(button.findViewById<TextView>(R.id.title).text.isEmpty())
        assertEquals("description", button.findViewById<TextView>(R.id.description).text.toString())
    }

    @Test
    fun `test constructor, with title and description, then override`() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.settingButtonTitle, "title")
            .addAttribute(R.attr.settingButtonDescription, "description")
            .build()
        val button = SettingButton(context, attrs)
        assertEquals("title", button.findViewById<TextView>(R.id.title).text.toString())
        assertEquals("description", button.findViewById<TextView>(R.id.description).text.toString())

        button.setDescription("updated desc")
        assertEquals("title", button.findViewById<TextView>(R.id.title).text.toString())
        assertEquals("updated desc", button.findViewById<TextView>(R.id.description).text.toString())

        button.setDescription(R.string.title_settings)
        assertEquals("title", button.findViewById<TextView>(R.id.title).text.toString())
        assertEquals("Settings", button.findViewById<TextView>(R.id.description).text.toString())
    }
}
