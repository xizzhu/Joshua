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

package me.xizzhu.android.joshua.end2end

import androidx.annotation.CallSuper
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingPolicies
import me.xizzhu.android.joshua.core.repository.local.android.db.AndroidDatabase
import me.xizzhu.android.joshua.core.repository.local.android.db.MetadataDao
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class BaseE2eTest {
    private lateinit var androidDatabase: AndroidDatabase

    @BeforeTest
    @CallSuper
    open fun setup() {
        IdlingPolicies.setMasterPolicyTimeout(5L, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(5L, TimeUnit.MINUTES)

        androidDatabase = AndroidDatabase(ApplicationProvider.getApplicationContext())
        resetLocalDatabase()
    }

    private fun resetLocalDatabase() {
        androidDatabase.removeAll()
    }

    @AfterTest
    @CallSuper
    open fun tearDown() {
        resetLocalDatabase()
        androidDatabase.close()
    }

    protected fun assertNoCurrentTranslation() {
        assertTrue(androidDatabase.metadataDao.read(MetadataDao.KEY_CURRENT_TRANSLATION, "").isEmpty())
    }

    protected fun assertCurrentTranslation(currentTranslation: String) {
        assertEquals(currentTranslation, androidDatabase.metadataDao.read(MetadataDao.KEY_CURRENT_TRANSLATION, ""))
    }
}
