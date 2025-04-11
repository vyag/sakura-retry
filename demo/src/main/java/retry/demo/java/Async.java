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

package retry.demo.java;

import retry.BackoffPolicies;
import retry.RetryPolicy;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static retry.Rules.maxAttempts;

public class Async {

    public static void main(String[] args) throws Exception {
        Random random = new Random(System.currentTimeMillis());

        try (ScheduledExecutorService executor = Executors.newScheduledThreadPool(4)) {
            RetryPolicy policy = new RetryPolicy.Builder(maxAttempts(99), BackoffPolicies.NONE).build();

            CompletableFuture<String> result1 = policy.submit(executor, () -> {
                int r = random.nextInt(10);
                if (r < 6) throw new IOException("foo failed");
                return "foo-" + r;
            });

            CompletableFuture<String> result2 = policy.submit(executor, () -> {
                int r = random.nextInt(10);
                if (r < 6) throw new IOException("bar failed");
                return "bar-" + r;
            });
            System.out.println(result1.get());
            System.out.println(result2.get());
        }
    }
}
