package com.github.yag.retry

import com.github.yag.config.Value
import java.time.Duration

class ExponentialBackOffPolicy(
    @Value var baseIntervalMs: Long,
    @Value var maxIntervalMs: Long
) :
    BackOffPolicy {

    constructor() : this(1000, 10000)

    override fun backOff(retryCount: Int, duration: Duration, error: Throwable): Duration {
        return Duration.ofMillis(minOf(maxOf(minOf(duration.toMillis(), Long.MAX_VALUE / 2) * 2, baseIntervalMs), maxIntervalMs))
    }
}