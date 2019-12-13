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

fun <T> mergeSort(left: List<T>, right: List<T>, comparator: (T, T) -> Int, chooserWhenEqual: (T, T) -> T = { l, _ -> l }): List<T> {
    val result = mutableListOf<T>()

    val leftIterator = left.iterator()
    val rightIterator = right.iterator()
    var l = leftIterator.nextOrNull()
    var r = rightIterator.nextOrNull()
    while (l != null && r != null) {
        val c = comparator(l, r)
        when {
            c < 0 -> {
                result.add(l)
                l = leftIterator.nextOrNull()
            }
            c == 0 -> {
                result.add(chooserWhenEqual(l, r))
                l = leftIterator.nextOrNull()
                r = rightIterator.nextOrNull()
            }
            else -> {
                result.add(r)
                r = rightIterator.nextOrNull()
            }
        }
    }

    while (l != null) {
        result.add(l)
        l = leftIterator.nextOrNull()
    }
    while (r != null) {
        result.add(r)
        r = rightIterator.nextOrNull()
    }

    return result
}

private fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null
