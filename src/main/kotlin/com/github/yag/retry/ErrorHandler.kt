package com.github.yag.retry

import java.time.Duration

interface ErrorHandler {

    fun handle(retryCount: Int, duration: Duration, error: Throwable, allowRetry: Boolean, backOffDuration: Duration)

}