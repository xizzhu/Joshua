/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.core.repository.local.android.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.android.BaseSqliteTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TranslationInfoDaoTest : BaseSqliteTest() {
    @Test
    fun testEmptyTable() {
        assertTrue(androidDatabase.translationInfoDao.read().isEmpty())
    }

    @Test
    fun testSaveThenRead() {
        androidDatabase.translationInfoDao.save(MockContents.kjvTranslationInfo)

        val actual = androidDatabase.translationInfoDao.read()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvTranslationInfo, actual[0])
    }

    @Test
    fun testSaveOverrideThenRead() {
        androidDatabase.translationInfoDao.save(MockContents.kjvTranslationInfo)
        androidDatabase.translationInfoDao.save(MockContents.kjvDownloadedTranslationInfo)

        val actual = androidDatabase.translationInfoDao.read()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvDownloadedTranslationInfo, actual[0])
    }

    @Test
    fun testReplace() {
        androidDatabase.translationInfoDao.save(MockContents.kjvDownloadedTranslationInfo)
        androidDatabase.translationInfoDao.save(TranslationInfo("shortName", "name", "language", 12345L, true))

        androidDatabase.translationInfoDao.replace(listOf(MockContents.kjvTranslationInfo))

        val actual = androidDatabase.translationInfoDao.read()
        assertEquals(1, actual.size)
        assertEquals(MockContents.kjvTranslationInfo, actual[0])
    }
}
