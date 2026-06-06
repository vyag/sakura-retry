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

import static sakura.retry.BackoffPolicies.fixedDelayInSeconds;
import static sakura.retry.Conditions.maxAttempts;
import static sakura.retry.FailureListeners.logging;

/**
 * Demonstrates the scoped name override using {@link Retry#withName}.
 *
 * Inside the {@code withName} block, the name "hard-work" is passed to the
 * {@link Retry#call} invocation, so failure listeners and log output identify
 * the call by this name instead of the default (object identity).
 */
public class WithName {

    public static void main(String[] args) throws Exception {
        Retry retry = new Retry.Builder()
            .setCondition(maxAttempts(5))
            .setBackoffPolicy(fixedDelayInSeconds(1))
            .addFailureListener(logging())
            .build();
        double result = retry.withName("hard-work", r -> {
            try {
                return r.call(() -> {
                    double v = new Random().nextDouble(10);
                    if (v < 7) throw new IOException("Too small: " + v);
                    return v;
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(result);
    }
}
