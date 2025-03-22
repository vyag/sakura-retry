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
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * The retry class.
 *
 * @param retryCondition The condition to retry.
 * @param abortCondition The condition to abort.
 * @param backOff The back off strategy.
 * @param errorHandler The error handler.
 */
class Retry @JvmOverloads constructor(
    val retryCondition: Condition = Condition.TRUE,
    val abortCondition: Condition = InstanceOf(InterruptedException::class.java, RuntimeException::class.java, Error::class.java),
    val backOff: BackOff = FixedDelay(1.seconds),
    val errorHandler: ErrorHandler = DefaultErrorHandler()
) {

    private val condition = !abortCondition and retryCondition

    @JvmSynthetic
    internal var backOffExecutor: BackOffExecutor = BackOffExecutor {
        Thread.sleep(it.toMillis(), (it.toNanos() % 1e6).toInt())
    }

    /**
     * Calls the given function with retry.
     *
     * @param name The optional name of the function.
     * @param function The function to call.
     * @return The result of the function.
     * @throws Throwable The original exception by the function call if the retry is aborted.
     */
    @JvmOverloads
    @JvmName("call")
    @Throws(Exception::class)
    internal fun <T> callWithThrows(name: String = "call", function: Callable<T>): T {
        return call(name, function)
    }

    /**
     * Calls the given function with retry.
     * 
     * @param name The optional name of the function.
     * @param function The function to call.
     * @return The result of the function.
     * @throws Throwable The original exception by the function call if the retry is aborted.
     */
    @JvmOverloads
    @JvmName("callWithNoThrowDeclaration")
    fun <T> call(name: String = "call", function: Callable<T>): T {
        var retryCount = 0
        val startTime = Instant.now()
        while (true) {
            try {
                val result = function.call()
                LOG.debug("Finally {} success after {} retries.", name, retryCount)
                return result
            } catch (t: Throwable) {
                val context = Context(startTime, Instant.now(), retryCount, t)
                val allowRetry = condition.check(context)
                LOG.debug("Check retry condition: {}, then allow retry: {}.", condition.toString(context), allowRetry)
                val backOff = if (allowRetry) backOff.backOff(context) else Duration.ZERO
                errorHandler.handle(context, allowRetry, backOff)
                if (allowRetry) {
                    backOffExecutor.backOff(backOff)
                    if (condition.check(context)) {
                        retryCount++
                        continue
                    }
                }
                logGiveUp(name, retryCount, t)
                throw t
            }
        }
    }

    /**
     * Submits the given function with retry.
     *
     * @param executor The executor to submit the function.
     * @param name The optional name of the function.
     * @param function The function to submit.
     * @return The [java.util.concurrent.Future] result of the function.
     */
    @JvmOverloads
    fun <T> submit(executor: ScheduledExecutorService, name: String = "call", function: Callable<T>): CompletableFuture<T> {
        var retryCount = 0
        val startTime = Instant.now()
        val result = CompletableFuture<T>()
        class Task : Runnable {
            override fun run() {
                try {
                    result.complete(function.call())
                } catch (t: Throwable) {
                    val context = Context(startTime, Instant.now(), retryCount, t)
                    val allowRetry = condition.check(context)
                    val backOff = if (allowRetry) backOff.backOff(context) else Duration.ZERO
                    errorHandler.handle(context, allowRetry, backOff)
                    if (allowRetry) {
                        if (condition.check(context)) {
                            retryCount++
                        }
                        executor.schedule(this, backOff.toMillis(), TimeUnit.MILLISECONDS)
                    } else {
                        logGiveUp(name, retryCount, t)
                        result.completeExceptionally(t)
                    }
                }
            }
        }
        executor.execute(Task())
        return result
    }     
    
    private fun logGiveUp(name: String, retryCount: Int, t: Throwable) {
        if (LOG.isDebugEnabled) {
            LOG.debug("Give up {} after {} retries, error: {}.", name, retryCount, t.toString())
        }
    }

    /**
     * Creates a proxy for the given target object with retry.
     *
     * @param clazz The interface class of the target object.
     * @param target The target object.
     * @param name The optional name of the target object.
     * @return The proxy object.
     */
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

        /**
         * The policy that never retries.
         */
        @JvmField
        val NONE = Retry(retryCondition = Condition.FALSE)
    }
}
