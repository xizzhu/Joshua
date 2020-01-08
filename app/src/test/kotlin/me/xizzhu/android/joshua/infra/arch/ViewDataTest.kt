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

package me.xizzhu.android.joshua.infra.arch

import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ViewDataTest : BaseUnitTest() {
    @Test
    fun testSuccess() {
        val actual = ViewData.success("random data")
        assertEquals(ViewData.STATUS_SUCCESS, actual.status)
        assertEquals("random data", actual.data)
        assertNull(actual.exception)
    }

    @Test
    fun testError() {
        val actual = ViewData.error("random data")
        assertEquals(ViewData.STATUS_ERROR, actual.status)
        assertEquals("random data", actual.data)
        assertNull(actual.exception)
    }

    @Test
    fun testErrorWithException() {
        val exception = RuntimeException("random exception")
        val actual = ViewData.error("random data", exception)
        assertEquals(ViewData.STATUS_ERROR, actual.status)
        assertEquals("random data", actual.data)
        assertEquals(exception, actual.exception)
    }

    @Test
    fun testLoading() {
        val actual = ViewData.loading("random data")
        assertEquals(ViewData.STATUS_LOADING, actual.status)
        assertEquals("random data", actual.data)
        assertNull(actual.exception)
    }

    @Test
    fun testViewDataWrapper() {
        assertEquals(ViewData.success(Unit), viewData { Unit })
    }

    @Test
    fun testViewDataWrapperWithException() {
        val exception = RuntimeException("random exception")
        assertEquals(ViewData.error(exception = exception), viewData { throw exception })
    }

    @Test
    fun testDataOnSuccess() {
        assertEquals(Unit, ViewData.success(Unit).dataOnSuccessOrThrow(""))
    }

    @Test(expected = IllegalStateException::class)
    fun testDataOnLoading() {
        ViewData.loading(Unit).dataOnSuccessOrThrow("")
    }

    @Test(expected = IllegalStateException::class)
    fun testDataOnError() {
        ViewData.error(Unit).dataOnSuccessOrThrow("")
    }
}
