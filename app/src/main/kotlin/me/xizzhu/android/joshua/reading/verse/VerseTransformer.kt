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

package me.xizzhu.android.joshua.reading.verse

import me.xizzhu.android.joshua.core.*

fun List<Verse>.toSimpleVerseItems(highlights: List<Highlight>): List<SimpleVerseItem> {
    val items = ArrayList<SimpleVerseItem>(size)

    val verseIterator = iterator()
    var verse: Verse? = null
    val highlightIterator = highlights.iterator()
    var highlight: Highlight? = null
    while (verse != null || verseIterator.hasNext()) {
        verse = verse ?: verseIterator.next()

        val (nextVerse, parallel, followingEmptyVerseCount) = verseIterator.nextNonEmpty(verse)

        val verseIndex = verse.verseIndex.verseIndex
        if (highlight == null || highlight.verseIndex.verseIndex < verseIndex) {
            while (highlightIterator.hasNext()) {
                highlight = highlightIterator.next()
                if (highlight.verseIndex.verseIndex >= verseIndex) {
                    break
                }
            }
        }
        val highlightColor = highlight
                ?.let { if (it.verseIndex.verseIndex == verseIndex) it.color else Highlight.COLOR_NONE }
                ?: Highlight.COLOR_NONE

        items.add(SimpleVerseItem(verse.transform(parallel), size, followingEmptyVerseCount, highlightColor))

        verse = nextVerse
    }

    return items
}

// skips the empty verses, and concatenates the parallels
private fun Iterator<Verse>.nextNonEmpty(current: Verse): Triple<Verse?, Array<StringBuilder>, Int> {
    val parallel = Array(current.parallel.size) { StringBuilder() }.append(current.parallel)

    var nextVerse: Verse? = null
    while (hasNext()) {
        nextVerse = next()
        if (nextVerse.text.text.isEmpty()) {
            parallel.append(nextVerse.parallel)
            nextVerse = null
        } else {
            break
        }
    }

    val followingEmptyVerseCount = nextVerse
            ?.let { it.verseIndex.verseIndex - 1 - current.verseIndex.verseIndex }
            ?: 0

    return Triple(nextVerse, parallel, followingEmptyVerseCount)
}

private fun Array<StringBuilder>.append(texts: List<Verse.Text>): Array<StringBuilder> {
    texts.forEachIndexed { index, text ->
        with(get(index)) {
            if (isNotEmpty()) append(' ')
            append(text.text)
        }
    }
    return this
}

private fun Verse.transform(concatenatedParallel: Array<StringBuilder>): Verse {
    if (parallel.isEmpty() || concatenatedParallel.isEmpty()) return this

    val parallelTexts = ArrayList<Verse.Text>(concatenatedParallel.size)
    parallel.forEachIndexed { index, text ->
        parallelTexts.add(Verse.Text(text.translationShortName, concatenatedParallel[index].toString()))
    }
    return copy(parallel = parallelTexts)
}

fun List<Verse>.toVerseItems(bookmarks: List<Bookmark>, highlights: List<Highlight>, notes: List<Note>): List<VerseItem> {
    val items = ArrayList<VerseItem>(size)

    val verseIterator = iterator()
    var verse: Verse? = null
    val bookmarkIterator = bookmarks.iterator()
    var bookmark: Bookmark? = null
    val highlightIterator = highlights.iterator()
    var highlight: Highlight? = null
    val noteIterator = notes.iterator()
    var note: Note? = null
    while (verse != null || verseIterator.hasNext()) {
        verse = verse ?: verseIterator.next()

        val (nextVerse, parallel, followingEmptyVerseCount) = verseIterator.nextNonEmpty(verse)

        val verseIndex = verse.verseIndex.verseIndex
        if (bookmark == null || bookmark.verseIndex.verseIndex < verseIndex) {
            while (bookmarkIterator.hasNext()) {
                bookmark = bookmarkIterator.next()
                if (bookmark.verseIndex.verseIndex >= verseIndex) {
                    break
                }
            }
        }
        val hasBookmark = bookmark?.let { it.verseIndex.verseIndex == verseIndex } ?: false

        if (highlight == null || highlight.verseIndex.verseIndex < verseIndex) {
            while (highlightIterator.hasNext()) {
                highlight = highlightIterator.next()
                if (highlight.verseIndex.verseIndex >= verseIndex) {
                    break
                }
            }
        }
        val highlightColor = highlight
                ?.let { if (it.verseIndex.verseIndex == verseIndex) it.color else Highlight.COLOR_NONE }
                ?: Highlight.COLOR_NONE

        if (note == null || note.verseIndex.verseIndex < verseIndex) {
            while (noteIterator.hasNext()) {
                note = noteIterator.next()
                if (note.verseIndex.verseIndex >= verseIndex) {
                    break
                }
            }
        }
        val hasNote = note?.let { it.verseIndex.verseIndex == verseIndex } ?: false

        items.add(VerseItem(
                verse = verse.transform(parallel),
                followingEmptyVerseCount = followingEmptyVerseCount,
                hasBookmark = hasBookmark,
                highlightColor = highlightColor,
                hasNote = hasNote
        ))

        verse = nextVerse
    }

    return items
}

