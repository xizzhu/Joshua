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

package me.xizzhu.android.joshua.reading.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import com.google.android.material.appbar.MaterialToolbar
import me.xizzhu.android.joshua.R

class ReadingToolbar : MaterialToolbar {
    sealed class ViewEvent {
        object OpenBookmarks : ViewEvent()
        object OpenHighlights : ViewEvent()
        object OpenNotes : ViewEvent()
        object OpenReadingProgress : ViewEvent()
        object OpenSearch : ViewEvent()
        object OpenSettings : ViewEvent()
        object OpenTranslations : ViewEvent()
        data class RemoveParallelTranslation(val translationToRemove: String) : ViewEvent()
        data class RequestParallelTranslation(val translationToRequest: String) : ViewEvent()
        data class SelectCurrentTranslation(val translationToSelect: String) : ViewEvent()
        object TitleClicked : ViewEvent()
    }

    private var spinnerPosition = -1

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflateMenu(R.menu.menu_reading)
    }

    fun initialize(onViewEvent: (ViewEvent) -> Unit) {
        with(spinner()) {
            adapter = TranslationSpinnerAdapter(context = context, onViewEvent = onViewEvent)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (spinnerPosition == position) return

                    when (val item = spinnerAdapter().getItem(position)) {
                        is TranslationItem.Translation -> {
                            spinnerPosition = position
                            onViewEvent(ViewEvent.SelectCurrentTranslation(item.translationShortName))
                        }
                        is TranslationItem.More -> {
                            // selected "More", re-select the current translation,
                            // and start translations management activity
                            if (spinnerPosition >= 0) setSelection(spinnerPosition)
                            onViewEvent(ViewEvent.OpenTranslations)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // do nothing
                }
            }
        }
        setOnClickListener { onViewEvent(ViewEvent.TitleClicked) }
        setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_reading_progress -> {
                    onViewEvent(ViewEvent.OpenReadingProgress)
                    true
                }
                R.id.action_bookmarks -> {
                    onViewEvent(ViewEvent.OpenBookmarks)
                    true
                }
                R.id.action_highlights -> {
                    onViewEvent(ViewEvent.OpenHighlights)
                    true
                }
                R.id.action_notes -> {
                    onViewEvent(ViewEvent.OpenNotes)
                    true
                }
                R.id.action_search -> {
                    onViewEvent(ViewEvent.OpenSearch)
                    true
                }
                R.id.action_settings -> {
                    onViewEvent(ViewEvent.OpenSettings)
                    true
                }
                else -> false
            }
        }
    }

    private fun spinner(): Spinner = menu.findItem(R.id.action_translations).actionView as Spinner

    private fun spinnerAdapter(): TranslationSpinnerAdapter = spinner().adapter as TranslationSpinnerAdapter

    fun setTranslationItems(items: List<TranslationItem>) {
        spinnerAdapter().setItems(items)

        val currentTranslationPosition = items.indexOfFirst { (it as? TranslationItem.Translation)?.isCurrentTranslation == true }
        if (currentTranslationPosition >= 0) {
            spinnerPosition = currentTranslationPosition
            spinner().setSelection(currentTranslationPosition)
        }
    }
}
