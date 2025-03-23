/*
 * Copyright 2018-2025 marks.yag@gmail.com
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
package retry

import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object BackoffPolicies {

    /**
     * No backoff.
     */
    @JvmField
    val NONE = BackoffPolicy { Duration.ZERO }

    /**
     * Fixed delay back off of specified seconds.
     * @see FixedDelay
     */
    @JvmStatic
    fun seconds(amount: Long) = FixedDelay(amount.seconds)

    /**
     * Fixed delay back off of specified milliseconds.
     * 
     * @see FixedDelay
     */
    @JvmStatic
    fun millis(amount: Long) = FixedDelay(amount.milliseconds)
}

/**
 * Fixed delay back off.
 *
 * @param delay The delay.
 */
data class FixedDelay(val delay: Duration) : BackoffPolicy {

    /**
     * Constructs a fixed delay back off.
     *
     * @param delay The interval.
     */
    constructor(delay: kotlin.time.Duration) : this(delay.toJavaDuration())

    override fun backoff(context: Context): Duration {
        return delay
    }
}

/**
 * Fixed interval back off.
 *
 * @param interval The delay.
 */
data class FixedInterval(val interval: Duration) : BackoffPolicy {

    /**
     * Constructs a fixed interval back off.
     *
     * @param interval The interval.
     */
    constructor(interval: kotlin.time.Duration) : this(interval.toJavaDuration())

    override fun backoff(context: Context): Duration {
        val targetRetryTime = context.startTime.plus(
            interval.multipliedBy(context.retryCount.toLong()))
        return if (targetRetryTime.isAfter(context.now)) {
            Duration.between(context.now, targetRetryTime)
        } else {
            Duration.ZERO
        }
    }
}

/**
 * Exponential backoff implementation.
 *
 * @property initDuration The initial duration.
 * @property maxDuration The maximum duration.
 */
data class Exponential(
    val initDuration: Duration,
    val maxDuration: Duration
) : BackoffPolicy {

    /**
     * Constructs an Exponential backoff implementation.
     *
     * @param initDuration The initial duration.
     * @param maxDuration The maximum duration.
     */
    constructor(initDuration: kotlin.time.Duration, maxDuration: kotlin.time.Duration) : this(initDuration.toJavaDuration(), maxDuration.toJavaDuration())

    override fun backoff(context: Context): Duration {
        var value = initDuration.toMillis()
        for (i in 0 until context.retryCount) {
            if (value < Long.MAX_VALUE / 2) {
                value = value shl 1
            } else {
                value = Long.MAX_VALUE
                break
            }
            if (value > maxDuration.toMillis()) {
                break
            }
        }
        value = minOf(value, maxDuration.toMillis())
        return Duration.ofMillis(value)
    }

}