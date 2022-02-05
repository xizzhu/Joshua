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

package me.xizzhu.android.joshua.core.repository.remote.android

import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class HttpCrossReferencesServiceTest : BaseUnitTest() {
    private lateinit var crossReferencesService: HttpCrossReferencesService

    @BeforeTest
    override fun setup() {
        super.setup()

        crossReferencesService = HttpCrossReferencesService(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test toRemoteStrongNumberIndexes()`() {
        crossReferencesService.toRemoteCrossReferences(javaClass.classLoader.getResourceAsStream("cross_references.zip"))
                .references.forEach { (from, to) ->
                    assertTrue(from.isValid())
                    assertTrue(to.isNotEmpty())
                    to.forEach { assertTrue(it.isValid()) }
                }
    }
}
