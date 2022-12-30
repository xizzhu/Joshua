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
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R

class ReadingToolbar : MaterialToolbar {
    private var spinnerPosition = -1

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflateMenu(R.menu.menu_reading)
    }

    fun initialize(
        requestParallelTranslation: (translationShortName: String) -> Unit,
        removeParallelTranslation: (translationShortName: String) -> Unit,
        selectCurrentTranslation: (translationShortName: String) -> Unit,
        titleClicked: () -> Unit,
        navigate: (screen: Int) -> Unit
    ) {
        with(spinner()) {
            adapter = TranslationSpinnerAdapter(
                context = context,
                requestParallelTranslation = requestParallelTranslation,
                removeParallelTranslation = removeParallelTranslation
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (spinnerPosition == position) return

                    when (val item = spinnerAdapter().getItem(position)) {
                        is TranslationItem.Translation -> {
                            spinnerPosition = position
                            selectCurrentTranslation(item.translationShortName)
                        }
                        is TranslationItem.More -> {
                            // selected "More", re-select the current translation,
                            // and start translations management activity
                            if (spinnerPosition >= 0) setSelection(spinnerPosition)
                            navigate(Navigator.SCREEN_TRANSLATIONS)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // do nothing
                }
            }
        }
        setOnClickListener { titleClicked() }
        setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_reading_progress -> {
                    navigate(Navigator.SCREEN_READING_PROGRESS)
                    true
                }
                R.id.action_bookmarks -> {
                    navigate(Navigator.SCREEN_BOOKMARKS)
                    true
                }
                R.id.action_highlights -> {
                    navigate(Navigator.SCREEN_HIGHLIGHTS)
                    true
                }
                R.id.action_notes -> {
                    navigate(Navigator.SCREEN_NOTES)
                    true
                }
                R.id.action_search -> {
                    navigate(Navigator.SCREEN_SEARCH)
                    true
                }
                R.id.action_settings -> {
                    navigate(Navigator.SCREEN_SETTINGS)
                    true
                }
                else -> false
            }
        }
    }

    private fun spinner(): Spinner = menu.findItem(R.id.action_translations).actionView as Spinner

    private fun spinnerAdapter(): TranslationSpinnerAdapter = spinner().adapter as TranslationSpinnerAdapter

    fun setData(currentTranslation: String, parallelTranslations: List<String>, downloadedTranslations: List<String>) {
        val items = ArrayList<TranslationItem>(downloadedTranslations.size + 1)
        downloadedTranslations.forEach { downloaded ->
            val isCurrentTranslation = currentTranslation == downloaded
            items.add(TranslationItem.Translation(
                translationShortName = downloaded,
                isCurrentTranslation = isCurrentTranslation,
                isParallelTranslation = parallelTranslations.contains(downloaded),
            ))
        }
        items.add(TranslationItem.More)
        spinnerAdapter().setItems(items)

        val currentTranslationPosition = downloadedTranslations.indexOfFirst { it == currentTranslation }
        if (currentTranslationPosition >= 0) {
            spinnerPosition = currentTranslationPosition
            spinner().setSelection(currentTranslationPosition)
        }
    }
}
