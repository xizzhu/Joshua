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
import me.xizzhu.android.joshua.core.repository.CrossReferencesRepository

data class CrossReferences(val verseIndex: VerseIndex, val referenced: List<VerseIndex>) {
    companion object {
        val INVALID = CrossReferences(VerseIndex.INVALID, emptyList())
    }

    fun isValid(): Boolean = verseIndex.isValid() && referenced.isNotEmpty() && referenced.all { it.isValid() }
}

class CrossReferencesManager(private val crossReferencesRepository: CrossReferencesRepository) {
    suspend fun readCrossReferences(verseIndex: VerseIndex): CrossReferences = crossReferencesRepository.readCrossReferences(verseIndex)

    fun download(): Flow<Int> = crossReferencesRepository.download()
}
