/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.github.yag.retry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetryProxyTest {

    @Test
    fun testRetrySuccess() {
        val retry = Retry(
            CountDownRetryPolicy(10, 3000),
            ExponentialBackOffPolicy(1, 10),
            DefaultErrorHandler()
        )
        val rawFoo = Foo(10)
        val foo = retry.proxy(IFoo::class.java, rawFoo)
        foo.bar()
        assertEquals(11, rawFoo.counter)
    }

    @Test
    fun testRetryFailed() {
        val retry = Retry(
            CountDownRetryPolicy(10, 3000),
            ExponentialBackOffPolicy(1, 10),
            DefaultErrorHandler()
        )
        val rawFoo = Foo(11)
        val foo = retry.proxy(IFoo::class.java, rawFoo)
        assertFailsWith(IllegalStateException::class) {
            foo.bar()
        }
        assertEquals(11, rawFoo.counter)
    }

}