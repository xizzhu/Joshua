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

package me.xizzhu.android.joshua.translations

import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import kotlin.test.assertEquals

class TranslationInfoComparatorTest : BaseUnitTest() {
    private val enUs1 = TranslationInfo("", "enUs1", "en_us", 0L, false)
    private val enUs2 = TranslationInfo("", "enUs2", "en_us", 0L, false)
    private val enGb = TranslationInfo("", "enGb", "en_gb", 0L, false)
    private val zhCn = TranslationInfo("", "zhCn", "zh_cn", 0L, false)

    private lateinit var comparator: TranslationInfoComparator

    @Before
    override fun setup() {
        super.setup()
        comparator = spy(TranslationInfoComparator())
        `when`(comparator.userLanguage()).thenReturn("en")
    }

    @Test
    fun testSameLocale() {
        val expected = listOf(enUs1, enUs2)
        assertEquals(expected, listOf(enUs1, enUs2).sortedWith(comparator))
        assertEquals(expected, listOf(enUs2, enUs1).sortedWith(comparator))
    }

    @Test
    fun testSameLanguageDifferentCountry() {
        val expected = listOf(enGb, enUs1)
        assertEquals(expected, listOf(enUs1, enGb).sortedWith(comparator))
        assertEquals(expected, listOf(enGb, enUs1).sortedWith(comparator))
    }

    @Test
    fun testDifferentLanguage() {
        val expected = listOf(enUs1, zhCn)
        assertEquals(expected, listOf(enUs1, zhCn).sortedWith(comparator))
        assertEquals(expected, listOf(zhCn, enUs1).sortedWith(comparator))
    }
}
