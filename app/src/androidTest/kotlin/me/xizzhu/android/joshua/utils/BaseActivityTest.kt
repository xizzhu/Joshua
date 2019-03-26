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

package me.xizzhu.android.joshua.utils

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class BaseActivityTest : BaseUnitTest() {
    private class BaseActivityStub : BaseActivity() {
        lateinit var myJob: Job

        public override fun onStart() {
            try {
                super.onStart()
            } catch (ignored: Exception) {
            }

            myJob = launch(Dispatchers.Unconfined) {
                while (isActive) {
                    delay(1L)
                }
            }
        }

        public override fun onDestroy() {
            try {
                super.onDestroy()
            } catch (ignored: Exception) {
            }
        }
    }

    private lateinit var baseActivityStub: BaseActivityStub

    @Before
    override fun setup() {
        super.setup()
        Looper.prepare()
        baseActivityStub = BaseActivityStub()
    }

    @Test
    fun testJobCancellation() {
        baseActivityStub.onStart()
        assertTrue(baseActivityStub.myJob.isActive)

        baseActivityStub.onDestroy()
        assertTrue(baseActivityStub.myJob.isCancelled)
    }
}
