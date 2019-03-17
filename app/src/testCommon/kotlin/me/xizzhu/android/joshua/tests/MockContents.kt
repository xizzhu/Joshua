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

    const val cuvShortName = "中文和合本"
    val cuvTranslationInfo = TranslationInfo(cuvShortName, "中文和合本（简体）", "zh_cn", 1781521L, false)
    const val cuvTranslationInfoJson = "{\"translations\":[{\"name\":\"中文和合本（简体）\",\"shortName\":\"中文和合本\",\"language\":\"zh_cn\",\"size\":1781521}]}"
    val cuvRemoteTranslationInfo = RemoteTranslationInfo(cuvShortName, "中文和合本（简体）", "zh_cn", 1781521L)

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
