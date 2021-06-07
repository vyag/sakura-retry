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

import com.github.yag.config.Value
import java.time.Duration
import kotlin.random.Random

class IntervalBackOffPolicy @JvmOverloads constructor(@Value var intervalMs: Long = 1000, @Value var maxIntervalMs: Long = intervalMs) : BackOffPolicy {

    private val random = Random(System.currentTimeMillis())

    override fun backOff(retryCount: Int, duration: Duration, error: Throwable): Duration {
        return Duration.ofMillis(random.nextLong(intervalMs, maxIntervalMs + 1))
    }
}
