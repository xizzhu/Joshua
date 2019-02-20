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

import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex

const val cuvTranslationShortName = "中文和合本"
val cuvBookNames = arrayListOf("创世记", "出埃及记", "利未记", "民数记", "申命记")
val cuvVerses = arrayListOf(
        Verse(VerseIndex(0, 0, 0), cuvTranslationShortName, "起初神创造天地。"),
        Verse(VerseIndex(0, 0, 1), cuvTranslationShortName, "地是空虚混沌。渊面黑暗。神的灵运行在水面上。"),
        Verse(VerseIndex(0, 0, 2), cuvTranslationShortName, "神说，要有光，就有了光。"),
        Verse(VerseIndex(0, 0, 3), cuvTranslationShortName, "神看光是好的，就把光暗分开了。"),
        Verse(VerseIndex(0, 0, 4), cuvTranslationShortName, "神称光为昼，称暗为夜。有晚上，有早晨，这是头一日。"),
        Verse(VerseIndex(0, 0, 5), cuvTranslationShortName, "神说，诸水之间要有空气，将水分为上下。"),
        Verse(VerseIndex(0, 0, 6), cuvTranslationShortName, "神就造出空气，将空气以下的水，空气以上的水分开了。事就这样成了。"),
        Verse(VerseIndex(0, 0, 7), cuvTranslationShortName, "神称空气为天。有晚上，有早晨，是第二日。"),
        Verse(VerseIndex(0, 0, 8), cuvTranslationShortName, "神说，天下的水要聚在一处，使旱地露出来。事就这样成了。"),
        Verse(VerseIndex(0, 0, 9), cuvTranslationShortName, "神称旱地为地，称水的聚处为海。神看着是好的。")
)

const val kjvTranslationShortName = "KJV"
val kjvBookNames = arrayListOf("Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy")
val kjvVerses = arrayListOf(
        Verse(VerseIndex(0, 0, 0), cuvTranslationShortName, "In the beginning God created the heaven and the earth."),
        Verse(VerseIndex(0, 0, 1), cuvTranslationShortName, "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."),
        Verse(VerseIndex(0, 0, 2), cuvTranslationShortName, "And God said, Let there be light: and there was light."),
        Verse(VerseIndex(0, 0, 3), cuvTranslationShortName, "And God saw the light, that it was good: and God divided the light from the darkness."),
        Verse(VerseIndex(0, 0, 4), cuvTranslationShortName, "And God called the light Day, and the darkness he called Night. And the evening and the morning were the first day."),
        Verse(VerseIndex(0, 0, 5), cuvTranslationShortName, "And God said, Let there be a firmament in the midst of the waters, and let it divide the waters from the waters."),
        Verse(VerseIndex(0, 0, 6), cuvTranslationShortName, "And God made the firmament, and divided the waters which were under the firmament from the waters which were above the firmament: and it was so."),
        Verse(VerseIndex(0, 0, 7), cuvTranslationShortName, "And God called the firmament Heaven. And the evening and the morning were the second day."),
        Verse(VerseIndex(0, 0, 8), cuvTranslationShortName, "And God said, Let the waters under the heaven be gathered together unto one place, and let the dry land appear: and it was so."),
        Verse(VerseIndex(0, 0, 9), cuvTranslationShortName, "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.")
)

fun prepareBookNames(translationShortName: String, bookNames: ArrayList<String>) {
    runBlocking {
        createLocalStorage().bookNamesDao.save(translationShortName, bookNames)
    }
}

fun prepareVerses(translationShortName: String, verses: ArrayList<Verse>) {
    runBlocking {
        val localStorage = createLocalStorage()
        localStorage.translationDao.createTable(translationShortName)

        val verseTexts = ArrayList<String>()
        for (verse in verses) {
            verseTexts.add(verse.text)
        }
        localStorage.translationDao.save(translationShortName, verses[0].verseIndex.bookIndex,
                verses[0].verseIndex.bookIndex, verseTexts)
    }
}
