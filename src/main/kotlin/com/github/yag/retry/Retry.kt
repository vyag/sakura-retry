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
import java.util.concurrent.Callable
import kotlin.random.Random

class Retry(
    internal var retryPolicy: RetryPolicy = CountDownRetryPolicy(),
    internal var backOffPolicy: BackOffPolicy = IntervalBackOffPolicy(),
    internal var errorHandler: ErrorHandler = DefaultErrorHandler(),
    internal var checker: Checker = Checker.TRUE,
    internal var backOffRandomRange: Double = 0.1,
    internal var abortOnRuntimeException: Boolean = true,
    internal var abortOnError: Boolean = true
) {

    @JvmOverloads
    fun <T> call(name: String = "call", body: Callable<T>): T {
        return call(name, body::call)
    }

    fun <T> call(name: String = "call", body: () -> T): T {
        var retryCount = 0
        val startTime = System.nanoTime()

        while (true) {
            try {
                val result = body()
                if (retryCount > 0) {
                    LOG.debug("Finally {} success after {} retries.", name, retryCount)
                }
                return result
            } catch (t: Throwable) {
                if (t is InterruptedException) {
                    throw t
                }

                if (abortOnRuntimeException && t is RuntimeException) {
                    throw t
                }

                if (abortOnError && t is Error) {
                    throw t
                }

                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                val allowRetry = retryPolicy.allowRetry(retryCount, duration, t) && checker.check()
                val backOff = if (allowRetry) backOffPolicy.backOff(retryCount, duration, t) else Duration.ZERO
                val finalBackOff = random(backOff, backOffRandomRange)

                errorHandler.handle(retryCount, duration, t, allowRetry, backOff)
                if (allowRetry) {
                    try {
                        Thread.sleep(finalBackOff.toMillis(), (finalBackOff.toNanos() % 1e6).toInt())
                    } catch (e: InterruptedException) {
                        throw e
                    }
                    if (checker.check()) {
                        retryCount++
                        continue
                    }
                }
                LOG.debug("Give up {} after {} retries, error: {}.", name, retryCount, t.toString())
                throw t
            }
        }
    }


    @JvmOverloads
    fun <T> proxy(clazz: Class<T>, target: T, name: String = target.toString()): T {
        @Suppress("UNCHECKED_CAST")
        return (Proxy.newProxyInstance(
            Retry::class.java.classLoader, arrayOf(clazz),
            RetryHandler(this, target, name)
        ) as T)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(Retry::class.java)

        private val random = Random(System.currentTimeMillis())

        @JvmStatic
        val NONE = max(0, Duration.ZERO)

        @JvmStatic
        val ALWAYS = duration(Duration.ofMillis(Long.MAX_VALUE))

        @JvmStatic
        fun max(maxRetries: Int, backOffInterval: Duration = Duration.ofSeconds(1)) =
            Retry(CountDownRetryPolicy(maxRetries, Long.MAX_VALUE), IntervalBackOffPolicy(backOffInterval.toMillis()))

        @JvmStatic
        fun duration(
            duration: Duration,
            backOffInterval: Duration = Duration.ofSeconds(1)
        ): Retry {
            return Retry(
                CountDownRetryPolicy(Int.MAX_VALUE, duration.toMillis()),
                IntervalBackOffPolicy(backOffInterval.toMillis())
            )
        }


        internal fun random(backOff: Duration, randomRange: Double): Duration {
            return if (randomRange == 0.0) {
                backOff
            } else {
                val scale = random.nextDouble(1 - randomRange, 1 + randomRange)
                Duration.ofNanos(
                    (backOff.toNanos() * scale).toLong()
                )
            }
        }
    }
}
