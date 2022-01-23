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

import io.mockk.every
import io.mockk.spyk
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationInfoComparatorTest : BaseUnitTest() {
    private val enUs1 = TranslationInfo("2", "enUs1", "en_us", 0L, false)
    private val enUs2 = TranslationInfo("1", "enUs2", "en_us", 0L, false)
    private val enGb = TranslationInfo("3", "enGb", "en_gb", 0L, false)
    private val zhCn = TranslationInfo("5", "zhCn", "zh_cn", 0L, false)
    private val fiFi = TranslationInfo("4", "fiFi", "fi_fi", 0L, false)

    private lateinit var comparatorLanguageThenName: TranslationInfoComparator
    private lateinit var comparatorLanguageThenShortName: TranslationInfoComparator
    private lateinit var defaultLocale: Locale

    @BeforeTest
    override fun setup() {
        super.setup()

        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)

        comparatorLanguageThenName = spyk(TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME))
        every { comparatorLanguageThenName.userLanguage() } returns Locale.US.displayLanguage

        comparatorLanguageThenShortName = spyk(TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME))
        every { comparatorLanguageThenShortName.userLanguage() } returns Locale.US.displayLanguage
    }

    @AfterTest
    override fun tearDown() {
        Locale.setDefault(defaultLocale)

        super.tearDown()
    }

    @Test
    fun testSameLocale() {
        val expected = listOf(enUs1, enUs2)
        assertEquals(expected, listOf(enUs1, enUs2).sortedWith(comparatorLanguageThenName))
        assertEquals(expected, listOf(enUs2, enUs1).sortedWith(comparatorLanguageThenName))
    }

    @Test
    fun testSameLocaleByShortName() {
        val expected = listOf(enUs2, enUs1)
        assertEquals(expected, listOf(enUs1, enUs2).sortedWith(comparatorLanguageThenShortName))
        assertEquals(expected, listOf(enUs2, enUs1).sortedWith(comparatorLanguageThenShortName))
    }

    @Test
    fun testSameLanguageDifferentCountry() {
        val expected = listOf(enGb, enUs1)
        assertEquals(expected, listOf(enUs1, enGb).sortedWith(comparatorLanguageThenName))
        assertEquals(expected, listOf(enGb, enUs1).sortedWith(comparatorLanguageThenName))
    }

    @Test
    fun testDifferentLanguage() {
        val expected = listOf(enUs1, enUs2, zhCn, fiFi)
        assertEquals(expected, listOf(enUs1, fiFi, enUs2, zhCn).sortedWith(comparatorLanguageThenName))
        assertEquals(expected, listOf(zhCn, enUs1, fiFi, enUs2).sortedWith(comparatorLanguageThenName))
        assertEquals(expected, listOf(enUs2, enUs1, zhCn, fiFi).sortedWith(comparatorLanguageThenName))
    }

    @Test
    fun testDifferentLanguageByShortName() {
        val expected = listOf(enUs2, enUs1, zhCn, fiFi)
        assertEquals(expected, listOf(enUs1, fiFi, enUs2, zhCn).sortedWith(comparatorLanguageThenShortName))
        assertEquals(expected, listOf(zhCn, enUs1, fiFi, enUs2).sortedWith(comparatorLanguageThenShortName))
        assertEquals(expected, listOf(enUs2, enUs1, zhCn, fiFi).sortedWith(comparatorLanguageThenShortName))
    }
}
