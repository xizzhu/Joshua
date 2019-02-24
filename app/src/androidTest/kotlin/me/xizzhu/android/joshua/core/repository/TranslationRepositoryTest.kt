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

package me.xizzhu.android.joshua.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.tests.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TranslationRepositoryTest : BaseTest() {
    private lateinit var translationRepository: TranslationRepository

    @Before
    override fun setup() {
        super.setup()
        translationRepository = TranslationRepository(createLocalStorage(), createBackendService())
    }

    @Test
    fun testDefaultLocalTranslations() {
        runBlocking {
            Assert.assertTrue(translationRepository.readTranslationsFromLocal().isEmpty())
        }
    }

    @Test
    fun testLocalTranslations() {
        val expected = translations.clone()
        val actual = runBlocking {
            prepareTranslations()
            translationRepository.readTranslationsFromLocal()
        }
        Assert.assertEquals(expected, actual)
    }
}
