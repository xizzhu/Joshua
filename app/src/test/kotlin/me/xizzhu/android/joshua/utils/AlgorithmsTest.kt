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

package me.xizzhu.android.joshua.utils

import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AlgorithmsTest : BaseUnitTest() {
    @Test
    fun testMergeSort() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                mergeSort(listOf(1, 2, 3, 4, 5), listOf(6, 7, 8, 9, 10), { a, b -> a - b }))

        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                mergeSort(listOf(6, 7, 8, 9, 10), listOf(1, 2, 3, 4, 5), { a, b -> a - b }))

        assertEquals(listOf(1, 2, 3, 4, 5, 7, 8, 10, 11, 15),
                mergeSort(listOf(1, 3, 5, 7, 11), listOf(2, 4, 8, 10, 15), { a, b -> a - b }))

        assertEquals(listOf(1, 2, 3, 4, 5, 7, 8, 10, 11, 15),
                mergeSort(listOf(2, 4, 8, 10, 15), listOf(1, 3, 5, 7, 11), { a, b -> a - b }))

        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7),
                mergeSort(listOf(1, 3, 4, 5, 7), listOf(2, 3, 4, 5, 6), { a, b -> a - b }))
    }
}
