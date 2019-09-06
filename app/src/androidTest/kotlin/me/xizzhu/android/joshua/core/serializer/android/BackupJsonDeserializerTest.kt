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

package me.xizzhu.android.joshua.core.serializer.android

import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupJsonDeserializerTest : BaseUnitTest() {
    private lateinit var deserializer: BackupJsonDeserializer

    @BeforeTest
    override fun setup() {
        super.setup()
        deserializer = BackupJsonDeserializer()
    }

    @Test
    fun testWithEmptyContent() {
        assertEquals(BackupManager.Data(emptyList(), emptyList(), emptyList(), ReadingProgress(0, 0L, emptyList())),
                deserializer.withContent("").deserialize())
    }
}
