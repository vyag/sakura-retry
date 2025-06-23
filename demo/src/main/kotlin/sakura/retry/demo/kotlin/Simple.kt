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
import sakura.retry.BackoffPolicies.randomDelayInSeconds
import sakura.retry.FailureListeners.logging
import sakura.retry.RetryTemplate
import sakura.retry.RetryPolicies.maxAttempts
import sakura.retry.RetryPolicies.runtimeException

fun main() {
    val retryPolicy = maxAttempts(10) and !runtimeException()
    val backoffPolicy = fixedDelayInSeconds(10) + randomDelayInSeconds(0, 1)
    val template = RetryTemplate.Builder(retryPolicy, backoffPolicy)
        .addFailureListener(logging())
        .build()
    template.call {
        println("maybe fail")
    }
}

