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
import sakura.retry.internal.DefaultBackoffExecutor
import sakura.retry.internal.DefaultDelaySubmitter
import sakura.retry.internal.DelaySubmitter
import sakura.retry.internal.RetryHandler
import java.lang.reflect.Proxy
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

/**
 * The retry class.
 *
 * @param condition The policy to retry.
 * @param backoffPolicy The back off strategy.
 * @param failureListeners The error handler.
 */
open class Retry private constructor(
    val condition: Condition,
    val backoffPolicy: BackoffPolicy,
    val failureListeners: List<FailureListener>,
    val backoffExecutor: BackoffExecutor,
    internal val delaySubmitter: DelaySubmitter,
    val name : String?
) {
    
    /**
     * Copy the retry instance with a new name.
     *
     * @param newName The new name.
     * @return The new retry instance.
     */
    fun withName(newName: String) : Retry {
        return Retry(
            condition = condition,
            backoffPolicy = backoffPolicy, 
            failureListeners = failureListeners, 
            backoffExecutor = backoffExecutor,
            delaySubmitter = delaySubmitter,
            name = newName
        )
    }

    /**
     * Call the given function with retry.
     * 
     * @param function The function to call.
     * @return The result of the function.
     * @throws Exception The original exception by the function call if the retry is aborted.
     */
    @Throws(Exception::class)
    fun <T> call(function: Callable<T>): T {
        var attemptCount = 1
        val startTime = Instant.now()
        while (true) {
            try {
                val result = function.call()
                LOG.debug("Finally {} success after {} attempts.", name, attemptCount)
                return result
            } catch (t: Throwable) {
                val context = Context.of(startTime, Instant.now(), attemptCount, t)
                val allowRetry = condition.check(context)
                val backOff = if (allowRetry) backoffPolicy.backoff(context) else Duration.ZERO
                for (failureListener in failureListeners) {
                    failureListener.onFailure(name, context, allowRetry, backOff)
                }
                if (allowRetry) {
                    backoffExecutor.backoff(backOff)
                    if (condition.check(context)) {
                        attemptCount++
                        continue
                    }
                }
                throw t
            }
        }
    }

    /**
     * Execute the given runnable with retry.
     *
     * @param runnable The runnable to execute.
     */
    @Throws(Exception::class)
    fun execute(runnable: Runnable) {
        call(runnable::run)
    }

    fun <T> wrapAsync(executor: ScheduledExecutorService, function: Supplier<CompletionStage<T>>): CompletableFuture<T> {
        val attemptCount = AtomicInteger(1)
        val startTime = Instant.now()
        val result = CompletableFuture<T>()
        executor.execute(object: Runnable {
            override fun run() {
                try {
                    function.get().whenComplete { t, u ->
                        if (u == null) {
                            result.complete(t)
                        } else {
                            handleFailure(u)
                        }
                    }
                } catch (t: Throwable) {
                    handleFailure(t)
                }
            }

            private fun handleFailure(u: Throwable) {
                val context = Context.of(startTime, Instant.now(), attemptCount.get(), u)
                val allowRetry = condition.check(context)
                val backOff = if (allowRetry) backoffPolicy.backoff(context) else Duration.ZERO
                for (failureListener in failureListeners) {
                    failureListener.onFailure(name, context, allowRetry, backOff)
                }
                if (allowRetry) {
                    attemptCount.incrementAndGet()
                    delaySubmitter.submit(executor, this, backOff)
                } else {
                    result.completeExceptionally(u)
                }
            }
        })
        return result
    }
    
    /**
     * Submit the given function with retry.
     *
     * @param executor The executor to submit the function.
     * @param function The function to submit.
     * @return The [Future] result of the function.
     */
    fun <T> callAsync(executor: ScheduledExecutorService, function: Callable<T>): CompletableFuture<T> {
        val attemptCount = AtomicInteger(1)
        val startTime = Instant.now()
        val result = CompletableFuture<T>()
        executor.execute(object: Runnable {
            override fun run() {
                try {
                    result.complete(function.call())
                } catch (t: Throwable) {
                    val context = Context.of(startTime, Instant.now(), attemptCount.get(), t)
                    val allowRetry = condition.check(context)
                    val backOff = if (allowRetry) backoffPolicy.backoff(context) else Duration.ZERO
                    for (failureListener in failureListeners) {
                        failureListener.onFailure(name, context, allowRetry, backOff)
                    }
                    if (allowRetry) {
                        attemptCount.incrementAndGet()
                        delaySubmitter.submit(executor, this, backOff)
                    } else {
                        result.completeExceptionally(t)
                    }
                }
            }
        })
        return result
    }

    /**
     * Creates a proxy for the given target object with retry.
     *
     * @param clazz The interface class of the target object.
     * @param target The target object.
     * @param name The optional name of the target object.
     * @return The proxy object.
     */
    fun <T> proxy(clazz: Class<T>, target: T): T {
        @Suppress("UNCHECKED_CAST")
        return (Proxy.newProxyInstance(
            Retry::class.java.classLoader, arrayOf(clazz),
            RetryHandler(this, target)
        ) as T)
    }

    /**
     * The Java-style builder for [Retry].
     */
    class Builder {

        private var condition: Condition = Conditions.TRUE
        
        private var backoffPolicy: BackoffPolicy = BackoffPolicies.NONE

        private val failureListeners: MutableList<FailureListener> = CopyOnWriteArrayList()

        private var name: String? = null

        private var backoffExecutor: BackoffExecutor = DefaultBackoffExecutor
        
        private var delaySubmitter: DelaySubmitter = DefaultDelaySubmitter

        /**
         * Set the name.
         *
         * @param name the name
         * @return the builder
         */
        fun setName(name: String?) = apply {
            this.name = name
        }

        /**
         * Set the condition.
         * 
         * @param condition the condition
         * @return the builder
         */
        fun setCondition(condition: Condition) = apply {
            this.condition = condition
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
         * Set the backoff executor.
         *
         * @param backoffExecutor the backoff executor
         * @return the builder
         */
        fun setBackoffExecutor(backoffExecutor: BackoffExecutor) = apply {
            this.backoffExecutor = backoffExecutor
        }

        /**
         * Set the delay submitter
         * 
         * @param delaySubmitter the delay submitter
         * @return the builder
         */
        internal fun setDelaySubmitter(delaySubmitter: DelaySubmitter) = apply {
            this.delaySubmitter = delaySubmitter
        }

        /**
         * Builds the [Retry].
         *
         * @return the [Retry]
         */
        fun build() : Retry {
            return Retry(
                condition = condition,
                backoffPolicy = backoffPolicy,
                failureListeners = Collections.unmodifiableList(failureListeners),
                backoffExecutor = backoffExecutor,
                delaySubmitter = delaySubmitter,
                name = name
            )
        }
    }

    private companion object {

        private val LOG = LoggerFactory.getLogger(Retry::class.java)

    }
}
