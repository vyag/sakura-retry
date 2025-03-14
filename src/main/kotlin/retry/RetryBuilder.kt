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
 * The Java-style builder for [Retry]. We suggest that kotlin users use [Retry] constructor with default parameters instead.
 */
class RetryBuilder {

    private var retryCondition: Condition = PROTOTYPE.retryCondition
    private var abortCondition: Condition = PROTOTYPE.abortCondition
    private var backOff: BackOff = PROTOTYPE.backOff
    private var errorHandler: ErrorHandler = PROTOTYPE.errorHandler

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
    fun backOff(backOff: BackOff) : RetryBuilder = apply {
        this.backOff = backOff
    }
    
    /**
     * Sets the error handler.
     *
     * @param errorHandler the error handler
     */
    fun errorHandler(errorHandler: ErrorHandler) = apply {
        this.errorHandler = errorHandler
    }
    
    /**
     * Builds the [Retry].
     *
     * @return the [Retry]
     */
    fun build() : Retry {
        return Retry(retryCondition, abortCondition, backOff, errorHandler)
    }

    companion object {
        private val PROTOTYPE = Retry()
    } 
}