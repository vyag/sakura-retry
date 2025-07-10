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
package sakura.retry

import org.slf4j.LoggerFactory
import sakura.retry.internal.BackoffExecutor
import sakura.retry.internal.DefaultBackoffExecutor
import sakura.retry.internal.RetryHandler
import java.lang.reflect.Proxy
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.*

/**
 * The retry class.
 *
 * @param retryPolicy The policy to retry.
 * @param backoffPolicy The back off strategy.
 * @param failureListeners The error handler.
 */
class Retry private constructor(
    val retryPolicy: RetryPolicy,
    val backoffPolicy: BackoffPolicy,
    val failureListeners: List<FailureListener>) {

    @JvmSynthetic
    internal var backoffExecutor: BackoffExecutor = DefaultBackoffExecutor()

    /**
     * Call the given function with retry.
     * 
     * @param name The optional name of the function.
     * @param function The function to call.
     * @return The result of the function.
     * @throws Exception The original exception by the function call if the retry is aborted.
     */
    @JvmOverloads
    @Throws(Exception::class)
    fun <T> call(name: String? = null, function: Callable<T>): T {
        var attemptCount = 1
        val startTime = Instant.now()
        while (true) {
            try {
                val result = function.call()
                LOG.debug("Finally {} success after {} attempts.", name, attemptCount)
                return result
            } catch (t: Throwable) {
                val context = Context(startTime, Instant.now(), attemptCount, t)
                val allowRetry = retryPolicy.check(context)
                val backOff = if (allowRetry) backoffPolicy.backoff(context) else Duration.ZERO
                for (failureListener in failureListeners) {
                    failureListener.onFailure(name, context, allowRetry, backOff)
                }
                if (allowRetry) {
                    backoffExecutor.backoff(backOff)
                    if (retryPolicy.check(context)) {
                        attemptCount++
                        continue
                    }
                }
                throw t
            }
        }
    }

    /**
     * Execute the given task with retry.
     *
     * @param name The optional name of the task.
     * @param runnable The task to execute.
     * @throws Exception The original exception by the task if the retry is aborted.
     * 
     * @see call
     */
    @JvmOverloads
    @Throws(Exception::class)
    fun execute(name: String? = null, runnable: Runnable) {
        call(name) {
            runnable.run()
        }
    }

    /**
     * Submit the given function with retry.
     *
     * @param executor The executor to submit the function.
     * @param name The optional name of the function.
     * @param function The function to submit.
     * @return The [Future] result of the function.
     */
    @JvmOverloads
    fun <T> call(executor: ScheduledExecutorService, name: String? = null, function: Callable<T>): CompletableFuture<T> {
        var attemptCount = 1
        val startTime = Instant.now()
        val result = CompletableFuture<T>()
        class Task : Runnable {
            override fun run() {
                try {
                    result.complete(function.call())
                } catch (t: Throwable) {
                    val context = Context(startTime, Instant.now(), attemptCount, t)
                    val allowRetry = retryPolicy.check(context)
                    val backOff = if (allowRetry) backoffPolicy.backoff(context) else Duration.ZERO
                    for (failureListener in failureListeners) {
                        failureListener.onFailure(name, context, allowRetry, backOff)
                    }
                    if (allowRetry) {
                        if (retryPolicy.check(context)) {
                            attemptCount++
                        }
                        executor.schedule(this, backOff.toMillis(), TimeUnit.MILLISECONDS)
                    } else {
                        result.completeExceptionally(t)
                    }
                }
            }
        }
        executor.execute(Task())
        return result
    }

    /**
     * Submit the given task with retry.
     *
     * @param executor The executor to submit the task.
     * @param name The optional name of the task.
     * @param task The task to execute.
     * @return The [Future] result of the task.
     * 
     * @see call
     */
    @JvmOverloads
    fun execute(executor: ScheduledExecutorService, name: String? = null, task: Runnable): CompletableFuture<Void?> {
        return call(executor, name) {
            task.run()
            null
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

    /**
     * The Java-style builder for [Retry].
     */
    class Builder {

        private var retryPolicy: RetryPolicy = RetryPolicies.TRUE
        
        private var backoffPolicy: BackoffPolicy = BackoffPolicies.NONE

        private val failureListeners: MutableList<FailureListener> = CopyOnWriteArrayList()
        
        /**
         * Set the retry policy.
         * 
         * @param retryPolicy the retry policy
         * @return the builder
         */
        fun setRetryPolicy(retryPolicy: RetryPolicy) = apply {
            this.retryPolicy = retryPolicy
        }
        
        /**
         * Set the backoff policy.
         *
         * @param backoffPolicy the backoff policy
         * @return the builder
         */
        fun setBackoffPolicy(backoffPolicy: BackoffPolicy) = apply {
            this.backoffPolicy = backoffPolicy
        }

        /**
         * Add a failure listener.
         *
         * @param failureListener the failure listener
         * @return the builder
         */
        fun addFailureListener(failureListener: FailureListener) = apply {
            this.failureListeners.add(failureListener)
        }

        /**
         * Builds the [Retry].
         *
         * @return the [Retry]
         */
        fun build() : Retry {
            return Retry(retryPolicy = retryPolicy, backoffPolicy = backoffPolicy, Collections.unmodifiableList(failureListeners))
        }
    }

    private companion object {

        private val LOG = LoggerFactory.getLogger(Retry::class.java)

    }
}
