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
import kotlin.random.Random
import kotlin.random.nextLong

data class Interval @JvmOverloads constructor(val minIntervalMs: Long = 1000, val maxIntervalMs: Long = minIntervalMs) : BackOff {

    private val intervalMs = random.nextLong(LongRange(minIntervalMs,  maxIntervalMs))

    override fun backOff(context: Context): Duration {
        return Duration.ofMillis(intervalMs)
    }

    companion object {
        @JvmStatic
        private val random = Random(System.currentTimeMillis())
    }
}
