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

/**
 * The Java-style builder for [RetryPolicy]. We suggest that kotlin users use [RetryPolicy] constructor with default parameters instead.
 */
class RetryBuilder {

    private var retryCondition: Condition = PROTOTYPE.retryCondition
    private var abortCondition: Condition = PROTOTYPE.abortCondition
    private var backOff: Backoff = PROTOTYPE.backOff
    private var loggingStrategy: LoggingStrategy = PROTOTYPE.loggingStrategy

    /**
     * Sets the retry condition.
     *
     * @param retryCondition the retry condition
     */
    fun retryCondition(retryCondition: Condition) = apply {
        this.retryCondition = retryCondition
    }
    
    /**
     * Sets the abort condition.
     *
     * @param abortCondition the abort condition
     */
    fun abortCondition(abortCondition: Condition) = apply {
        this.abortCondition = abortCondition
    }
    
    /**
     * Sets the back off.
     *
     * @param backOff the back off
     */
    fun backOff(backOff: Backoff) : RetryBuilder = apply {
        this.backOff = backOff
    }
    
    /**
     * Sets the error handler.
     *
     * @param loggingStrategy the error handler
     */
    fun errorHandler(loggingStrategy: LoggingStrategy) = apply {
        this.loggingStrategy = loggingStrategy
    }
    
    /**
     * Builds the [RetryPolicy].
     *
     * @return the [RetryPolicy]
     */
    fun build() : RetryPolicy {
        return RetryPolicy(retryCondition, abortCondition, backOff, loggingStrategy)
    }

    private companion object {
        private val PROTOTYPE = RetryPolicy()
    } 
}