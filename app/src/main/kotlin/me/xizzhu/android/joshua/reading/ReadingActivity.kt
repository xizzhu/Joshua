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

package me.xizzhu.android.joshua.reading

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.ActionMode
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityReadingBinding
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.reading.detail.StrongNumberItem
import me.xizzhu.android.joshua.reading.detail.VerseDetailViewLayout
import me.xizzhu.android.joshua.reading.detail.VerseTextItem
import me.xizzhu.android.joshua.reading.verse.SimpleVerseItem
import me.xizzhu.android.joshua.reading.verse.VerseItem
import me.xizzhu.android.joshua.reading.verse.VersePagerAdapter
import me.xizzhu.android.joshua.reading.verse.toBookIndex
import me.xizzhu.android.joshua.reading.verse.toChapterIndex
import me.xizzhu.android.joshua.reading.verse.toPagePosition
import me.xizzhu.android.joshua.reading.verse.toStringForSharing
import me.xizzhu.android.joshua.strongnumber.StrongNumberActivity
import me.xizzhu.android.joshua.ui.ProgressDialog
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.listDialog
import me.xizzhu.android.joshua.ui.makeLessSensitive
import me.xizzhu.android.joshua.ui.progressDialog
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.joshua.utils.copyToClipBoard
import me.xizzhu.android.joshua.utils.shareToSystem
import me.xizzhu.android.logger.Log
import kotlin.math.max

