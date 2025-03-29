/*
 * Copyright 2025-2025 marks.yag@gmail.com
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

import java.util.*
import java.util.function.Function

/**
 * The Java-style builder for [RetryPolicy].
 */
class RetryPolicyBuilder(private val retryCondition: Condition, private val backoffPolicy: BackoffPolicy) {
    
    private val prototype: RetryPolicy = createPrototype(retryCondition, backoffPolicy)

    private var abortCondition: Condition = prototype.abortCondition
    
    private var failureListeners: MutableList<FailureListener> = prototype.failureListeners.toMutableList()
    
    /**
     * Sets the abort condition.
     *
     * @param abortCondition the abort condition
     */
    fun abortCondition(abortCondition: Condition) = apply {
        this.abortCondition = abortCondition
    }

    /**
     * Updates the abort condition.
     *
     * @param updater the updater
     */
    fun updateAbortCondition(updater: Function<Condition, Condition>) = apply {
        this.abortCondition = updater.apply(abortCondition)
    }

    /**
     * Adds the abort condition.
     *
     * @param abortCondition the abort condition
     */
    fun addAbortCondition(abortCondition: Condition) = apply {
        this.abortCondition = this.abortCondition or abortCondition
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
        return RetryPolicy(retryCondition = retryCondition, backoffPolicy = backoffPolicy, abortCondition = abortCondition, Collections.unmodifiableList(failureListeners))
    }

    private companion object {
        private fun createPrototype(retryCondition: Condition, backoffPolicy: BackoffPolicy) = RetryPolicy(retryCondition, backoffPolicy)
    } 
}