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

package me.xizzhu.android.joshua.preview

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.utils.firstNotEmpty
import java.util.ArrayList

class PreviewViewData(val settings: Settings, val title: String, val items: List<BaseItem>, val currentPosition: Int)

suspend fun loadPreviewV2(
        bibleReadingManager: BibleReadingManager,
        settingsManager: SettingsManager,
        verseIndex: VerseIndex,
        converter: List<Verse>.() -> List<BaseItem>
): Result<PreviewViewData> = runCatching {
    if (!verseIndex.isValid()) {
        throw IllegalArgumentException("Verse index [$verseIndex] is invalid")
    }

    val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
    PreviewViewData(
            settings = settingsManager.settings().first(),
            title = "${bibleReadingManager.readBookShortNames(currentTranslation)[verseIndex.bookIndex]}, ${verseIndex.chapterIndex + 1}",
            items = converter(bibleReadingManager.readVerses(currentTranslation, verseIndex.bookIndex, verseIndex.chapterIndex)),
            currentPosition = verseIndex.verseIndex
    )
}

fun loadPreview(
        bibleReadingManager: BibleReadingManager,
        settingsManager: SettingsManager,
        verseIndex: VerseIndex,
        converter: List<Verse>.() -> List<BaseItem>
): Flow<BaseViewModel.ViewData<PreviewViewData>> = viewData {
    if (!verseIndex.isValid()) {
        throw IllegalArgumentException("Verse index [$verseIndex] is invalid")
    }

    val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
    PreviewViewData(
            settings = settingsManager.settings().first(),
            title = "${bibleReadingManager.readBookShortNames(currentTranslation)[verseIndex.bookIndex]}, ${verseIndex.chapterIndex + 1}",
            items = converter(bibleReadingManager.readVerses(currentTranslation, verseIndex.bookIndex, verseIndex.chapterIndex)),
            currentPosition = verseIndex.verseIndex
    )
}

fun toVersePreviewItems(verses: List<Verse>): List<VersePreviewItem> {
    val items = ArrayList<VersePreviewItem>(verses.size)

    val verseIterator = verses.iterator()
    var verse: Verse? = null
    while (verse != null || verseIterator.hasNext()) {
        verse = verse ?: verseIterator.next()

        val (nextVerse, followingEmptyVerseCount) = verseIterator.nextNonEmpty(verse)

        items.add(VersePreviewItem(verse, followingEmptyVerseCount))

        verse = nextVerse
    }

    return items
}

// skips the empty verses
fun Iterator<Verse>.nextNonEmpty(current: Verse): Pair<Verse?, Int> {
    var nextVerse: Verse? = null
    while (hasNext()) {
        nextVerse = next()
        if (nextVerse.text.text.isEmpty()) {
            nextVerse = null
        } else {
            break
        }
    }

    val followingEmptyVerseCount = nextVerse
            ?.let { it.verseIndex.verseIndex - 1 - current.verseIndex.verseIndex }
            ?: 0

    return Pair(nextVerse, followingEmptyVerseCount)
}
