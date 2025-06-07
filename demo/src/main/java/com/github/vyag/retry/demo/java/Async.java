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

package com.github.vyag.retry.demo.java;

import com.github.vyag.retry.BackoffPolicies;
import com.github.vyag.retry.RetryTemplate;
import com.github.vyag.retry.RetryPolicies;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.IntStream;

public class Async {

    public static void main(String[] args) {
        Random random = new Random(System.currentTimeMillis());
        try (ScheduledExecutorService executor = Executors.newScheduledThreadPool(4)) {
            RetryTemplate policy = new RetryTemplate.Builder(RetryPolicies.TRUE, BackoffPolicies.fixedDelayInSeconds(1))
                .addFailureListener((call, context, allowRetry, backOffDuration) ->
                    System.out.println("Call " + call + ", attempt " + context.getAttemptCount() + " failed: (" + context.getFailure().getMessage() + ")"))
                .build();
            IntStream.range(0, 3).mapToObj(value -> policy.submit(executor, "call-" + value, () -> {
                double d = random.nextDouble(10);
                if (d < 8) throw new IOException("Too small");
                return d;
            })).map(i -> i.thenAccept(System.out::println))
                .toList()
                .forEach(CompletableFuture::join);
        }
    }
}
