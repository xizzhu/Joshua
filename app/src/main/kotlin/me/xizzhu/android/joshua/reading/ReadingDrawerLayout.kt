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

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.MVPView

class ReadingDrawerPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<ReadingDrawerView>() {
    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            readingInteractor.observeCurrentVerseIndex().filter { it.isValid() }
                    .collect { view?.hide() }
        }
    }
}

interface ReadingDrawerView : MVPView {
    fun hide(): Boolean
}

class ReadingDrawerLayout : DrawerLayout, ReadingDrawerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * @return true if drawer was open, or false otherwise
     * */
    override fun hide(): Boolean {
        if (isDrawerOpen(GravityCompat.START)) {
            closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }
}
