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

import sakura.retry.RetryTemplate;

import static sakura.retry.BackoffPolicies.fixedDelayInSeconds;
import static sakura.retry.BackoffPolicies.randomDelayInSeconds;
import static sakura.retry.FailureListeners.logging;
import static sakura.retry.RetryPolicies.maxAttempts;

public class Simple {

    public static void main(String[] args) throws Exception {
        RetryTemplate policy = new RetryTemplate.Builder()
            .setRetryPolicy(maxAttempts(3))
            .setBackoffPolicy(fixedDelayInSeconds(10).plus(randomDelayInSeconds(0, 1)))
            .addFailureListener(logging())
            .build();
        policy.execute(
            () -> {
                System.out.println("Hello world!");
            }
        );
    }
}