fun Collection<Verse>.toStringForSharing(bookName: String, consolidateVerses: Boolean): String {
    val sortedVerses = sortedBy { verse ->
        val verseIndex = verse.verseIndex
        verseIndex.bookIndex * 100000 + verseIndex.chapterIndex * 1000 + verseIndex.verseIndex
    }

    if (!consolidateVerses || size == 1) {
        return StringBuilder().apply {
            sortedVerses.forEach { verse -> append(verse, bookName) }
        }.toString()
    }

    // format (without parallel):
    // <book name> <chapter index>:<start verse index>-<end verse index>
    // <verse text>
    // <book name> <chapter index>:<verse index>
    // <verse text>
    //
    // format (with parallel):
    // <book name> <chapter index>:<start verse index>-<end verse index>
    // <primary translation>: <verse text>
    // <parallel translation 1>: <verse text>
    // <parallel translation 2>: <verse text>
    // <book name> <chapter index>:<verse index>
    // <primary translation>: <verse text>
    // <parallel translation 1>: <verse text>
    // <parallel translation 2>: <verse text>

    // step 1: find all start - end verse index pairs
    val verseGroups = arrayListOf<Pair<Int, Int>>()
    sortedVerses.forEach { verse ->
        val lastVerseIndexPair = verseGroups.lastOrNull()
        if (lastVerseIndexPair != null && lastVerseIndexPair.second + 1 == verse.verseIndex.verseIndex) {
            verseGroups[verseGroups.size - 1] = lastVerseIndexPair.copy(second = verse.verseIndex.verseIndex)
        } else {
            verseGroups.add(Pair(verse.verseIndex.verseIndex, verse.verseIndex.verseIndex))
        }
    }

    // step 2: build the string for sharing
    val stringBuilder = StringBuilder()

    var currentVerseGroupIndex = 0
    val parallelVersesBuilder = Array(sortedVerses.first().parallel.size) { StringBuilder() }
    sortedVerses.forEach { verse ->
        val currentVerseGroup = verseGroups[currentVerseGroupIndex]

        // start of the verse group
        if (verse.verseIndex.verseIndex == currentVerseGroup.first) {
            if (stringBuilder.isNotEmpty()) stringBuilder.append("\n\n")

            stringBuilder.append(bookName).append(' ')
                    .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1)
            if (currentVerseGroup.first < currentVerseGroup.second) {
                stringBuilder.append('-').append(currentVerseGroup.second + 1)
            }
            stringBuilder.append('\n')

            if (verse.parallel.isNotEmpty()) {
                stringBuilder.append(verse.text.translationShortName).append(": ")

                verse.parallel.forEachIndexed { index, parallel ->
                    with(parallelVersesBuilder[index]) {
                        clear()
                        append(parallel.translationShortName).append(": ")
                    }
                }
            }
        }

        if (verse.verseIndex.verseIndex > currentVerseGroup.first) stringBuilder.append(' ')
        stringBuilder.append(verse.text.text)

        verse.parallel.forEachIndexed { index, parallel ->
            with(parallelVersesBuilder[index]) {
                if (verse.verseIndex.verseIndex > currentVerseGroup.first) append(' ')
                append(parallel.text)
            }
        }

        // end of the verse group
        if (verse.verseIndex.verseIndex == currentVerseGroup.second) {
            parallelVersesBuilder.forEach { parallelBuilder ->
                stringBuilder.append('\n').append(parallelBuilder)
            }

            currentVerseGroupIndex++
        }
    }

    return stringBuilder.toString()
}

private fun StringBuilder.append(verse: Verse, bookName: String): StringBuilder {
    if (isNotEmpty()) append('\n')

    if (verse.parallel.isEmpty()) {
        // format: <book name> <chapter index>:<verse index> <text>
        append(bookName).append(' ')
                .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1).append(' ')
                .append(verse.text.text)
    } else {
        // format:
        // <book name> <chapter verseIndex>:<verse verseIndex>
        // <primary translation>: <verse text>
        // <parallel translation 1>: <verse text>
        // <parallel translation 2>: <verse text>
        append(bookName).append(' ')
                .append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1).append('\n')
                .append(verse.text.translationShortName).append(": ").append(verse.text.text).append('\n')
        verse.parallel.forEach { text -> append(text.translationShortName).append(": ").append(text.text).append('\n') }
        setLength(length - 1) // remove the appended space
    }
    return this
}

fun Verse.toStringForSharing(bookName: String): String = StringBuilder().append(this, bookName).toString()
