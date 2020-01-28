package com.github.yag.retry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetryCallTest {

    @Test
    fun testNoError() {
        val retry = Retry(
            CountDownRetryPolicy(),
            ExponentialBackOffPolicy(),
            DefaultErrorHandler()
        )
        var count = 0
        retry.call {
            count++
        }
        assertEquals(1, count)
    }

    @Test
    fun testRetrySuccess() {
        val retry = Retry(
            CountDownRetryPolicy(10, 3000),
            ExponentialBackOffPolicy(1, 10),
            DefaultErrorHandler()
        )
        var foo = Foo(10)
        retry.call {
            foo.bar()
        }
        assertEquals(11, foo.counter)
    }

    @Test
    fun testRetryFailed() {
        val retry = Retry(
            CountDownRetryPolicy(10, 3000),
            ExponentialBackOffPolicy(1, 10),
            DefaultErrorHandler()
        )
        var foo = Foo(11)
        assertFailsWith(IllegalStateException::class) {
            retry.call {
                foo.bar()
            }
        }
        assertEquals(11, foo.counter)
    }

}