/*
 * Copyright (C) 2019 Xizhi Zhu
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
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Before
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatterTest : BaseUnitTest() {
    @Mock
    private lateinit var resources: Resources

    @Before
    override fun setup() {
        super.setup()

        `when`(resources.getStringArray(R.array.text_months)).thenReturn(Array(12) { "" })
    }

    @Test
    fun testFormatDateSameYear() {
        val expected = "Random expected text"
        `when`(resources.getString(anyInt(), anyString(), anyInt())).thenReturn(expected)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)

        val actual = calendar.timeInMillis.formatDate(resources, calendar)
        assertEquals(expected, actual)
        verify(resources, times(1)).getStringArray(R.array.text_months)
    }

    @Test
    fun testFormatDateDifferentYear() {
        val expected = "Random expected text"
        `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn(expected)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)

        val actual = calendar.timeInMillis.formatDate(resources, calendar)
        assertEquals(expected, actual)
        verify(resources, times(1)).getStringArray(R.array.text_months)
    }
}
