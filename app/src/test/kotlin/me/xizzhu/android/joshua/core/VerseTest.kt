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

import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VerseTest : BaseUnitTest() {
    @Test
    fun `test valid Verse_Text`() {
        assertTrue(Verse.Text("translationShortName", "").isValid())
        assertTrue(Verse.Text("translationShortName", "some random text").isValid())
    }

    @Test
    fun `test invalid Verse_Text`() {
        assertFalse(Verse.Text("", "").isValid())
        assertFalse(Verse.Text("", "some random text").isValid())
    }

    @Test
    fun `test valid Verse`() {
        assertTrue(
                Verse(
                        VerseIndex(0, 0, 0),
                        Verse.Text("translationShortName", ""),
                        emptyList()
                ).isValid()
        )

        assertTrue(
                Verse(
                        VerseIndex(0, 0, 0),
                        Verse.Text("translationShortName", ""),
                        listOf(Verse.Text("parallelTranslation", ""))
                ).isValid()
        )

        assertTrue(
                Verse(
                        VerseIndex(0, 0, 0),
                        Verse.Text("translationShortName", ""),
                        listOf(
                                Verse.Text("parallelTranslation", ""),
                                Verse.Text("anotherParallelTranslation", "")
                        )
                ).isValid()
        )
    }

    @Test
    fun `test invalid Verse`() {
        assertFalse(
                Verse(
                        VerseIndex.INVALID,
                        Verse.Text("translationShortName", ""),
                        emptyList()
                ).isValid()
        )

        assertFalse(
                Verse(
                        VerseIndex(0, 0, 0),
                        Verse.Text("", ""),
                        emptyList()
                ).isValid()
        )

        assertFalse(
                Verse(
                        VerseIndex(0, 0, 0),
                        Verse.Text("translationShortName", ""),
                        listOf(Verse.Text("", ""))
                ).isValid()
        )

        assertFalse(
                Verse(
                        VerseIndex(0, 0, 0),
                        Verse.Text("translationShortName", ""),
                        listOf(
                                Verse.Text("", ""),
                                Verse.Text("anotherParallelTranslation", "")
                        )
                ).isValid()
        )

        assertFalse(
                Verse(
                        VerseIndex(0, 0, 0),
                        Verse.Text("translationShortName", ""),
                        listOf(
                                Verse.Text("parallelTranslation", ""),
                                Verse.Text("", "")
                        )
                ).isValid()
        )
    }
}
