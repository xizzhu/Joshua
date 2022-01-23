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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.flow.Flow
import me.xizzhu.android.joshua.core.repository.StrongNumberRepository

data class StrongNumber(val sn: String, val meaning: String) {
    companion object {
        const val TOTAL_HEBREW_ROOT_WORDS = 8674
        const val TOTAL_GREEK_ROOT_WORDS = 5624

        val INVALID = StrongNumber("", "")
    }

    fun isValid(): Boolean = sn.isNotEmpty() && meaning.isNotEmpty()
}

class StrongNumberManager(private val strongNumberRepository: StrongNumberRepository) {
    suspend fun readStrongNumber(strongNumber: String): StrongNumber = strongNumberRepository.readStrongNumber(strongNumber)

    suspend fun readStrongNumber(verseIndex: VerseIndex): List<StrongNumber> = strongNumberRepository.readStrongNumber(verseIndex)

    suspend fun readVerseIndexes(strongNumber: String): List<VerseIndex> = strongNumberRepository.readVerseIndexes(strongNumber)

    fun download(): Flow<Int> = strongNumberRepository.download()
}
