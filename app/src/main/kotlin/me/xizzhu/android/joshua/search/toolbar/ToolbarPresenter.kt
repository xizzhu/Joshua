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

package me.xizzhu.android.joshua.search.toolbar

import me.xizzhu.android.joshua.search.SearchInteractor
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.logger.Log
import java.lang.Exception

class ToolbarPresenter(private val searchInteractor: SearchInteractor) : MVPPresenter<ToolbarView>() {
    fun updateSearchQuery(query: String): Boolean {
        if (query.isEmpty()) {
            return false
        }
        try {
            return searchInteractor.updateSearchQuery(query)
        } catch (e: Exception) {
            Log.e(tag, "Failed to update search query", e)
            return false
        }
    }
}
