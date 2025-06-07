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
import com.github.vyag.retry.FailureListeners;
import com.github.vyag.retry.RetryTemplate;

import static com.github.vyag.retry.RetryPolicies.maxAttempts;

public class Simple {

    public static void main(String[] args) throws Exception {
        RetryTemplate policy = new RetryTemplate.Builder(maxAttempts(3), BackoffPolicies.NONE)
            .addFailureListener(FailureListeners.logging())
            .build();
        policy.call(
            () -> {
                System.out.println("Hello world!");
                return null;
            }
        );
    }
}
