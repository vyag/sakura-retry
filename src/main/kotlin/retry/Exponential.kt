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

import java.time.Duration
import kotlin.time.toJavaDuration

/**
 * Exponential backoff implementation.
 *
 * @property initDuration The initial duration.
 * @property maxDuration The maximum duration.
 */
data class Exponential(
    val initDuration: Duration,
    val maxDuration: Duration
) : Backoff {
    
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
