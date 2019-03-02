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

package me.xizzhu.android.joshua.core.repository.local.android

import android.content.Context
import androidx.annotation.CallSuper
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before

abstract class BaseSqliteTest {
    protected lateinit var androidDatabase: AndroidDatabase

    @Before
    @CallSuper
    open fun setup() {
        clearLocalStorage()
        androidDatabase = AndroidDatabase(ApplicationProvider.getApplicationContext<Context>())
    }

    private fun clearLocalStorage() {
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(AndroidDatabase.DATABASE_NAME)
    }

    @After
    @CallSuper
    open fun tearDown() {
        androidDatabase.close()
        clearLocalStorage()
    }
}
