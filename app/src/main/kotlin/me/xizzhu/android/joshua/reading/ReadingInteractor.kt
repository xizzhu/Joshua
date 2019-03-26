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
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.utils.BaseSettingsInteractor


class ReadingInteractor(private val readingActivity: ReadingActivity,
                        private val navigator: Navigator,
                        private val bibleReadingManager: BibleReadingManager,
                        private val readingProgressManager: ReadingProgressManager,
                        private val translationManager: TranslationManager,
                        settingsManager: SettingsManager) : BaseSettingsInteractor(settingsManager) {
    private val verseDetailOpenState: ConflatedBroadcastChannel<VerseIndex> = ConflatedBroadcastChannel()

    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> =
            translationManager.observeDownloadedTranslations()

    fun observeCurrentTranslation(): ReceiveChannel<String> = bibleReadingManager.observeCurrentTranslation()

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun observeParallelTranslations(): ReceiveChannel<List<String>> = bibleReadingManager.observeParallelTranslations()

    fun observeVerseDetailOpenState(): ReceiveChannel<VerseIndex> = verseDetailOpenState.openSubscription()

    suspend fun openVerseDetail(verseIndex: VerseIndex) {
        verseDetailOpenState.send(verseIndex)
    }

    /**
     * @return true if verse detail view was open, or false otherwise
     * */
    suspend fun closeVerseDetail(): Boolean {
        if (verseDetailOpenState.valueOrNull?.isValid() == true) {
            verseDetailOpenState.send(VerseIndex.INVALID)
            return true
        }
        return false
    }

    suspend fun requestParallelTranslation(translationShortName: String) {
        bibleReadingManager.requestParallelTranslation(translationShortName)
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
        bibleReadingManager.removeParallelTranslation(translationShortName)
    }

    fun observeCurrentVerseIndex(): ReceiveChannel<VerseIndex> = bibleReadingManager.observeCurrentVerseIndex()

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, bookIndex, chapterIndex)

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex)

    suspend fun readVerse(translationShortName: String, verseIndex: VerseIndex): Verse =
            bibleReadingManager.readVerse(translationShortName, verseIndex)

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookNames(translationShortName)

    fun openSearch() {
        navigator.navigate(readingActivity, Navigator.SCREEN_SEARCH)
    }

    fun openTranslationManagement() {
        navigator.navigate(readingActivity, Navigator.SCREEN_TRANSLATION_MANAGEMENT)
    }

    fun openReadingProgress() {
        navigator.navigate(readingActivity, Navigator.SCREEN_READING_PROGRESS)
    }

    fun openSettings() {
        navigator.navigate(readingActivity, Navigator.SCREEN_SETTINGS)
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
        val chooseIntent = createChooserForSharing(readingActivity.packageManager, readingActivity.resources,
                "com.facebook.katana", verses.toStringForSharing())
        if (chooseIntent != null) {
            readingActivity.startActivity(chooseIntent)
            return true
        }
        return false
    }

    suspend fun startTrackingReadingProgress() {
        readingProgressManager.startTracking()
    }

    suspend fun stopTrackingReadingProgress() {
        readingProgressManager.stopTracking()
    }
}

@VisibleForTesting
fun createChooserForSharing(packageManager: PackageManager, resources: Resources,
                            packageToExclude: String, text: String): Intent? {
    val sendIntent = Intent(Intent.ACTION_SEND).setType("text/plain")
    val resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0)
    if (resolveInfoList.isEmpty()) {
        return null
    }

    val filteredIntents = ArrayList<Intent>(resolveInfoList.size)
    for (resolveInfo in resolveInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        if (packageToExclude != packageName) {
            val labeledIntent = LabeledIntent(packageName, resolveInfo.loadLabel(packageManager), resolveInfo.iconResource)
            labeledIntent.setAction(Intent.ACTION_SEND).setPackage(packageName)
                    .setComponent(ComponentName(packageName, resolveInfo.activityInfo.name))
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, text)
            filteredIntents.add(labeledIntent)
        }
    }
    if (filteredIntents.isEmpty()) {
        return null
    }

    val chooserIntent = Intent.createChooser(filteredIntents.removeAt(0),
            resources.getText(R.string.text_share_with))
    val array = arrayOfNulls<Parcelable>(filteredIntents.size)
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, filteredIntents.toArray(array))
    return chooserIntent
}
