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

package retry

import retry.internal.Utils.toReadableString
import java.time.Duration
import java.time.Instant

/**
 * Represents the context of the retry operation.
 * 
 * @property startTime The start time of the retry operation.
 * @property now The current time.
 * @property retryCount The number of retries.
 * @property error The error that occurred during the retry operation.
 */
data class Context(val startTime: Instant, val now: Instant, val retryCount: Int, val error: Throwable) {
    
    fun duration() = Duration.between(startTime, now)
    
    override fun toString(): String {
        val duration = Duration.between(startTime, now)
        return "(startTime=${startTime}, now=${now}, retryCount=$retryCount, duration=${duration.toReadableString()}, error: $error)"
    }
}
