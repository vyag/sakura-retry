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

class Retry @JvmOverloads constructor(
    var retryPolicy: RetryPolicy = CountDownRetryPolicy(),
    var backOffPolicy: BackOffPolicy = IntervalBackOffPolicy(),
    var errorHandler: ErrorHandler = DefaultErrorHandler(),
    var checker: Checker = Checker.TRUE,
    var abortOn: Set<Class<out Throwable>> = setOf(
        InterruptedException::class.java,
        RuntimeException::class.java,
        Error::class.java
    )
) {

    @JvmOverloads
    fun <T> call(name: String = "call", body: Callable<T>): T {
        var retryCount = 0
        val startTime = System.nanoTime()

        while (true) {
            try {
                val result = body.call()
                if (retryCount > 0) {
                    LOG.debug("Finally {} success after {} retries.", name, retryCount)
                }
                return result
            } catch (t: Throwable) {
                if (abortOn.any { it.isInstance(t) }) {
                    throw t
                }

                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                val allowRetry = retryPolicy.allowRetry(retryCount, duration, t) && checker.check()
                val backOff = if (allowRetry) backOffPolicy.backOff(retryCount, duration, t) else Duration.ZERO

                errorHandler.handle(retryCount, duration, t, allowRetry, backOff)
                if (allowRetry) {
                    try {
                        Thread.sleep(backOff.toMillis(), (backOff.toNanos() % 1e6).toInt())
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

        @JvmStatic
        val NONE = Retry(retryPolicy = RetryPolicy.NONE)

        @JvmStatic
        val ALWAYS = Retry(retryPolicy = RetryPolicy.ALWAYS)

    }
}
