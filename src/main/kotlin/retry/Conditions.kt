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

object Conditions {

    /**
     * A condition that always returns true.
     */
    @JvmField
    val TRUE = object: Condition {
        override fun check(context: Context): Boolean {
            return true
        }

        override fun toString(): String {
            return "true"
        }
    }

    /**
     * A condition that always returns false.
     */
    @JvmField
    val FALSE = object: Condition {
        override fun check(context: Context): Boolean {
            return false
        }

        override fun toString(): String {
            return "false"
        }
    }

    /**
     * A condition that returns true if the error is unrecoverable:
     * - [InterruptedException]
     * - [RuntimeException]
     * - [Error]
     *
     * This condition is used by the default [RetryPolicy] to determine if the retry is allowed.
     *
     * @see [RetryPolicy]
     * @see [InstanceOf]
     */
    @JvmField
    val UNRECOVERABLE_EXCEPTIONS = InstanceOf(InterruptedException::class.java, RuntimeException::class.java, Error::class.java)
}

/**
 * The condition check if the retry count is less than the given number.
 *
 * @param maxRetries the maximum number of retries
 */
data class MaxRetries(val maxRetries: Int) : Condition {

    override fun check(context: Context): Boolean {
        return context.retryCount < maxRetries
    }

    override fun toString(): String {
        return "context.retryCount < $maxRetries"
    }

    override fun toString(context: Context): String {
        return "context.retryCount=${context.retryCount} < $maxRetries"
    }
}

/**
 * The condition check if the duration is less than the given duration.
 *
 * @param duration The duration.
 */
data class MaxTimeElapsed(val duration: Duration) : Condition {

    /**
     * Constructs a max time elapsed condition.
     *
     * @param duration The duration.
     */
    constructor(duration: kotlin.time.Duration) : this(duration.toJavaDuration())

    override fun check(context: Context): Boolean {
        return context.duration().toMillis() < duration.toMillis()
    }

    override fun toString(): String {
        return "context.duration < $duration"
    }

    override fun toString(context: Context): String {
        return "context.duration=${context.duration()} < $duration"
    }
}

/**
 * The condition check if the error is an instance of one of the given classes.
 *
 * @param errors the classes of the errors
 */
data class InstanceOf(val errors: Set<Class<out Throwable>>) : Condition {

    /**
     * The condition check if the error is an instance of one of the given classes.
     *
     * @param errors the classes of the errors
     */
    constructor(vararg errors: Class<out Throwable>) : this(errors.toSet())

    override fun check(context: Context): Boolean {
        val error = context.error
        return errors.contains(error.javaClass) || errors.any { it.isInstance(error) }
    }

    override fun toString(): String {
        return "context.error is in $errors"
    }

    override fun toString(context: Context): String {
        return "context.error=${context.error} is in $errors"
    }
}
