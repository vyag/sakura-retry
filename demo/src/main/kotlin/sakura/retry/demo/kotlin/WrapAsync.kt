/*
 * Copyright 2025-2025 marks.yag@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sakura.retry.demo.kotlin

import sakura.retry.BackoffPolicies.fixedDelayInSeconds
import sakura.retry.Conditions.maxAttempts
import sakura.retry.FailureListeners.logging
import sakura.retry.Retry
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

/**
 * Demonstrates asynchronous retry with a [java.util.concurrent.CompletionStage]
 * supplier using [Retry.wrapAsync].
 *
 * The supplier returns a [CompletableFuture] that may complete exceptionally.
 * On failure the operation is retried according to the configured policy.
 * This is useful when integrating with APIs that already return futures.
 */
fun main() {
    val executor = Executors.newScheduledThreadPool(2)
    val retry = Retry.Builder()
        .setCondition(maxAttempts(5))
        .setBackoffPolicy(fixedDelayInSeconds(1))
        .addFailureListener(logging())
        .build()
    val future = retry.wrapAsync(executor) {
        val v = Random().nextDouble(10.0)
        if (v < 7) throw IOException("Too small: $v")
        CompletableFuture.completedFuture(v)
    }
    println(future.get())
    executor.shutdown()
}
