package com.github.yag.retry

import java.time.Duration

interface BackOffPolicy {

    fun backOff(retryCount: Int, duration: Duration, error: Throwable): Long

}

