/*
 * Copyright (C) 2021 Xizhi Zhu
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

import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo

object MockContents {
    const val kjvShortName = "KJV"
    val kjvTranslationInfo = TranslationInfo(kjvShortName, "Authorized King James", "en_gb", 1860978L, false)
    val kjvDownloadedTranslationInfo = TranslationInfo(kjvShortName, "Authorized King James", "en_gb", 1860978L, true)
    val kjvBookNames: List<String> = listOf(
            "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua",
            "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings",
            "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job",
            "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah",
            "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
            "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah",
            "Haggai", "Zechariah", "Malachi", "St. Matthew", "St. Mark", "St. Luke",
            "St. John", "The Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians",
            "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy",
            "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter",
            "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"
    )
    val kjvBookShortNames: List<String> = listOf(
            "Gen.", "Ex.", "Lev.", "Num.", "Deut.", "Josh.",
            "Judg.", "Ruth", "1 Sam.", "2 Sam.", "1 Kings", "2 Kings",
            "1 Chron.", "2 Chron.", "Ezra", "Neh.", "Est.", "Job",
            "Ps.", "Prov", "Eccles.", "Song", "Isa.", "Jer.",
            "Lam.", "Ezek.", "Dan.", "Hos.", "Joel", "Amos",
            "Obad.", "Jonah", "Mic.", "Nah.", "Hab.", "Zeph.",
            "Hag.", "Zech.", "Mal.", "Matt.", "Mark", "Luke",
            "John", "Acts", "Rom.", "1 Cor.", "2 Cor.", "Gal.",
            "Eph.", "Phil.", "Col.", "1 Thess.", "2 Thess.", "1 Tim.",
            "2 Tim.", "Titus", "Philem.", "Heb.", "James", "1 Pet.",
            "2 Pet.", "1 John", "2 John", "3 John", "Jude", "Rev"
    )
    val kjvVerses: List<Verse> = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "In the beginning God created the heaven and the earth."), emptyList()),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 2), Verse.Text(kjvShortName, "And God said, Let there be light: and there was light."), emptyList()),
            Verse(VerseIndex(0, 0, 3), Verse.Text(kjvShortName, "And God saw the light, that it was good: and God divided the light from the darkness."), emptyList()),
            Verse(VerseIndex(0, 0, 4), Verse.Text(kjvShortName, "And God called the light Day, and the darkness he called Night. And the evening and the morning were the first day."), emptyList()),
            Verse(VerseIndex(0, 0, 5), Verse.Text(kjvShortName, "And God said, Let there be a firmament in the midst of the waters, and let it divide the waters from the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 6), Verse.Text(kjvShortName, "And God made the firmament, and divided the waters which were under the firmament from the waters which were above the firmament: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 7), Verse.Text(kjvShortName, "And God called the firmament Heaven. And the evening and the morning were the second day."), emptyList()),
            Verse(VerseIndex(0, 0, 8), Verse.Text(kjvShortName, "And God said, Let the waters under the heaven be gathered together unto one place, and let the dry land appear: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 9), Verse.Text(kjvShortName, "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good."), emptyList())
    )
    val kjvExtraVerses: List<Verse> = listOf(
            Verse(VerseIndex(0, 9, 9), Verse.Text(kjvShortName, "And the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar."), emptyList()),
            Verse(VerseIndex(1, 22, 18), Verse.Text(kjvShortName, "The first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk."), emptyList())
    )
    const val kjvTranslationInfoJson = "{\"translations\":[{\"name\":\"Authorized King James\",\"shortName\":\"KJV\",\"language\":\"en_gb\",\"size\":1860978}]}"
    val kjvRemoteTranslationInfo = RemoteTranslationInfo(kjvShortName, "Authorized King James", "en_gb", 1860978L)

    const val bbeShortName = "BBE"
    val bbeTranslationInfo = TranslationInfo(bbeShortName, "Basic English", "en_us", 1869733L, false)
    val bbeDownloadedTranslationInfo = TranslationInfo(bbeShortName, "Basic English", "en_us", 1869733L, true)
    val bbeBookNames: List<String> = listOf("Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy")
    val bbeBookShortNames: List<String> = listOf("Gen.", "Ex.", "Lev.", "Num.", "Deut.")
    val bbeVerses: List<Verse> = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "At the first God made the heaven and the earth."), emptyList()),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "And the earth was waste and without form; and it was dark on the face of the deep: and the Spirit of God was moving on the face of the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 2), Verse.Text(kjvShortName, "And God said, Let there be light: and there was light."), emptyList()),
            Verse(VerseIndex(0, 0, 3), Verse.Text(kjvShortName, "And God, looking on the light, saw that it was good: and God made a division between the light and the dark,"), emptyList()),
            Verse(VerseIndex(0, 0, 4), Verse.Text(kjvShortName, "Naming the light, Day, and the dark, Night. And there was evening and there was morning, the first day."), emptyList()),
            Verse(VerseIndex(0, 0, 5), Verse.Text(kjvShortName, "And God said, Let there be a solid arch stretching over the waters, parting the waters from the waters."), emptyList()),
            Verse(VerseIndex(0, 0, 6), Verse.Text(kjvShortName, "And God made the arch for a division between the waters which were under the arch and those which were over it: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 7), Verse.Text(kjvShortName, "And God gave the arch the name of Heaven. And there was evening and there was morning, the second day."), emptyList()),
            Verse(VerseIndex(0, 0, 8), Verse.Text(kjvShortName, "And God said, Let the waters under the heaven come together in one place, and let the dry land be seen: and it was so."), emptyList()),
            Verse(VerseIndex(0, 0, 9), Verse.Text(kjvShortName, "And God gave the dry land the name of Earth; and the waters together in their place were named Seas: and God saw that it was good."), emptyList())
    )

    const val cuvShortName = "中文和合本"
    val cuvTranslationInfo = TranslationInfo(cuvShortName, "中文和合本（简体）", "zh_cn", 1781521L, false)
    val cuvDownloadedTranslationInfo = TranslationInfo(cuvShortName, "中文和合本（简体）", "zh_cn", 1781521L, true)
    val cuvBookNames: List<String> = listOf("创世记", "出埃及记", "利未记", "民数记", "申命记")
    val cuvBookShortNames: List<String> = listOf("创", "出", "利", "民", "申")
    val cuvVerses: List<Verse> = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(cuvShortName, "起初神创造天地。"), emptyList()),
            Verse(VerseIndex(0, 0, 1), Verse.Text(cuvShortName, "地是空虚混沌。渊面黑暗。神的灵运行在水面上。"), emptyList()),
            Verse(VerseIndex(0, 0, 2), Verse.Text(cuvShortName, "神说，要有光，就有了光。"), emptyList()),
            Verse(VerseIndex(0, 0, 3), Verse.Text(cuvShortName, "神看光是好的，就把光暗分开了。"), emptyList()),
            Verse(VerseIndex(0, 0, 4), Verse.Text(cuvShortName, "神称光为昼，称暗为夜。有晚上，有早晨，这是头一日。"), emptyList()),
            Verse(VerseIndex(0, 0, 5), Verse.Text(cuvShortName, "神说，诸水之间要有空气，将水分为上下。"), emptyList()),
            Verse(VerseIndex(0, 0, 6), Verse.Text(cuvShortName, "神就造出空气，将空气以下的水，空气以上的水分开了。事就这样成了。"), emptyList()),
            Verse(VerseIndex(0, 0, 7), Verse.Text(cuvShortName, "神称空气为天。有晚上，有早晨，是第二日。"), emptyList()),
            Verse(VerseIndex(0, 0, 8), Verse.Text(cuvShortName, "神说，天下的水要聚在一处，使旱地露出来。事就这样成了。"), emptyList()),
            Verse(VerseIndex(0, 0, 9), Verse.Text(cuvShortName, "神称旱地为地，称水的聚处为海。神看着是好的。"), emptyList())
    )
    const val cuvTranslationInfoJson = "{\"translations\":[{\"name\":\"中文和合本（简体）\",\"shortName\":\"中文和合本\",\"language\":\"zh_cn\",\"size\":1781521}]}"
    val cuvRemoteTranslationInfo = RemoteTranslationInfo(cuvShortName, "中文和合本（简体）", "zh_cn", 1781521L)

    const val msgShortName = "MSG"
    val msgTranslationInfo = TranslationInfo(msgShortName, "The Message Bible", "en_us", 2066687L, false)
    val msgBookNames: List<String> = listOf("Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy")
    val msgBookShortNames: List<String> = listOf("Gen.", "Ex.", "Lev.", "Num.", "Deut.")
    val msgVerses: List<Verse> = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(msgShortName, "First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss."), emptyList()),
            Verse(VerseIndex(0, 0, 1), Verse.Text(msgShortName, ""), emptyList()),
            Verse(VerseIndex(0, 0, 2), Verse.Text(msgShortName, "God spoke: \"Light!\"\nAnd light appeared.\nGod saw that light was good\nand separated light from dark.\nGod named the light Day,\nhe named the dark Night.\nIt was evening, it was morning—\nDay One."), emptyList()),
            Verse(VerseIndex(0, 0, 3), Verse.Text(msgShortName, ""), emptyList()),
            Verse(VerseIndex(0, 0, 4), Verse.Text(msgShortName, ""), emptyList()),
            Verse(VerseIndex(0, 0, 5), Verse.Text(msgShortName, "God spoke: \"Sky! In the middle of the waters;\nseparate water from water!\"\nGod made sky.\nHe separated the water under sky\nfrom the water above sky.\nAnd there it was:\nhe named sky the Heavens;\nIt was evening, it was morning—\nDay Two."), emptyList()),
            Verse(VerseIndex(0, 0, 6), Verse.Text(msgShortName, ""), emptyList()),
            Verse(VerseIndex(0, 0, 7), Verse.Text(msgShortName, ""), emptyList()),
            Verse(VerseIndex(0, 0, 8), Verse.Text(msgShortName, "God spoke: \"Separate!\nWater-beneath-Heaven, gather into one place;\nLand, appear!\"\nAnd there it was.\nGod named the land Earth.\nHe named the pooled water Ocean.\nGod saw that it was good."), emptyList())
    )

    val kjvVersesWithCuvParallel = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "In the beginning God created the heaven and the earth."), listOf(Verse.Text(cuvShortName, "起初神创造天地。"))),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."), listOf(Verse.Text(cuvShortName, "地是空虚混沌。渊面黑暗。神的灵运行在水面上。")))
    )
    val kjvVersesWithBbeCuvParallel = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(kjvShortName, "In the beginning God created the heaven and the earth."), listOf(Verse.Text(bbeShortName, "At the first God made the heaven and the earth."), Verse.Text(cuvShortName, "起初神创造天地。"))),
            Verse(VerseIndex(0, 0, 1), Verse.Text(kjvShortName, "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."), listOf(Verse.Text(bbeShortName, "And the earth was waste and without form; and it was dark on the face of the deep: and the Spirit of God was moving on the face of the waters."), Verse.Text(cuvShortName, "地是空虚混沌。渊面黑暗。神的灵运行在水面上。")))
    )
    val msgVersesWithKjvParallel = listOf(
            Verse(VerseIndex(0, 0, 0), Verse.Text(msgShortName, "First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss."), listOf(Verse.Text(kjvShortName, "In the beginning God created the heaven and the earth."))),
            Verse(VerseIndex(0, 0, 1), Verse.Text(msgShortName, ""), listOf(Verse.Text(kjvShortName, "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.")))
    )

    const val kjvCuvTranslationInfoJson = "{\"translations\":[{\"name\":\"Authorized King James\",\"shortName\":\"KJV\",\"language\":\"en_gb\",\"size\":1860978},{\"name\":\"中文和合本（简体）\",\"shortName\":\"中文和合本\",\"language\":\"zh_cn\",\"size\":1781521}]}"

    val strongNumberIndex = mapOf(
            VerseIndex(0, 0, 0) to listOf("H7225", "H1254", "H430", "H853", "H8064", "H776"),
            VerseIndex(39, 0, 0) to listOf("G976", "G1078", "G2424", "G5547", "G5207", "G1138", "G5207", "G11")
    )
    val strongNumberReverseIndex = mapOf(
            "H7225" to listOf(VerseIndex(0, 0, 0), VerseIndex(0, 9, 9)),
            "H1254" to listOf(VerseIndex(0, 0, 0)),
            "G976" to listOf(VerseIndex(39, 0, 0)),
            "G1078" to listOf(VerseIndex(39, 0, 0))
    )
    val strongNumberWords = mapOf(
            "H7225" to "beginning, chief(-est), first(-fruits, part, time), principal thing.",
            "H1254" to "choose, create (creator), cut down, dispatch, do, make (fat).",
            "H430" to "angels, [idiom] exceeding, God (gods) (-dess, -ly), [idiom] (very) great, judges, [idiom] mighty.",
            "H853" to "(as such unrepresented in English).",
            "H8064" to "mantle",
            "H776" to "[idiom] common, country, earth, field, ground, land, [idiom] natins, way, [phrase] wilderness, world.",
            "G976" to "book",
            "G1078" to "generation, nature(-ral)",
            "G2424" to "Jesus",
            "G5547" to "Christ",
            "G5207" to "child, foal, son",
            "G1138" to "David",
            "G11" to "Abraham"
    )
    val strongNumber = mapOf(
            VerseIndex(0, 0, 0) to listOf(
                    StrongNumber("H7225", strongNumberWords.getValue("H7225")),
                    StrongNumber("H1254", strongNumberWords.getValue("H1254")),
                    StrongNumber("H430", strongNumberWords.getValue("H430")),
                    StrongNumber("H853", strongNumberWords.getValue("H853")),
                    StrongNumber("H8064", strongNumberWords.getValue("H8064")),
                    StrongNumber("H776", strongNumberWords.getValue("H776"))
            ),
            VerseIndex(39, 0, 0) to listOf(
                    StrongNumber("G976", strongNumberWords.getValue("G976")),
                    StrongNumber("G1078", strongNumberWords.getValue("G1078")),
                    StrongNumber("G2424", strongNumberWords.getValue("G2424")),
                    StrongNumber("G5547", strongNumberWords.getValue("G5547")),
                    StrongNumber("G5207", strongNumberWords.getValue("G5207")),
                    StrongNumber("G1138", strongNumberWords.getValue("G1138")),
                    StrongNumber("G5207", strongNumberWords.getValue("G5207")),
                    StrongNumber("G11", strongNumberWords.getValue("G11"))
            )
    )
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
