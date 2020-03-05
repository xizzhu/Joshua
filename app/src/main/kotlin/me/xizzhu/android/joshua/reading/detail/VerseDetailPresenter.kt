/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.reading.verse.toStringForSharing
import me.xizzhu.android.joshua.strongnumber.StrongNumberListActivity
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.logger.Log
import java.lang.StringBuilder
import kotlin.math.max

data class VerseDetailViewHolder(val verseDetailViewLayout: VerseDetailViewLayout) : ViewHolder

class VerseDetailPresenter(
        private val navigator: Navigator, readingViewModel: ReadingViewModel, readingActivity: ReadingActivity,
        coroutineScope: CoroutineScope = readingActivity.lifecycleScope
) : BaseSettingsPresenter<VerseDetailViewHolder, ReadingViewModel, ReadingActivity>(readingViewModel, readingActivity, coroutineScope) {
    private val translationComparator = TranslationInfoComparator(
            TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)

    @VisibleForTesting
    var verseDetail: VerseDetail? = null
    private var updateBookmarkJob: Job? = null
    private var updateHighlightJob: Job? = null
    private var updateNoteJob: Job? = null

    private var downloadStrongNumberJob: Job? = null
    private var downloadStrongNumberDialog: ProgressDialog? = null

    @UiThread
    override fun onBind() {
        super.onBind()

        viewHolder.verseDetailViewLayout.setListeners(
                onClicked = { close() }, onBookmarkClicked = { updateBookmark() },
                onHighlightClicked = { updateHighlight() }, onNoteUpdated = { updateNote(it) },
                onNoStrongNumberClicked = { downloadStrongNumber() }
        )
        viewHolder.verseDetailViewLayout.post {
            viewHolder.verseDetailViewLayout.hide()
        }
    }

    private fun updateBookmark() {
        updateBookmarkJob?.cancel()
        updateBookmarkJob = coroutineScope.launch {
            verseDetail?.let { detail ->
                viewModel.saveBookmark(detail.verseIndex, detail.bookmarked)
                verseDetail = detail.copy(bookmarked = !detail.bookmarked)
                viewHolder.verseDetailViewLayout.setVerseDetail(verseDetail!!)
            }
        }.also { it.invokeOnCompletion { updateBookmarkJob = null } }
    }

    private fun updateHighlight() {
        activity.dialog(R.string.text_pick_highlight_color,
                activity.resources.getStringArray(R.array.text_colors),
                max(0, Highlight.AVAILABLE_COLORS.indexOf(verseDetail?.highlightColor
                        ?: Highlight.COLOR_NONE)),
                DialogInterface.OnClickListener { dialog, which ->
                    updateHighlight(Highlight.AVAILABLE_COLORS[which])

                    dialog.dismiss()
                })
    }

    private fun updateHighlight(@Highlight.Companion.AvailableColor highlightColor: Int) {
        updateHighlightJob?.cancel()
        updateHighlightJob = coroutineScope.launch {
            verseDetail?.let { detail ->
                viewModel.saveHighlight(detail.verseIndex, highlightColor)
                verseDetail = detail.copy(highlightColor = highlightColor)
                viewHolder.verseDetailViewLayout.setVerseDetail(verseDetail!!)
            }
        }.also { it.invokeOnCompletion { updateHighlightJob = null } }
    }

    private fun updateNote(note: String) {
        updateNoteJob?.cancel()
        updateNoteJob = coroutineScope.launch {
            verseDetail?.let { detail ->
                viewModel.saveNote(detail.verseIndex, note)
                verseDetail = detail.copy(note = note)
            }
        }.also { it.invokeOnCompletion { updateNoteJob = null } }
    }

    private fun downloadStrongNumber() {
        if (downloadStrongNumberJob != null || downloadStrongNumberDialog != null) {
            // just in case the user clicks too fast
            return
        }

        downloadStrongNumberDialog = activity.progressDialog(
                R.string.dialog_downloading, 100) { downloadStrongNumberJob?.cancel() }
        downloadStrongNumberJob = viewModel.downloadStrongNumber()
                .onEach { progress ->
                    when (progress) {
                        -1 -> {
                            activity.toast(R.string.toast_downloaded)
                            verseDetail?.let {
                                verseDetail = it.copy(strongNumberItems = viewModel.readStrongNumber(it.verseIndex).toStrongNumberItems())
                                viewHolder.verseDetailViewLayout.setVerseDetail(verseDetail!!)
                            }
                        }
                        in 0 until 100 -> {
                            downloadStrongNumberDialog?.setProgress(progress)
                        }
                        else -> {
                            downloadStrongNumberDialog?.run {
                                setTitle(R.string.dialog_installing)
                                setIsIndeterminate(true)
                            }
                        }
                    }
                }.catch { e ->
                    Log.e(tag, "Failed to download Strong's numberrs", e)
                    activity.dialog(true, R.string.dialog_download_error,
                            DialogInterface.OnClickListener { _, _ -> downloadStrongNumber() })
                }.onCompletion {
                    downloadStrongNumberDialog?.dismiss()
                    downloadStrongNumberDialog = null
                    downloadStrongNumberJob = null
                }.launchIn(coroutineScope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.settings().onEach { viewHolder.verseDetailViewLayout.setSettings(it) }.launchIn(coroutineScope)
        viewModel.verseDetailRequest().onEach {
            loadVerseDetail(it.verseIndex)
            viewHolder.verseDetailViewLayout.show(it.content)
        }.launchIn(coroutineScope)
        viewModel.currentVerseIndex().onEach { close() }.launchIn(coroutineScope)
    }

    private fun loadVerseDetail(verseIndex: VerseIndex) {
        coroutineScope.launch {
            try {
                viewHolder.verseDetailViewLayout.setVerseDetail(VerseDetail.INVALID)
                verseDetail = coroutineScope {
                    val bookmarkAsync = async { viewModel.readBookmark(verseIndex) }
                    val highlightAsync = async { viewModel.readHighlight(verseIndex) }
                    val noteAsync = async { viewModel.readNote(verseIndex) }
                    val strongNumberAsync = async { viewModel.readStrongNumber(verseIndex) }
                    return@coroutineScope VerseDetail(verseIndex, buildVerseTextItems(verseIndex),
                            bookmarkAsync.await().isValid(), highlightAsync.await().color,
                            noteAsync.await().note, strongNumberAsync.await().toStrongNumberItems())
                }
                viewHolder.verseDetailViewLayout.setVerseDetail(verseDetail!!)
            } catch (e: Exception) {
                Log.e(tag, "Failed to load verse detail", e)
                activity.dialog(true, R.string.dialog_load_verse_detail_error,
                        DialogInterface.OnClickListener { _, _ -> loadVerseDetail(verseIndex) })
            }
        }
    }

    @VisibleForTesting
    suspend fun buildVerseTextItems(verseIndex: VerseIndex): List<VerseTextItem> {
        val currentTranslation = viewModel.currentTranslation().first()
        val parallelTranslations = viewModel.downloadedTranslations().first()
                .sortedWith(translationComparator)
                .filter { it.shortName != currentTranslation }
                .map { it.shortName }
        val verses = viewModel.readVerses(currentTranslation, parallelTranslations,
                verseIndex.bookIndex, verseIndex.chapterIndex)

        // 1. finds the verse
        var start: VerseIndex? = null
        for (verse in verses) {
            if (verse.text.text.isNotEmpty()) start = verse.verseIndex // we need to consider the empty verses
            if (verse.verseIndex.verseIndex >= verseIndex.verseIndex) break
        }

        val verseIterator = verses.iterator()
        var verse: Verse? = null
        while (verseIterator.hasNext()) {
            val v = verseIterator.next()
            if (v.verseIndex == start) {
                verse = v
                break
            }
        }
        if (verse == null) throw IllegalStateException("Failed to find target verse")

        // 2. builds the parallel
        val parallel = Array(parallelTranslations.size) { StringBuilder() }
        val parallelBuilder: (index: Int, Verse.Text) -> Unit = { index, text ->
            with(parallel[index]) {
                if (isNotEmpty()) append(' ')
                append(text.text)
            }
        }
        verse.parallel.forEachIndexed(parallelBuilder)

        var followingEmptyVerseCount = 0
        while (verseIterator.hasNext()) {
            val v = verseIterator.next()
            if (v.text.text.isNotEmpty()) break
            v.parallel.forEachIndexed(parallelBuilder)
            followingEmptyVerseCount++
        }

        // 3. constructs VerseTextItems
        val verseTextItems = ArrayList<VerseTextItem>(parallelTranslations.size + 1)
        verseTextItems.add(VerseTextItem(verse.verseIndex, followingEmptyVerseCount, verse.text,
                viewModel.readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex],
                this@VerseDetailPresenter::onVerseClicked, this@VerseDetailPresenter::onVerseLongClicked))

        parallelTranslations.forEachIndexed { index, translation ->
            verseTextItems.add(VerseTextItem(verse.verseIndex, followingEmptyVerseCount,
                    Verse.Text(translation, parallel[index].toString()),
                    viewModel.readBookNames(translation)[verse.verseIndex.bookIndex],
                    this@VerseDetailPresenter::onVerseClicked, this@VerseDetailPresenter::onVerseLongClicked))
        }

        return verseTextItems
    }

    @VisibleForTesting
    fun onVerseClicked(translation: String) {
        coroutineScope.launch {
            try {
                if (translation != viewModel.currentTranslation().first()) {
                    viewModel.saveCurrentTranslation(translation)
                    close()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to select translation", e)
                activity.toast(R.string.toast_unknown_error)
            }
        }
    }

    @VisibleForTesting
    fun onVerseLongClicked(verse: Verse) {
        coroutineScope.launch {
            try {
                val bookName = viewModel.readBookNames(verse.text.translationShortName)[verse.verseIndex.bookIndex]
                // On older devices, this only works on the threads with loopers.
                (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText(verse.text.translationShortName + " " + bookName,
                                verse.toStringForSharing(bookName)))
                activity.toast(R.string.toast_verses_copied)
            } catch (e: Exception) {
                Log.e(tag, "Failed to copy", e)
                activity.toast(R.string.toast_unknown_error)
            }
        }
    }

    private fun List<StrongNumber>.toStrongNumberItems(): List<StrongNumberItem> = map {
        StrongNumberItem(it, this@VerseDetailPresenter::onStrongNumberClicked)
    }

    private fun onStrongNumberClicked(strongNumber: String) {
        try {
            navigator.navigate(activity, Navigator.SCREEN_STRONG_NUMBER,
                    StrongNumberListActivity.bundle(strongNumber))
        } catch (e: Exception) {
            Log.e(tag, "Failed to open Strong's number list activity", e)
            activity.dialog(true, R.string.dialog_navigation_error,
                    DialogInterface.OnClickListener { _, _ -> onStrongNumberClicked(strongNumber) })
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        downloadStrongNumberJob?.cancel()
    }

    /**
     * @return true if verse detail view was open, or false otherwise
     * */
    fun close(): Boolean {
        viewHolder.verseDetailViewLayout.hide()

        return verseDetail?.let {
            viewModel.closeVerseDetail(it.verseIndex)
            verseDetail = null
            true
        } ?: false
    }
}
