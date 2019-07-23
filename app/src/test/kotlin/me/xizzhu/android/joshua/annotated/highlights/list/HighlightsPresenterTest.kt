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

package me.xizzhu.android.joshua.annotated.highlights.list

import android.content.res.Resources
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.annotated.AnnotatedVersesView
import me.xizzhu.android.joshua.annotated.highlights.HighlightsInteractor
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class HighlightsPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var highlightsInteractor: HighlightsInteractor
    @Mock
    private lateinit var annotatedVersesView: AnnotatedVersesView
    @Mock
    private lateinit var resources: Resources

    private lateinit var settingsChannel: BroadcastChannel<Settings>
    private lateinit var sortOrder: BroadcastChannel<Int>
    private lateinit var highlightsPresenter: HighlightsPresenter

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
            sortOrder = ConflatedBroadcastChannel(Constants.SORT_BY_DATE)
            `when`(highlightsInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())
            `when`(highlightsInteractor.observeSortOrder()).thenReturn(sortOrder.asFlow())
            `when`(highlightsInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(resources.getString(anyInt())).thenReturn("")
            `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
            `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })

            highlightsPresenter = spy(HighlightsPresenter(highlightsInteractor, resources))
        }
    }

    @Test
    fun testLoadEmptyHighlights() {
        runBlocking {
            `when`(highlightsInteractor.readHighlights(Constants.SORT_BY_DATE)).thenReturn(emptyList())

            // load() is called by onViewAttached(), so no need to call again
            highlightsPresenter.attachView(annotatedVersesView)
            verify(annotatedVersesView, times(1)).onItemsLoaded(listOf(TextItem("")))
            verify(annotatedVersesView, never()).onLoadingFailed(anyInt())

            highlightsPresenter.detachView()
        }
    }

    @Test
    fun testLoadHighlightsSortByDate() {
        runBlocking {
            `when`(highlightsInteractor.readHighlights(Constants.SORT_BY_DATE)).thenReturn(listOf(
                    Highlight(VerseIndex(0, 0, 4), Highlight.COLOR_PURPLE, 2L * 365L * 24L * 3600L * 1000L),
                    Highlight(VerseIndex(0, 0, 1), Highlight.COLOR_BLUE, 36L * 3600L * 1000L),
                    Highlight(VerseIndex(0, 0, 3), Highlight.COLOR_YELLOW, 36L * 3600L * 1000L - 1000L),
                    Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_PINK, 0L)
            ))
            `when`(highlightsInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(highlightsInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 1)))
                    .thenReturn(MockContents.kjvVerses[1])
            `when`(highlightsInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 2)))
                    .thenReturn(MockContents.kjvVerses[2])
            `when`(highlightsInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 3)))
                    .thenReturn(MockContents.kjvVerses[3])
            `when`(highlightsInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 4)))
                    .thenReturn(MockContents.kjvVerses[4])
            `when`(highlightsInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            `when`(highlightsInteractor.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

            // load() is called by onViewAttached(), so no need to call again
            highlightsPresenter.attachView(annotatedVersesView)

            with(inOrder(highlightsInteractor, annotatedVersesView)) {
                verify(highlightsInteractor, times(1)).notifyLoadingStarted()
                verify(annotatedVersesView, times(1)).onLoadingStarted()
                verify(annotatedVersesView, times(1)).onItemsLoaded(listOf(
                        TitleItem("", false),
                        HighlightItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, Highlight.COLOR_PURPLE, Constants.SORT_BY_DATE, highlightsPresenter::openVerse),
                        TitleItem("", false),
                        HighlightItem(VerseIndex(0, 0, 1), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[1].text.text, Highlight.COLOR_BLUE, Constants.SORT_BY_DATE, highlightsPresenter::openVerse),
                        HighlightItem(VerseIndex(0, 0, 3), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, Highlight.COLOR_YELLOW, Constants.SORT_BY_DATE, highlightsPresenter::openVerse),
                        TitleItem("", false),
                        HighlightItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, Highlight.COLOR_PINK, Constants.SORT_BY_DATE, highlightsPresenter::openVerse)
                ))
                verify(annotatedVersesView, times(1)).onLoadingCompleted()
                verify(highlightsInteractor, times(1)).notifyLoadingFinished()
            }
            verify(annotatedVersesView, never()).onLoadingFailed(anyInt())

            highlightsPresenter.detachView()
        }
    }

    @Test
    fun testLoadHighlightsSortByBook() {
        runBlocking {
            sortOrder.send(Constants.SORT_BY_BOOK)
            `when`(highlightsInteractor.readHighlights(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Highlight(VerseIndex(0, 0, 3), Highlight.COLOR_PINK, 0L)
            ))
            `when`(highlightsInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(highlightsInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 3)))
                    .thenReturn(MockContents.kjvVerses[3])
            `when`(highlightsInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            `when`(highlightsInteractor.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

            // load() is called by onViewAttached(), so no need to call again
            highlightsPresenter.attachView(annotatedVersesView)

            with(inOrder(highlightsInteractor, annotatedVersesView)) {
                verify(highlightsInteractor, times(1)).notifyLoadingStarted()
                verify(annotatedVersesView, times(1)).onLoadingStarted()
                verify(annotatedVersesView, times(1)).onItemsLoaded(listOf(
                        TitleItem(MockContents.kjvBookNames[0], false),
                        HighlightItem(VerseIndex(0, 0, 3), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, Highlight.COLOR_PINK, Constants.SORT_BY_BOOK, highlightsPresenter::openVerse)
                ))
                verify(annotatedVersesView, times(1)).onLoadingCompleted()
                verify(highlightsInteractor, times(1)).notifyLoadingFinished()
            }
            verify(annotatedVersesView, never()).onLoadingFailed(anyInt())

            highlightsPresenter.detachView()
        }
    }

    @Test
    fun testLoadHighlightsWithException() {
        runBlocking {
            `when`(highlightsInteractor.readHighlights(Constants.SORT_BY_DATE)).thenThrow(RuntimeException("Random exception"))

            // load() is called by onViewAttached(), so no need to call again
            highlightsPresenter.attachView(annotatedVersesView)

            with(inOrder(highlightsInteractor, annotatedVersesView)) {
                verify(highlightsInteractor, times(1)).notifyLoadingStarted()
                verify(annotatedVersesView, times(1)).onLoadingStarted()
                verify(annotatedVersesView, times(1)).onLoadingFailed(Constants.SORT_BY_DATE)
                verify(highlightsInteractor, times(1)).notifyLoadingFinished()
            }
            verify(annotatedVersesView, never()).onItemsLoaded(any())
            verify(annotatedVersesView, never()).onLoadingCompleted()

            highlightsPresenter.detachView()
        }
    }
}
