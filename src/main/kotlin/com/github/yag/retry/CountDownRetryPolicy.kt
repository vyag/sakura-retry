package com.github.yag.retry

import com.github.yag.config.Value
import java.time.Duration

class CountDownRetryPolicy @JvmOverloads constructor(
    @Value var maxRetries: Int = Integer.MAX_VALUE,
    @Value var maxTimeElapsedMs: Long = Long.MAX_VALUE) : RetryPolicy {

    override fun allowRetry(retryCount: Int, duration: Duration, error: Throwable): Boolean {
        return retryCount < maxRetries && duration.toMillis() < maxTimeElapsedMs
    }

}