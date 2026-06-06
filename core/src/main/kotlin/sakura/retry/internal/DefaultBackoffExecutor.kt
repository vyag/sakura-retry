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
package sakura.retry.internal

import sakura.retry.BackoffExecutor
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal object DefaultBackoffExecutor : BackoffExecutor {

    override fun backoff(duration: Duration) {
        if (!duration.isNegative && !duration.isZero) {
            Thread.sleep(duration.toMillis(), (duration.toNanos() % 1_000_000L).toInt())
        }
    }
}
