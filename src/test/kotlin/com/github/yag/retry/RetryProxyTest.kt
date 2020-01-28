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