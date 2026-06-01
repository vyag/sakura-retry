/*
 * Copyright 2018-2020 marks.yag@gmail.com
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

package sakura.retry

import sakura.retry.internal.Utils.toReadableString
import java.time.Duration
import java.time.Instant

/**
 * Represents the context of the retry operation.
 * 
 * @property startTime The start time of the retry operation.
 * @property now The current time.
 * @property attemptCount The number of attempts.
 * @property failure The failure that occurred during the retry operation.
 */
@ConsistentCopyVisibility
data class Context private constructor(val startTime: Instant, val now: Instant, val attemptCount: Int, val failure: Throwable) {
    
    init {
        require(attemptCount > 0) { "attemptCount must be greater than 0" }
    }
    
    /**
     * The duration between [startTime] and [now].
     *
     * @return the duration
     */
    fun getDuration(): Duration = Duration.between(startTime, now)
    
    override fun toString(): String {
        val duration = Duration.between(startTime, now)
        return "(startTime=${startTime}, now=${now}, attemptCount=$attemptCount, duration=${duration.toReadableString()}, failure: $failure)"
    }

    companion object {
        /**
         * Create a new [Context] instance.
         *
         * @param startTime the start time of the retry operation
         * @param now the current time
         * @param attemptCount the number of attempts (must be greater than 0)
         * @param failure the failure that occurred
         * @return the context
         */
        @JvmStatic
        fun of(startTime: Instant, now: Instant, attemptCount: Int, failure: Throwable): Context {
            return Context(startTime, now, attemptCount, failure)
        }
    }
}
