/*
 * Copyright 2018-2020 marks.yag@gmail.com
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

package com.github.yag.retry

class RetryBuilder {

    private val prototype = Retry()

    fun retryPolicy(retryPolicy: RetryPolicy) = apply {
        prototype.retryPolicy = retryPolicy
    }

    fun max(retries: Int) = apply {
        prototype.retryPolicy = CountDownRetryPolicy(retries)
    }

    fun backOffPolicy(backOffPolicy: BackOffPolicy) = apply {
        prototype.backOffPolicy = backOffPolicy
    }

    fun backOff(minMs: Long, maxMs: Long = minMs) = apply {
        prototype.backOffPolicy = IntervalBackOffPolicy(minMs, maxMs)
    }

    fun errorHandler(errorHandler: ErrorHandler) = apply {
        prototype.errorHandler = errorHandler
    }

    fun checker(checker: Checker) = apply {
        prototype.checker = checker
    }

    fun backOffRandomRange(backOffRandomRange: Double) = apply {
        prototype.backOffRandomRange = backOffRandomRange
    }

    fun abortOn(abortOn: Set<Class<out Throwable>>) = apply {
        prototype.abortOn = abortOn
    }

    fun build() = prototype
}
