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

package me.xizzhu.android.joshua.core.repository.remote

import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.assertEquals
import kotlin.test.Test

class RemoteTranslationInfoTest : BaseUnitTest() {
    @Test
    fun testFromTranslationInfo() {
        val expected = MockContents.kjvRemoteTranslationInfo
        val actual = RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo)
        assertEquals(expected, actual)
    }

    @Test
    fun testToTranslationInfo() {
        val expected = MockContents.kjvTranslationInfo
        val actual = MockContents.kjvRemoteTranslationInfo.toTranslationInfo(false)
        assertEquals(expected, actual)
    }

    @Test
    fun testToDownloadedTranslationInfo() {
        val expected = MockContents.kjvDownloadedTranslationInfo
        val actual = MockContents.kjvRemoteTranslationInfo.toTranslationInfo(true)
        assertEquals(expected, actual)
    }
}
