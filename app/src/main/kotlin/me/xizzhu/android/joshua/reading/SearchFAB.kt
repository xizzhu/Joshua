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
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.MVPView

class SearchButtonPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<SearchButtonView>() {
    fun openSearch() {
        try {
            readingInteractor.openSearch()
        } catch (e: Exception) {
            Log.e(tag, e, "Failed to open search activity")
            view?.onFailedToNavigateToSearch()
        }
    }
}

interface SearchButtonView : MVPView {
    fun onFailedToNavigateToSearch()
}

class SearchFloatingActionButton : FloatingActionButton, SearchButtonView, View.OnClickListener {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setOnClickListener(this)
    }

    private lateinit var presenter: SearchButtonPresenter

    fun setPresenter(presenter: SearchButtonPresenter) {
        this.presenter = presenter
    }

    override fun onClick(v: View) {
        presenter.openSearch()
    }

    override fun onFailedToNavigateToSearch() {
        DialogHelper.showDialog(context, true, R.string.dialog_navigate_to_search_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.openSearch()
                })
    }
}

class FloatingActionButtonScrollAwareBehavior : FloatingActionButton.Behavior {
    private var isHiding: Boolean = false

    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                     directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                   target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (dy > 0) {
            hide(child, coordinatorLayout)
        } else if (dy < 0) {
            show(child)
        }
    }

    private fun hide(fab: FloatingActionButton, parent: CoordinatorLayout) {
        if (isHiding) {
            return
        }
        isHiding = true

        fab.animate().cancel()
        fab.animate().translationY(parent.height - fab.y)
    }

    private fun show(fab: FloatingActionButton) {
        if (!isHiding) {
            return
        }
        isHiding = false

        fab.animate().translationY(0.0F)
    }
}
