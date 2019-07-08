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

package me.xizzhu.android.joshua.tests

import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo

object MockContents {
    const val kjvShortName = "KJV"
    val kjvTranslationInfo = TranslationInfo(kjvShortName, "Authorized King James", "en_gb", 1860978L, false)
    val kjvDownloadedTranslationInfo = TranslationInfo(kjvShortName, "Authorized King James", "en_gb", 1860978L, true)
    val kjvBookNames: List<String> = arrayListOf("Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy")
    val kjvBookShortNames: List<String> = arrayListOf("Gen.", "Ex.", "Lev.", "Num.", "Deut.")
    val kjvVerses: List<Verse> = arrayListOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "Genesis", "In the beginning God created the heaven and the earth."), emptyList()),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "Genesis", "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 2), Verse.Text(kjvShortName, "Genesis", "And God said, Let there be light: and there was light."), emptyList()),
            Verse(VerseIndex(0, 0, 3), Verse.Text(kjvShortName, "Genesis", "And God saw the light, that it was good: and God divided the light from the darkness."), emptyList()),
            Verse(VerseIndex(0, 0, 4), Verse.Text(kjvShortName, "Genesis", "And God called the light Day, and the darkness he called Night. And the evening and the morning were the first day."), emptyList()),
            Verse(VerseIndex(0, 0, 5), Verse.Text(kjvShortName, "Genesis", "And God said, Let there be a firmament in the midst of the waters, and let it divide the waters from the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 6), Verse.Text(kjvShortName, "Genesis", "And God made the firmament, and divided the waters which were under the firmament from the waters which were above the firmament: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 7), Verse.Text(kjvShortName, "Genesis", "And God called the firmament Heaven. And the evening and the morning were the second day."), emptyList()),
            Verse(VerseIndex(0, 0, 8), Verse.Text(kjvShortName, "Genesis", "And God said, Let the waters under the heaven be gathered together unto one place, and let the dry land appear: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 9), Verse.Text(kjvShortName, "Genesis", "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good."), emptyList())
    )
    const val kjvTranslationInfoJson = "{\"translations\":[{\"name\":\"Authorized King James\",\"shortName\":\"KJV\",\"language\":\"en_gb\",\"size\":1860978}]}"
    val kjvRemoteTranslationInfo = RemoteTranslationInfo(kjvShortName, "Authorized King James", "en_gb", 1860978L)

    const val bbeShortName = "BBE"
    val bbeTranslationInfo = TranslationInfo(bbeShortName, "Basic English", "en_us", 1869733L, false)
    val bbeBookNames: List<String> = arrayListOf("Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy")
    val bbeBookShortNames: List<String> = arrayListOf("Gen.", "Ex.", "Lev.", "Num.", "Deut.")
    val bbeVerses: List<Verse> = arrayListOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "Genesis", "At the first God made the heaven and the earth."), emptyList()),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "Genesis", "And the earth was waste and without form; and it was dark on the face of the deep: and the Spirit of God was moving on the face of the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 2), Verse.Text(kjvShortName, "Genesis", "And God said, Let there be light: and there was light."), emptyList()),
            Verse(VerseIndex(0, 0, 3), Verse.Text(kjvShortName, "Genesis", "And God, looking on the light, saw that it was good: and God made a division between the light and the dark,"), emptyList()),
            Verse(VerseIndex(0, 0, 4), Verse.Text(kjvShortName, "Genesis", "Naming the light, Day, and the dark, Night. And there was evening and there was morning, the first day."), emptyList()),
            Verse(VerseIndex(0, 0, 5), Verse.Text(kjvShortName, "Genesis", "And God said, Let there be a solid arch stretching over the waters, parting the waters from the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 6), Verse.Text(kjvShortName, "Genesis", "And God made the arch for a division between the waters which were under the arch and those which were over it: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 7), Verse.Text(kjvShortName, "Genesis", "And God gave the arch the name of Heaven. And there was evening and there was morning, the second day."), emptyList()),
            Verse(VerseIndex(0, 0, 8), Verse.Text(kjvShortName, "Genesis", "And God said, Let the waters under the heaven come together in one place, and let the dry land be seen: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 9), Verse.Text(kjvShortName, "Genesis", "And God gave the dry land the name of Earth; and the waters together in their place were named Seas: and God saw that it was good."), emptyList())
    )

    const val cuvShortName = "中文和合本"
    val cuvTranslationInfo = TranslationInfo(cuvShortName, "中文和合本（简体）", "zh_cn", 1781521L, false)
    val cuvBookNames: List<String> = arrayListOf("创世记", "出埃及记", "利未记", "民数记", "申命记")
    val cuvBookShortNames: List<String> = arrayListOf("创", "出", "利", "民", "申")
    val cuvVerses: List<Verse> = arrayListOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(cuvShortName, "创世记", "起初神创造天地。"), emptyList()),
            Verse(VerseIndex(0, 0, 1), Verse.Text(cuvShortName, "创世记", "地是空虚混沌。渊面黑暗。神的灵运行在水面上。"), emptyList()),
            Verse(VerseIndex(0, 0, 2), Verse.Text(cuvShortName, "创世记", "神说，要有光，就有了光。"), emptyList()),
            Verse(VerseIndex(0, 0, 3), Verse.Text(cuvShortName, "创世记", "神看光是好的，就把光暗分开了。"), emptyList()),
            Verse(VerseIndex(0, 0, 4), Verse.Text(cuvShortName, "创世记", "神称光为昼，称暗为夜。有晚上，有早晨，这是头一日。"), emptyList()),
            Verse(VerseIndex(0, 0, 5), Verse.Text(cuvShortName, "创世记", "神说，诸水之间要有空气，将水分为上下。"), emptyList()),
            Verse(VerseIndex(0, 0, 6), Verse.Text(cuvShortName, "创世记", "神就造出空气，将空气以下的水，空气以上的水分开了。事就这样成了。"), emptyList()),
            Verse(VerseIndex(0, 0, 7), Verse.Text(cuvShortName, "创世记", "神称空气为天。有晚上，有早晨，是第二日。"), emptyList()),
            Verse(VerseIndex(0, 0, 8), Verse.Text(cuvShortName, "创世记", "神说，天下的水要聚在一处，使旱地露出来。事就这样成了。"), emptyList()),
            Verse(VerseIndex(0, 0, 9), Verse.Text(cuvShortName, "创世记", "神称旱地为地，称水的聚处为海。神看着是好的。"), emptyList())
    )
    const val cuvTranslationInfoJson = "{\"translations\":[{\"name\":\"中文和合本（简体）\",\"shortName\":\"中文和合本\",\"language\":\"zh_cn\",\"size\":1781521}]}"
    val cuvRemoteTranslationInfo = RemoteTranslationInfo(cuvShortName, "中文和合本（简体）", "zh_cn", 1781521L)

    val kjvVersesWithCuvParallel = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "Genesis", "In the beginning God created the heaven and the earth."), listOf(Verse.Text(cuvShortName, "创世记", "起初神创造天地。"))),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "Genesis", "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."), listOf(Verse.Text(cuvShortName, "创世记", "地是空虚混沌。渊面黑暗。神的灵运行在水面上。")))
    )
    val kjvVersesWithBbeCuvParallel = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "Genesis", "In the beginning God created the heaven and the earth."), listOf(Verse.Text(bbeShortName, "Genesis", "At the first God made the heaven and the earth."), Verse.Text(cuvShortName, "创世记", "起初神创造天地。"))),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "Genesis", "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."), listOf(Verse.Text(bbeShortName, "Genesis", "And the earth was waste and without form; and it was dark on the face of the deep: and the Spirit of God was moving on the face of the waters."), Verse.Text(cuvShortName, "创世记", "地是空虚混沌。渊面黑暗。神的灵运行在水面上。")))
    )

    const val kjvCuvTranslationInfoJson = "{\"translations\":[{\"name\":\"Authorized King James\",\"shortName\":\"KJV\",\"language\":\"en_gb\",\"size\":1860978},{\"name\":\"中文和合本（简体）\",\"shortName\":\"中文和合本\",\"language\":\"zh_cn\",\"size\":1781521}]}"
}

fun List<Verse>.toMap(): Map<Pair<Int, Int>, List<String>> {
    val results: HashMap<Pair<Int, Int>, ArrayList<String>> = HashMap()
    for (verse in this) {
        val key = Pair(verse.verseIndex.bookIndex, verse.verseIndex.chapterIndex)
        if (!results.containsKey(key)) {
            results[key] = ArrayList()
        }
        results[key]!!.add(verse.text.text)
    }
    return results
}
