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

package retry

import org.slf4j.LoggerFactory
import retry.internal.BackOffExecutor
import retry.internal.RetryHandler
import java.lang.reflect.Proxy
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Function

class Retry {

    var retryCondition: Condition = Condition.TRUE

    var abortCondition: Condition = InstanceOf(InterruptedException::class.java, RuntimeException::class.java, Error::class.java)

    var backOff: BackOff = Interval()

    var errorHandler: ErrorHandler = DefaultErrorHandler()

    internal var backOffExecutor = BackOffExecutor {
        Thread.sleep(it.toMillis(), (it.toNanos() % 1e6).toInt())
    }

    @JvmOverloads
    fun <T> call(name: String = "call", body: Function<Long, T>): T {
        var retryCount = 0
        val startTime = System.nanoTime()

        val condition = !abortCondition and retryCondition

        while (true) {
            try {
                val result = body.apply(System.nanoTime() - startTime)
                LOG.debug("Finally {} success after {} retries.", name, retryCount)
                return result
            } catch (t: Throwable) {
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                val context = Context(retryCount, duration, t)
                val allowRetry = condition.match(context)
                val backOff = if (allowRetry) backOff.backOff(context) else Duration.ZERO

                errorHandler.handle(context, allowRetry, backOff)
                if (allowRetry) {
                    backOffExecutor.backOff(backOff)
                    if (condition.match(context)) {
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
    fun <T> submit(executor: ScheduledExecutorService, name: String = "call", body: Function<Long, T>): CompletableFuture<T> {
        var retryCount = 0
        val startTime = System.nanoTime()

        val result = CompletableFuture<T>()

        val condition = !abortCondition and retryCondition

        class Task : Runnable {
            override fun run() {
                try {
                    result.complete(body.apply(System.nanoTime() - startTime))
                } catch (t: Throwable) {
                    val duration = Duration.ofNanos(System.nanoTime() - startTime)
                    val context = Context(retryCount, duration, t)
                    val allowRetry = condition.match(context)
                    val backOff = if (allowRetry) backOff.backOff(context) else Duration.ZERO

                    errorHandler.handle(context, allowRetry, backOff)
                    if (allowRetry) {
                        executor.schedule(this, backOff.toMillis(), TimeUnit.MILLISECONDS)
                        if (condition.match(context)) {
                            retryCount++
                        }
                    } else {
                        LOG.debug("Give up {} after {} retries, error: {}.", name, retryCount, t.toString())
                        result.completeExceptionally(t)
                    }
                }
            }
        }

        executor.execute(Task())
        return result
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
        val NONE = Retry().apply {
            retryCondition = Condition.FALSE
        }

        @JvmStatic
        val ALWAYS = Retry().apply {
            abortCondition = Condition.FALSE
        }

        @JvmStatic
        @JvmOverloads
        fun create(config: Retry.() -> Unit = {}) : Retry {
            return Retry().apply {
                config()
            }
        }

        @JvmStatic
        fun eventually(maxTimeElapsed: Long, unit: TimeUnit = TimeUnit.SECONDS, backOffTimeMs: Long = minOf(100, unit.toMillis(maxTimeElapsed)) / 10) : Retry {
            return create {
                abortCondition = Condition.FALSE
                errorHandler = DefaultErrorHandler(Condition.FALSE, Condition.TRUE)
                retryCondition = MaxTimeElapsed(unit.toMillis(maxTimeElapsed))
                backOff = Interval(backOffTimeMs)
            }
        }

        @JvmStatic
        fun <T> eventually(maxTimeElapsed: Long, unit: TimeUnit = TimeUnit.SECONDS, backOffTimeMs: Long = minOf(100, unit.toMillis(maxTimeElapsed)) / 10, body: Function<Long, T>) : T {
            return eventually(maxTimeElapsed, unit, backOffTimeMs).call(body = body)
        }
    }
}
