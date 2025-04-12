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

object Rules {

    /**
     * A rule that always returns true.
     */
    @JvmField
    val TRUE = object: Rule {
        override fun check(context: Context): Boolean {
            return true
        }

        override fun toString(): String {
            return "true"
        }
    }

    /**
     * A rule that always returns false.
     */
    @JvmField
    val FALSE = object: Rule {
        override fun check(context: Context): Boolean {
            return false
        }

        override fun toString(): String {
            return "false"
        }
    }
    
    /**
     * Convenience method to create a MaxAttempts rule.
     *
     * @param amount the maximum number of attempts
     */
    @JvmStatic
    fun maxAttempts(amount: Int): MaxAttempts {
        return MaxAttempts(amount)
    }
    
    /**
     * Convenience method to create a MaxTimeElapsed rule.
     *
     * @param duration The duration.
     */
    @JvmStatic
    fun maxTimeElapsed(duration: Duration): MaxTimeElapsed {
        return MaxTimeElapsed(duration)
    }

    /**
     * Convenience method to create a MaxTimeElapsed rule.
     *
     * @param duration The duration.
     */
    @JvmStatic
    fun maxTimeElapsed(duration: kotlin.time.Duration): MaxTimeElapsed {
        return MaxTimeElapsed(duration)
    }
    
    /**
     * Convenience method to create a MaxTimeElapsed rule.
     *
     * @param seconds
     */
    @JvmStatic
    fun maxTimeElapsedInSeconds(seconds: Long): MaxTimeElapsed {
        return MaxTimeElapsed(Duration.ofSeconds(seconds))
    }
    
    /**
     * Convenience method to create an ExceptionIn rule.
     * 
     * @param errors the classes of the errors
     */
    @JvmStatic
    fun exceptionIn(vararg errors: Class<out Throwable>): ExceptionIn {
        return ExceptionIn(*errors)
    }

    /**
     * A rule that returns true if the error is unrecoverable:
     * - [InterruptedException]
     * - [RuntimeException]
     * - [Error]
     *
     * This rule is used by the default [RetryPolicy] to determine if the retry is allowed.
     *
     * @see [RetryPolicy]
     * @see [ExceptionIn]
     */
    @JvmField
    val UNRECOVERABLE_EXCEPTIONS = exceptionIn(InterruptedException::class.java, RuntimeException::class.java, Error::class.java)
}

/**
 * The rule check if the attempt count is less than the given number.
 *
 * @param amount the maximum number of attempts
 */
data class MaxAttempts(val amount: Int) : Rule {

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
 * The rule check if the duration is less than the given duration.
 *
 * @param duration The duration.
 */
data class MaxTimeElapsed(val duration: Duration) : Rule {

    /**
     * Constructs a max time elapsed rule.
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
 * The rule check if the failures is an instance of one of the given classes.
 *
 * @param failures the classes of the failures
 */
data class ExceptionIn(val failures: Set<Class<out Throwable>>) : Rule {

    /**
     * The rule check if the failure is an instance of one of the given classes.
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
