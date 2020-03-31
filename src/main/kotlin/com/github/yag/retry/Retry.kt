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

import org.slf4j.LoggerFactory
import java.lang.reflect.Proxy
import java.time.Duration
import kotlin.random.Random

class Retry(
    private val retryPolicy: RetryPolicy,
    private val backOffPolicy: BackOffPolicy,
    private val errorHandler: ErrorHandler = DefaultErrorHandler(),
    private val backoffRandomRange: Double = 0.1
) {

    private val random = Random(System.currentTimeMillis())

    fun <T> call(name: String = "call", body: () -> T): T {
        var retryCount = 0
        val startTime = System.nanoTime()

        while (true) {
            try {
                retryPolicy.check()
                val result = body()
                if (retryCount > 0) {
                    LOG.debug("Finally {} success after {} retries.", name, retryCount)
                }
                return result
            } catch (t: Throwable) {
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                val allowRetry = retryPolicy.allowRetry(retryCount, duration, t)
                val backOff = if (allowRetry) backOffPolicy.backOff(retryCount, duration, t) else Duration.ZERO
                val finalBackOff = if (backoffRandomRange == 0.0)
                    backOff
                else
                    Duration.ofNanos(backOff.toNanos() * random.nextDouble(1 - backoffRandomRange, 1 + backoffRandomRange).toLong())

                errorHandler.handle(retryCount, duration, t, allowRetry, backOff)
                if (allowRetry) {
                    Thread.sleep(finalBackOff.toMillis(), finalBackOff.toNanosPart() % 1_000_000)
                    retryCount++
                    continue
                }
                LOG.warn("Give up {} after {} retries, error: {}.", name, retryCount, t.toString())
                throw t
            }
        }
    }

    fun <T> proxy(clazz: Class<T>, target: T, name: String = target.toString()): T {
        @Suppress("UNCHECKED_CAST")
        return (Proxy.newProxyInstance(
            Retry::class.java.classLoader, arrayOf(clazz),
            RetryHandler(this, target, name)
        ) as T)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Retry::class.java)

        @JvmStatic
        val NONE = of(0, 0)

        fun of(maxRetries: Int, backOffIntervalMs: Long) = Retry(CountDownRetryPolicy(maxRetries, Long.MAX_VALUE), IntervalBackOffPolicy(backOffIntervalMs))
    }
}