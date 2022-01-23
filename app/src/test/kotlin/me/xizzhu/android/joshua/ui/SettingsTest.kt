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

package me.xizzhu.android.joshua.ui

import android.content.res.Resources
import android.util.TypedValue
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SettingsTest : BaseUnitTest() {
    private lateinit var resources: Resources
    private lateinit var textView: TextView

    @BeforeTest
    override fun setup() {
        super.setup()

        resources = mockk()

        textView = mockk()
        every { textView.resources } returns resources
    }

    @Test
    fun `test getPrimaryTextSize()`() {
        every { resources.getDimension(R.dimen.text_primary) } returns 12.0F
        assertEquals(24.0F, Settings.DEFAULT.copy(fontSizeScale = 2.0F).getPrimaryTextSize(resources))

        every { resources.getDimension(any()) } answers { fail() }
        assertEquals(24.0F, Settings.DEFAULT.copy(fontSizeScale = 2.0F).getPrimaryTextSize(resources))
        assertEquals(30.0F, Settings.DEFAULT.copy(fontSizeScale = 2.5F).getPrimaryTextSize(resources))
    }

    @Test
    fun `test getSecondaryTextSize()`() {
        every { resources.getDimension(R.dimen.text_secondary) } returns 12.0F
        assertEquals(24.0F, Settings.DEFAULT.copy(fontSizeScale = 2.0F).getSecondaryTextSize(resources))

        every { resources.getDimension(any()) } answers { fail() }
        assertEquals(24.0F, Settings.DEFAULT.copy(fontSizeScale = 2.0F).getSecondaryTextSize(resources))
        assertEquals(30.0F, Settings.DEFAULT.copy(fontSizeScale = 2.5F).getSecondaryTextSize(resources))
    }

    @Test
    fun `test setPrimaryTextSize()`() {
        every { resources.getDimension(R.dimen.text_primary) } returns 12.0F
        every { textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24.0F) } returns Unit

        textView.setPrimaryTextSize(Settings.DEFAULT.copy(fontSizeScale = 2.0F))
        verify(exactly = 1) { textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24.0F) }
    }

    @Test
    fun `test setSecondaryTextSize()`() {
        every { resources.getDimension(R.dimen.text_secondary) } returns 12.0F
        every { textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24.0F) } returns Unit

        textView.setSecondaryTextSize(Settings.DEFAULT.copy(fontSizeScale = 2.0F))
        verify(exactly = 1) { textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24.0F) }
    }
}
