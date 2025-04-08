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
import retry.internal.BackoffExecutor
import retry.internal.DefaultBackoffExecutor
import retry.internal.RetryHandler
import java.lang.reflect.Proxy
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.*
import java.util.function.Function

/**
 * The retry class.
 *
 * @param retryRule The rule to retry.
 * @param abortRule The rule to abort.
 * @param backoffPolicy The back off strategy.
 * @param failureListeners The error handler.
 */
class RetryPolicy private constructor(
    val retryRule: Rule,
    val backoffPolicy: BackoffPolicy,
    val abortRule: Rule,
    val failureListeners: List<FailureListener>) {

    private val rule = !abortRule and retryRule

    @JvmSynthetic
    internal var backoffExecutor: BackoffExecutor = DefaultBackoffExecutor()

    /**
     * Calls the given function with retry.
     * 
     * @param name The optional name of the function.
     * @param function The function to call.
     * @return The result of the function.
     * @throws Exception The original exception by the function call if the retry is aborted.
     */
    @JvmOverloads
    @Throws(Exception::class)
    fun <T> call(name: String = "call", function: Callable<T>): T {
        var attemptCount = 1
        val startTime = Instant.now()
        while (true) {
            try {
                val result = function.call()
                LOG.debug("Finally {} success after {} retries.", name, attemptCount)
                return result
            } catch (t: Throwable) {
                val context = Context(startTime, Instant.now(), attemptCount, t)
                val allowRetry = rule.check(context)
                LOG.debug("Check retry rule: {}, then allow retry: {}.", rule.toString(context), allowRetry)
                val backOff = if (allowRetry) backoffPolicy.backoff(context) else Duration.ZERO
                for (failureListener in failureListeners) {
                    failureListener.onFailure(context, allowRetry, backOff)
                }
                if (allowRetry) {
                    backoffExecutor.backoff(backOff)
                    if (rule.check(context)) {
                        attemptCount++
                        continue
                    }
                }
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
        var attemptCount = 1
        val startTime = Instant.now()
        val result = CompletableFuture<T>()
        class Task : Runnable {
            override fun run() {
                try {
                    result.complete(function.call())
                } catch (t: Throwable) {
                    val context = Context(startTime, Instant.now(), attemptCount, t)
                    val allowRetry = rule.check(context)
                    val backOff = if (allowRetry) backoffPolicy.backoff(context) else Duration.ZERO
                    for (failureListener in failureListeners) {
                        failureListener.onFailure(context, allowRetry, backOff)
                    }
                    if (allowRetry) {
                        if (rule.check(context)) {
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
            RetryPolicy::class.java.classLoader, arrayOf(clazz),
            RetryHandler(this, target, name)
        ) as T)
    }

    /**
     * The Java-style builder for [RetryPolicy].
     */
    class Builder(private val retryRule: Rule, private val backoffPolicy: BackoffPolicy) {

        private var abortRule: Rule = DEFAULT_ABORT_RULE

        private var failureListeners: MutableList<FailureListener> = CopyOnWriteArrayList(DEFAULT_FAILURE_LISTENERS)

        /**
         * Sets the abort rule.
         *
         * @param abortRule the abort rule
         */
        fun abortRule(abortRule: Rule) = apply {
            this.abortRule = abortRule
        }

        /**
         * Updates the abort rule.
         *
         * @param updater the updater
         */
        fun updateAbortRule(updater: Function<Rule, Rule>) = apply {
            this.abortRule = updater.apply(abortRule)
        }

        /**
         * Adds the abort rule.
         *
         * @param abortRule the abort rule
         */
        fun addAbortRule(abortRule: Rule) = apply {
            this.abortRule = this.abortRule or abortRule
        }

        /**
         * Sets the failure listeners.
         *
         * @param failureListeners the failure listeners
         */
        fun failureListeners(failureListeners: MutableList<FailureListener>) = apply {
            this.failureListeners = failureListeners
        }

        /**
         * Add a failure listener.
         *
         * @param failureListener the failure listener
         */
        fun addFailureListener(failureListener: FailureListener) = apply {
            this.failureListeners.add(failureListener)
        }

        /**
         * Clear the failure listeners.
         */
        fun clearFailureListeners() = apply {
            this.failureListeners.clear()
        }

        /**
         * Builds the [RetryPolicy].
         *
         * @return the [RetryPolicy]
         */
        fun build() : RetryPolicy {
            return RetryPolicy(retryRule = retryRule, backoffPolicy = backoffPolicy, abortRule = abortRule, Collections.unmodifiableList(failureListeners))
        }

        private companion object {
            private val DEFAULT_ABORT_RULE: Rule = Rules.UNRECOVERABLE_EXCEPTIONS

            private val DEFAULT_FAILURE_LISTENERS: List<FailureListener> = listOf(FailureListeners.logging(Rules.TRUE, Rules.TRUE))
        }
    }

    private companion object {

        private val LOG = LoggerFactory.getLogger(RetryPolicy::class.java)

    }
}
