/*
 * Copyright 2025-2025 marks.yag@gmail.com
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
package retry.internal

import java.time.Duration

class DefaultBackoffExecutor(
    private val executor: (Duration) -> Unit = {
        Thread.sleep(it.toMillis(), (it.toNanos() % 1e6).toInt())
    }
) : BackoffExecutor {

    override fun backoff(duration: Duration) {
        if (!duration.isNegative && !duration.isZero) {
            val maxDuration = Duration.ofMillis(Long.MAX_VALUE)
            var left = duration
            while (left > Duration.ZERO) {
                val delta = minOf(maxDuration, left)
                executor.invoke(delta)    
                left -= delta
            }
        }
    }

}