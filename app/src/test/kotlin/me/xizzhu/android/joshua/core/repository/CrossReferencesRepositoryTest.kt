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

package me.xizzhu.android.joshua.core.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalCrossReferencesStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteCrossReferences
import me.xizzhu.android.joshua.core.repository.remote.RemoteCrossReferencesStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossReferencesRepositoryTest : BaseUnitTest() {
    private lateinit var localCrossReferencesStorage: LocalCrossReferencesStorage
    private lateinit var remoteCrossReferencesStorage: RemoteCrossReferencesStorage
    private lateinit var crossReferencesRepository: CrossReferencesRepository

    @BeforeTest
    override fun setup() {
        super.setup()

        localCrossReferencesStorage = mockk()
        remoteCrossReferencesStorage = mockk()
        crossReferencesRepository = CrossReferencesRepository(localCrossReferencesStorage, remoteCrossReferencesStorage)
    }

    @Test
    fun `test download()`() = runTest {
        val references = mockk<Map<VerseIndex, List<VerseIndex>>>()
        val remoteCrossReferences = mockk<RemoteCrossReferences>()
        every { remoteCrossReferences.references } returns references
        coEvery { localCrossReferencesStorage.save(references) } returns Unit
        coEvery { remoteCrossReferencesStorage.removeCrossReferencesCache() } returns Unit
        coEvery { remoteCrossReferencesStorage.fetchCrossReferences(any()) } returns remoteCrossReferences

        with(crossReferencesRepository.download().toList()) {
            assertTrue(this.isNotEmpty())
            assertEquals(100, this.last())
        }

        coVerify(exactly = 1) {
            remoteCrossReferencesStorage.fetchCrossReferences(any())
            localCrossReferencesStorage.save(references)
            remoteCrossReferencesStorage.removeCrossReferencesCache()
        }
    }
}
