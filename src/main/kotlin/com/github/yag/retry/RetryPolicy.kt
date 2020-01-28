package com.github.yag.retry

import java.time.Duration

interface RetryPolicy {


    fun check() {
    }

    fun allowRetry(retryCount: Int, duration: Duration, error: Throwable) : Boolean

}