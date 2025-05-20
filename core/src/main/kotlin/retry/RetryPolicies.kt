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
import kotlin.time.toJavaDuration

object RetryPolicies {

    /**
     * A policy that always returns true.
     */
    @JvmField
    val TRUE = object: RetryPolicy {
        override fun check(context: Context): Boolean {
            return true
        }

        override fun toString(): String {
            return "true"
        }
    }

    /**
     * A policy that always returns false.
     */
    @JvmField
    val FALSE = object: RetryPolicy {
        override fun check(context: Context): Boolean {
            return false
        }

        override fun toString(): String {
            return "false"
        }
    }
    
    /**
     * Convenience method to create a MaxAttempts policy.
     *
     * @param amount the maximum number of attempts
     * @see [MaxAttempts]
     */
    @JvmStatic
    fun maxAttempts(amount: Int): MaxAttempts {
        return MaxAttempts(amount)
    }
    
    /**
     * Convenience method to create a MaxTimeElapsed policy.
     *
     * @param duration The duration.
     * @see [MaxTimeElapsed]
     */
    @JvmStatic
    fun maxTimeElapsed(duration: Duration): MaxTimeElapsed {
        return MaxTimeElapsed(duration)
    }

    /**
     * Convenience method to create a MaxTimeElapsed policy.
     *
     * @param duration The duration.
     * @see [MaxTimeElapsed]
     */
    @JvmStatic
    fun maxTimeElapsed(duration: kotlin.time.Duration): MaxTimeElapsed {
        return MaxTimeElapsed(duration)
    }
    
    /**
     * Convenience method to create a MaxTimeElapsed policy.
     *
     * @param seconds the duration in seconds
     * @see [MaxTimeElapsed]
     */
    @JvmStatic
    fun maxTimeElapsedInSeconds(seconds: Long): MaxTimeElapsed {
        return MaxTimeElapsed(Duration.ofSeconds(seconds))
    }
    
    /**
     * Convenience method to create an ExceptionType policy.
     * 
     * @return the policy
     * @see [ExceptionType]
     */
    @JvmStatic
    fun runtimeException(): ExceptionType {
        return ExceptionType(RuntimeException::class.java)
    }
    
    /**
     * Convenience method to create an ExceptionType policy.
     *
     * @return the policy
     * @see [ExceptionType]
     */
    @JvmStatic
    fun error(): ExceptionType {
        return ExceptionType(Error::class.java)
    }
    
    /**
     * Convenience method to create an ExceptionType policy.
     *
     * @return the policy
     * @see [ExceptionType]
     */
    @JvmStatic
    fun interruptedException(): ExceptionType {
        return ExceptionType(InterruptedException::class.java)
    }
}

/**
 * The policy check if the attempt count is less than the given number.
 *
 * @param amount the maximum number of attempts
 */
data class MaxAttempts(val amount: Int) : RetryPolicy {

    init {
        require(amount > 1) { "amount must be greater than 1" }
    }

    override fun check(context: Context): Boolean {
        return context.attemptCount < amount
    }

    override fun toString(): String {
        return "context.attemptCount < $amount"
    }

    override fun toString(context: Context): String {
        return "context.attemptCount=${context.attemptCount} < $amount"
    }
}

/**
 * The policy check if the duration is less than the given duration.
 *
 * @param duration The duration.
 */
data class MaxTimeElapsed(val duration: Duration) : RetryPolicy {

    /**
     * Constructs a max time elapsed policy.
     *
     * @param duration The duration.
     */
    constructor(duration: kotlin.time.Duration) : this(duration.toJavaDuration())

    init {
        require(duration > Duration.ZERO) { "duration must be greater than 0" }
    }

    override fun check(context: Context): Boolean {
        return context.getDuration() < duration
    }

    override fun toString(): String {
        return "context.duration < $duration"
    }

    override fun toString(context: Context): String {
        return "context.duration=${context.getDuration()} < $duration"
    }
}

/**
 * The policy check if the failures is an instance of one of the given classes.
 *
 * @param failures the classes of the failures
 */
data class ExceptionType(val failures: Set<Class<out Throwable>>) : RetryPolicy {

    /**
     * The policy check if the failure is an instance of one of the given classes.
     *
     * @param failures the classes of the failures
     */
    constructor(vararg failures: Class<out Throwable>) : this(failures.toSet())

    override fun check(context: Context): Boolean {
        val failure = context.failure
        return failures.contains(failure.javaClass) || failures.any { it.isInstance(failure) }
    }

    override fun toString(): String {
        return "context.failure is in $failures"
    }

    override fun toString(context: Context): String {
        return "context.failure=${context.failure} is in $failures"
    }
}
