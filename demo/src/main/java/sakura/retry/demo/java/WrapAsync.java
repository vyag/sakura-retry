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

package sakura.retry.demo.java;

import sakura.retry.Retry;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static sakura.retry.BackoffPolicies.fixedDelayInSeconds;
import static sakura.retry.Conditions.maxAttempts;
import static sakura.retry.FailureListeners.logging;

/**
 * Demonstrates asynchronous retry with a {@link java.util.concurrent.CompletionStage}
 * supplier using {@link Retry#wrapAsync}.
 *
 * The supplier returns a {@link CompletableFuture} that may complete
 * exceptionally. On failure the operation is retried according to the
 * configured policy. This is useful when integrating with APIs that already
 * return futures.
 */
public class WrapAsync {

    public static void main(String[] args) throws Exception {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        try {
            Retry retry = new Retry.Builder()
                .setCondition(maxAttempts(5))
                .setBackoffPolicy(fixedDelayInSeconds(1))
                .addFailureListener(logging())
                .build();
            CompletableFuture<Double> future = retry.wrapAsync(executor, () -> {
                try {
                    double v = new Random().nextDouble(10);
                    if (v < 7) throw new IOException("Too small: " + v);
                    return CompletableFuture.completedFuture(v);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            System.out.println(future.get());
        } finally {
            executor.shutdown();
        }
    }
}
