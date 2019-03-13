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

package me.xizzhu.android.joshua.reading

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.view.ActionMode
import kotlinx.coroutines.channels.ReceiveChannel
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.reading.verse.toStringForSharing
import android.content.ComponentName
import android.content.pm.LabeledIntent
import me.xizzhu.android.joshua.R


class ReadingInteractor(private val readingActivity: ReadingActivity,
                        private val navigator: Navigator,
                        private val bibleReadingManager: BibleReadingManager,
                        private val translationManager: TranslationManager) {
    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> =
            translationManager.observeDownloadedTranslations()

    fun observeCurrentTranslation(): ReceiveChannel<String> = bibleReadingManager.observeCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = bibleReadingManager.observeCurrentVerseIndex()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, bookIndex, chapterIndex)

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookNames(translationShortName)

    fun openSearch() {
        navigator.navigate(readingActivity, Navigator.SCREEN_SEARCH)
    }

    fun openTranslationManagement() {
        navigator.navigate(readingActivity, Navigator.SCREEN_TRANSLATION_MANAGEMENT)
    }

    fun finish() {
        readingActivity.finish()
    }

    fun startActionMode(callback: ActionMode.Callback): ActionMode? =
            readingActivity.startSupportActionMode(callback)

    fun copyToClipBoard(verses: Collection<Verse>) {
        if (verses.isEmpty()) {
            return
        }

        val verse = verses.first()
        (readingActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                ClipData.newPlainText(verse.text.translationShortName + " " + verse.text.bookName, verses.toStringForSharing())
    }

    fun share(verses: Collection<Verse>): Boolean {
        // Facebook doesn't want us to pre-fill the message, but still captures ACTION_SEND. Therefore,
        // I have to exclude their package from being shown.
        // Rants: it's a horrible way to force developers to use their SDK.
        // ref. https://developers.facebook.com/bugs/332619626816423
        val chooseIntent = createChooserForSharing("com.facebook.katana", verses.toStringForSharing())
        if (chooseIntent != null) {
            readingActivity.startActivity(chooseIntent)
            return true
        }
        return false
    }

    private fun createChooserForSharing(packageToExclude: String, text: String): Intent? {
        val sendIntent = Intent(Intent.ACTION_SEND).setType("text/plain")
        val pm = readingActivity.packageManager
        val resolveInfoList = pm.queryIntentActivities(sendIntent, 0)
        if (resolveInfoList.isEmpty()) {
            return null
        }

        val filteredIntents = ArrayList<Intent>(resolveInfoList.size)
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageToExclude != packageName) {
                val labeledIntent = LabeledIntent(packageName, resolveInfo.loadLabel(pm), resolveInfo.iconResource)
                labeledIntent.setAction(Intent.ACTION_SEND).setPackage(packageName)
                        .setComponent(ComponentName(packageName, resolveInfo.activityInfo.name))
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, text)
                filteredIntents.add(labeledIntent)
            }
        }

        val chooserIntent = Intent.createChooser(filteredIntents.removeAt(0),
                readingActivity.getText(R.string.text_share_with))
        val extraIntents = filteredIntents.size
        if (extraIntents > 0) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, filteredIntents.toArray())
        }
        return chooserIntent
    }
}