@AndroidEntryPoint
class ReadingActivity : BaseActivity<ActivityReadingBinding, ReadingViewModel>(), SimpleVerseItem.Callback, VerseItem.Callback,
        VerseTextItem.Callback, StrongNumberItem.Callback {
    companion object {
        private const val KEY_OPEN_NOTE = "me.xizzhu.android.joshua.KEY_OPEN_NOTE"

        fun bundleForOpenNote(): Bundle = Bundle().apply { putBoolean(KEY_OPEN_NOTE, true) }
    }

    private val readingViewModel: ReadingViewModel by viewModels()

    private var downloadStrongNumberJob: Job? = null
    private var downloadStrongNumberDialog: ProgressDialog? = null

    private val selectedVerses: MutableSet<Verse> = mutableSetOf()
    private var currentTranslation: String = ""
    private var currentVerseIndex: VerseIndex = VerseIndex.INVALID
    private var currentBookName: String = ""

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_reading_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
            R.id.action_copy -> {
                copy()
                true
            }
            R.id.action_share -> {
                share()
                true
            }
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectedVerses.forEach { verse -> versePagerAdapter.deselectVerse(verse.verseIndex) }
            selectedVerses.clear()
            actionMode = null
        }
    }

    private fun copy() {
        lifecycleScope.launch {
            try {
                if (selectedVerses.isEmpty()) return@launch

                copyToClipBoard(
                        label = "$currentTranslation $currentBookName",
                        text = selectedVerses.toStringForSharing(currentBookName, readingViewModel.settings().first().consolidateVersesForSharing)
                )
                toast(R.string.toast_verses_copied)
            } catch (e: Exception) {
                Log.e(tag, "Failed to copy", e)
                toast(R.string.toast_unknown_error)
            } finally {
                actionMode?.finish()
            }
        }
    }

    private fun share() {
        lifecycleScope.launch {
            try {
                if (selectedVerses.isEmpty()) return@launch

                shareToSystem(
                        title = getString(R.string.text_share_with),
                        text = selectedVerses.toStringForSharing(currentBookName, readingViewModel.settings().first().consolidateVersesForSharing)
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to share", e)
                toast(R.string.toast_unknown_error)
            } finally {
                actionMode?.finish()
            }
        }
    }

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var versePagerAdapter: VersePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeUi()
        observeSettings()
        observeCurrentReadingStatus()
        observeVerseUpdates()
    }

    private fun initializeUi() = with(viewBinding) {
        drawerToggle = ActionBarDrawerToggle(this@ReadingActivity, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                try {
                    viewBinding.chapterSelectionView.expandCurrentBook()
                } catch (e: Exception) {
                    Log.e(tag, "Error occurred while expanding current book", e)
                }
            }
        })

        toolbar.initialize(
                requestParallelTranslation = ::requestParallelTranslation,
                removeParallelTranslation = ::removeParallelTranslation,
                selectCurrentTranslation = ::selectTranslation,
                titleClicked = drawerLayout::open,
                navigate = ::startActivity
        )

        chapterSelectionView.initialize(::selectChapter)

        versePagerAdapter = VersePagerAdapter(this@ReadingActivity, ::loadVerses, ::updateCurrentVerse)
        verseViewPager.offscreenPageLimit = 1
        verseViewPager.adapter = versePagerAdapter
        verseViewPager.makeLessSensitive()
        verseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val bookIndex = position.toBookIndex()
                val chapterIndex = position.toChapterIndex()
                lifecycleScope.launch {
                    val currentVerseIndex = readingViewModel.currentVerseIndex()
                    if (bookIndex != currentVerseIndex.bookIndex || chapterIndex != currentVerseIndex.chapterIndex) {
                        selectChapter(bookIndex, chapterIndex)
                    }
                }
            }
        })

        search.setOnClickListener { startActivity(Navigator.SCREEN_SEARCH) }

        val openNoteWhenCreated = intent.getBooleanExtra(KEY_OPEN_NOTE, false)
        verseDetailView.initialize(
                onClicked = ::hideVerseDetail,
                updateBookmark = ::onBookmarkClicked,
                updateHighlight = ::onHighlightClicked,
                updateNote = ::saveNote,
                requestStrongNumber = ::downloadStrongNumber,
                hide = !openNoteWhenCreated
        )
        if (openNoteWhenCreated) {
            lifecycleScope.launch { showVerseDetail(readingViewModel.currentVerseIndex(), VerseDetailViewLayout.VERSE_DETAIL_NOTE) }
        }
    }

    private fun requestParallelTranslation(translation: String) {
        readingViewModel.requestParallelTranslation(translation).launchIn(lifecycleScope)
    }

    private fun removeParallelTranslation(translation: String) {
        readingViewModel.removeParallelTranslation(translation).launchIn(lifecycleScope)
    }

    private fun selectTranslation(translation: String) {
        readingViewModel.selectTranslation(translation)
                .onFailure { dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_translation, { _, _ -> selectTranslation(translation) }) }
                .launchIn(lifecycleScope)
    }

    private fun startActivity(@Navigator.Companion.Screen screen: Int, extras: Bundle? = null) {
        try {
            navigator.navigate(this, screen, extras)
        } catch (e: Exception) {
            Log.e(tag, "Failed to open activity", e)
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_navigate, { _, _ -> startActivity(screen) })
        }
    }

    private fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        readingViewModel.selectCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
                .onFailure { dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_chapter, { _, _ -> selectChapter(bookIndex, chapterIndex) }) }
                .launchIn(lifecycleScope)
    }

    private fun loadVerses(bookIndex: Int, chapterIndex: Int) {
        readingViewModel.loadVerses(bookIndex, chapterIndex)
                .onSuccess { verses -> versePagerAdapter.setVerses(bookIndex, chapterIndex, verses.items) }
                .onFailure { e ->
                    Log.e(tag, "Failed to load verses", e)
                    dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_verses, { _, _ -> loadVerses(bookIndex, chapterIndex) })
                }
                .launchIn(lifecycleScope)
    }

    private fun updateCurrentVerse(verseIndex: VerseIndex) {
        readingViewModel.selectCurrentVerseIndex(verseIndex).launchIn(lifecycleScope)
    }

    private fun downloadStrongNumber() {
        if (downloadStrongNumberJob != null || downloadStrongNumberDialog != null) {
            // just in case the user clicks too fast
            return
        }
        downloadStrongNumberDialog = progressDialog(R.string.dialog_title_downloading, 100) { downloadStrongNumberJob?.cancel() }

        lifecycleScope.launchWhenStarted {
            readingViewModel.downloadStrongNumber()
                    .onEach(
                            onLoading = { progress ->
                                when (progress) {
                                    in 0 until 99 -> {
                                        downloadStrongNumberDialog?.setProgress(progress!!)
                                    }
                                    else -> {
                                        downloadStrongNumberDialog?.run {
                                            setTitle(R.string.dialog_title_installing)
                                            setIsIndeterminate(true)
                                        }
                                    }
                                }
                            },
                            onSuccess = {
                                toast(R.string.toast_downloaded)

                                viewBinding.verseDetailView.verseDetail?.let { verseDetail ->
                                    readingViewModel.strongNumbers(verseDetail.verseIndex)
                                            .onSuccess { viewBinding.verseDetailView.setStrongNumbers(it) }
                                            .launchIn(lifecycleScope)
                                }
                            },
                            onFailure = {
                                dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_download, { _, _ -> downloadStrongNumber() })
                            }
                    )
                    .onCompletion {
                        downloadStrongNumberDialog?.dismiss()
                        downloadStrongNumberDialog = null
                        downloadStrongNumberJob = null
                    }
                    .collect()
        }
    }

    private fun observeSettings() {
        readingViewModel.settings().onEach { settings ->
            if (settings.hideSearchButton) {
                viewBinding.search.hide()
            } else {
                viewBinding.search.show()
            }

            versePagerAdapter.settings = settings
            viewBinding.verseDetailView.setSettings(settings)
        }.launchIn(lifecycleScope)
    }

    private fun observeCurrentReadingStatus() {
        readingViewModel.currentReadingStatus()
                .onSuccess { currentReadingStatus ->
                    with(viewBinding) {
                        toolbar.title = "${currentReadingStatus.bookShortNames[currentReadingStatus.currentVerseIndex.bookIndex]}, ${currentReadingStatus.currentVerseIndex.chapterIndex + 1}"
                        toolbar.setData(
                                currentTranslation = currentReadingStatus.currentTranslation,
                                parallelTranslations = currentReadingStatus.parallelTranslations,
                                downloadedTranslations = currentReadingStatus.downloadedTranslations
                        )

                        chapterSelectionView.setData(currentReadingStatus.currentVerseIndex, currentReadingStatus.bookNames)
                        drawerLayout.hide()

                        versePagerAdapter.setCurrent(
                                verseIndex = currentReadingStatus.currentVerseIndex,
                                translation = currentReadingStatus.currentTranslation,
                                parallel = currentReadingStatus.parallelTranslations
                        )
                        verseViewPager.setCurrentItem(currentReadingStatus.currentVerseIndex.toPagePosition(), false)
                    }
                    hideVerseDetail()

                    actionMode?.let { actionMode ->
                        if (currentVerseIndex.bookIndex != currentReadingStatus.currentVerseIndex.bookIndex
                                || currentVerseIndex.chapterIndex != currentReadingStatus.currentVerseIndex.chapterIndex) {
                            actionMode.finish()
                        }
                    }

                    currentTranslation = currentReadingStatus.currentTranslation
                    currentVerseIndex = currentReadingStatus.currentVerseIndex
                    currentBookName = currentReadingStatus.bookNames[currentReadingStatus.currentVerseIndex.bookIndex]
                }.launchIn(lifecycleScope)
    }

    private fun hideVerseDetail(): Boolean {
        val verseIndex = viewBinding.verseDetailView.verseDetail?.verseIndex ?: return false
        return if (viewBinding.verseDetailView.hide()) {
            readingViewModel.updateVerse(VerseUpdate(verseIndex, VerseUpdate.VERSE_DESELECTED))
            true
        } else {
            false
        }
    }

    private fun observeVerseUpdates() {
        readingViewModel.verseUpdates().onEach { versePagerAdapter.notifyVerseUpdate(it) }.launchIn(lifecycleScope)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            if (readingViewModel.hasDownloadedTranslation()) return@launch

            dialog(
                    false, R.string.dialog_title_no_translation_downloaded, R.string.dialog_message_download_translation_confirmation,
                    { _, _ -> startActivity(Navigator.SCREEN_TRANSLATIONS) }, { _, _ -> finish() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        readingViewModel.startTrackingReadingProgress()
    }

    override fun onPause() {
        readingViewModel.stopTrackingReadingProgress()
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (!viewBinding.drawerLayout.hide() && !hideVerseDetail()) {
            super.onBackPressed()
        }
    }

    override fun inflateViewBinding(): ActivityReadingBinding = ActivityReadingBinding.inflate(layoutInflater)

    override fun viewModel(): ReadingViewModel = readingViewModel

    override fun onVerseClicked(verse: Verse) {
        if (actionMode == null) {
            showVerseDetail(verse.verseIndex, VerseDetailViewLayout.VERSE_DETAIL_VERSES)
            return
        }

        if (selectedVerses.contains(verse)) {
            // de-select the verse
            selectedVerses.remove(verse)
            if (selectedVerses.isEmpty()) {
                actionMode?.finish()
            }

            versePagerAdapter.deselectVerse(verse.verseIndex)
        } else {
            // select the verse
            selectedVerses.add(verse)
            versePagerAdapter.selectVerse(verse.verseIndex)
        }
    }

    private fun showVerseDetail(verseIndex: VerseIndex, @VerseDetailViewLayout.Companion.VerseDetail content: Int) {
        readingViewModel.loadVerseDetail(verseIndex)
                .onEach(
                        onLoading = { viewBinding.verseDetailView.verseDetail = null },
                        onSuccess = {
                            with(viewBinding.verseDetailView) {
                                verseDetail = it
                                show(content)
                            }
                            readingViewModel.updateVerse(VerseUpdate(verseIndex, VerseUpdate.VERSE_SELECTED))
                        },
                        onFailure = { e ->
                            Log.e(tag, "Failed to load verses", e)
                            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_verse_detail, { _, _ -> showVerseDetail(verseIndex, content) })
                        }
                )
                .launchIn(lifecycleScope)
    }

    override fun onVerseLongClicked(verse: Verse) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback)
        }
        onVerseClicked(verse)
    }

    override fun onBookmarkClicked(verseIndex: VerseIndex, currentlyBookmarked: Boolean) {
        val toBeBookmarked = !currentlyBookmarked
        readingViewModel.saveBookmark(verseIndex = verseIndex, toBeBookmarked = toBeBookmarked)
                .onSuccess { viewBinding.verseDetailView.setBookmarked(toBeBookmarked) }
                .onFailure { toast(R.string.toast_unknown_error) }
                .launchIn(lifecycleScope)
    }

    override fun onHighlightClicked(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor currentHighlightColor: Int) {
        lifecycleScope.launch {
            val defaultHighlightColor = readingViewModel.settings().first().defaultHighlightColor
            if (Highlight.COLOR_NONE == defaultHighlightColor) {
                listDialog(R.string.text_pick_highlight_color,
                        resources.getStringArray(R.array.text_colors),
                        max(0, Highlight.AVAILABLE_COLORS.indexOf(currentHighlightColor))) { dialog, which ->
                    saveHighlight(verseIndex, Highlight.AVAILABLE_COLORS[which])

                    dialog.dismiss()
                }
            } else {
                saveHighlight(verseIndex, if (currentHighlightColor == Highlight.COLOR_NONE) defaultHighlightColor else Highlight.COLOR_NONE)
            }
        }
    }

    private fun saveHighlight(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor highlightColor: Int) {
        readingViewModel.saveHighlight(verseIndex, highlightColor)
                .onSuccess { viewBinding.verseDetailView.setHighlightColor(highlightColor) }
                .onFailure { toast(R.string.toast_unknown_error) }
                .launchIn(lifecycleScope)
    }

    override fun onNoteClicked(verseIndex: VerseIndex) {
        showVerseDetail(verseIndex, VerseDetailViewLayout.VERSE_DETAIL_NOTE)
    }

    private fun saveNote(verseIndex: VerseIndex, note: String) {
        readingViewModel.saveNote(verseIndex, note)
                .onSuccess { viewBinding.verseDetailView.setNote(note) }
                .onFailure { toast(R.string.toast_unknown_error) }
                .launchIn(lifecycleScope)
    }

    override fun onVerseTextClicked(translation: String) {
        selectTranslation(translation)
    }

    override fun onVerseTextLongClicked(verse: Verse) {
        lifecycleScope.launch {
            try {
                val bookName = readingViewModel.bookName(verse.text.translationShortName, verse.verseIndex.bookIndex)
                copyToClipBoard(
                        label = "${verse.text.translationShortName} $bookName",
                        text = verse.toStringForSharing(bookName)
                )
                toast(R.string.toast_verses_copied)
            } catch (e: Exception) {
                Log.e(tag, "Failed to copy", e)
                toast(R.string.toast_unknown_error)
            }
        }
    }

    override fun openStrongNumber(strongNumber: String) {
        startActivity(Navigator.SCREEN_STRONG_NUMBER, StrongNumberActivity.bundle(strongNumber))
    }
}
