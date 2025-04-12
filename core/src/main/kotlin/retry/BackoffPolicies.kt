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
import kotlin.random.Random
import kotlin.time.toJavaDuration

object BackoffPolicies {

    /**
     * Convenience method to create a FixedDelay backoff policy.
     * 
     * @param duration The delay.
     * @return The FixedDelay backoff policy.
     */
    @JvmStatic
    fun fixedDelay(duration: Duration) : FixedDelay {
        return FixedDelay(duration)
    }

    /**
     * Convenience method to create a FixedDelay backoff policy.
     *
     * @param duration The delay.
     * @return The FixedDelay backoff policy.
     */
    @JvmStatic
    fun fixedDelay(duration: kotlin.time.Duration) : FixedDelay {
        return FixedDelay(duration)
    }
    
    /**
     * Convenience method to create a FixedDelay backoff policy.
     * 
     * @param seconds The delay in seconds.
     * @return The FixedDelay backoff policy.
     */
    @JvmStatic
    fun fixedDelayInSeconds(seconds: Long) : FixedDelay {
        return FixedDelay(Duration.ofSeconds(seconds))
    }

    /**
     * Convenience method to create an ExponentialDelay backoff policy.
     * 
     * @param initDuration The initial duration.
     * @param maxDuration The maximum duration.
     * @return The ExponentialDelay backoff policy.
     */
    @JvmStatic
    fun exponentialDelay(initDuration: Duration, maxDuration: Duration) : ExponentialDelay {
        return ExponentialDelay(initDuration, maxDuration)
    }
    
    /**
     * Convenience method to create an ExponentialDelay backoff policy.
     *
     * @param initSeconds The initial duration in seconds.
     * @param maxSeconds The maximum duration in seconds.
     * @return The ExponentialDelay backoff policy.
     */
    @JvmStatic
    fun exponentialDelayInSeconds(initSeconds: Long, maxSeconds: Long) : ExponentialDelay {
        return ExponentialDelay(Duration.ofSeconds(initSeconds), Duration.ofSeconds(maxSeconds))
    }
    
    /**
     * Convenience method to create a RandomDelay backoff policy.
     *
     * @param minDuration The minimum duration.
     * @param maxDuration The maximum duration.
     * @return The RandomDelay backoff policy.
     */
    @JvmStatic
    fun randomDelay(minDuration: Duration, maxDuration: Duration) : RandomDelay {
        return RandomDelay(minDuration, maxDuration)
    }
    
    /**
     * Convenience method to create a RandomDelay backoff policy.
     *
     * @param minSeconds The minimum duration in seconds.
     * @param maxSeconds The maximum duration in seconds.
     * @return The RandomDelay backoff policy.
     */
    @JvmStatic
    fun randomDelayInSeconds(minSeconds: Long, maxSeconds: Long) : RandomDelay {
        return RandomDelay(Duration.ofSeconds(minSeconds), Duration.ofSeconds(maxSeconds))
    }

    /**
     * No backoff.
     */
    @JvmField
    val NONE = BackoffPolicy { Duration.ZERO }
}

/**
 * Fixed delay back off.
 *
 * @param duration The delay.
 */
data class FixedDelay(val duration: Duration) : BackoffPolicy {

    /**
     * Constructs a fixed delay back off.
     *
     * @param duration The dealy.
     */
    constructor(duration: kotlin.time.Duration) : this(duration.toJavaDuration())

    override fun backoff(context: Context): Duration {
        return duration
    }
}

/**
 * Exponential backoff implementation.
 *
 * @property initDuration The initial duration.
 * @property maxDuration The maximum duration.
 */
data class ExponentialDelay(
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
        
        repeat (context.attemptCount - 1) {
            if (value < Long.MAX_VALUE / 2) {
                value = value shl 1
            } else {
                value = Long.MAX_VALUE
                return@repeat
            }
            if (value > maxDuration.toMillis()) {
                return@repeat
            }
        }
        value = minOf(value, maxDuration.toMillis())
        return Duration.ofMillis(value)
    }

}

/**
 * Constructs a random backoff implementation in the range of [minDuration] to [maxDuration].
 *
 * [minDuration] must be less than or equal to [maxDuration], otherwise an [IllegalArgumentException] will be thrown.
 *
 * Zero and negative durations are allowed.
 *
 * @throws IllegalArgumentException if [minDuration] is greater than [maxDuration].
 * @param minDuration The minimum duration.
 * @param maxDuration The maximum duration.
 */
data class RandomDelay(val minDuration: Duration, val maxDuration: Duration) : BackoffPolicy {

    /**
     * Constructs a random backoff implementation.
     *
     * @param minDuration The minimum duration.
     * @param maxDuration The maximum duration.
     */
    constructor(minDuration: kotlin.time.Duration, maxDuration: kotlin.time.Duration) : this(minDuration.toJavaDuration(), maxDuration.toJavaDuration())

    init {
        require(minDuration <= maxDuration) { "minDuration must be less than or equal to maxDuration" }
    }

    override fun backoff(context: Context): Duration {
        val value = random.nextLong(minDuration.toMillis(), maxDuration.toMillis())
        return Duration.ofMillis(value)
    }

    private companion object {
        private val random = Random(System.currentTimeMillis())
    }
}
