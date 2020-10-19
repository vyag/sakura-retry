/*
 * Copyright 2018-2020 marks.yag@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.yag.retry

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class RetryCallAsyncTest {

    @Test
    fun testNoError() = runBlocking {
        val retry = Retry.NONE
        var count = 0

        assertEquals(0, retry.callAsync {
            count++
        }.await())

        assertEquals(1, count)
    }

    @Test
    fun testRetrySuccess() = runBlocking {
        val retry = Retry.ALWAYS
        val foo = Foo(10)
        assertSame(foo, retry.callAsync {
            foo.bar()
            foo
        }.await())
        assertEquals(11, foo.counter)
    }

    @Test
    fun testRetryFailed() = runBlocking {
        val retry = Retry(
            CountDownRetryPolicy(10, 3000),
            ExponentialBackOffPolicy(1, 10),
            DefaultErrorHandler()
        )
        val foo = Foo(11)
        assertFailsWith<IllegalStateException> {
            retry.callAsync {
                foo.bar()
            }.await()
        }
        assertEquals(11, foo.counter)
    }

    @Test
    fun testNoRetry() = runBlocking<Unit> {
        val foo = Foo(1)
        assertFailsWith<IllegalStateException> {
            Retry.NONE.callAsync {
                foo.bar()
            }.await()
        }
    }
}