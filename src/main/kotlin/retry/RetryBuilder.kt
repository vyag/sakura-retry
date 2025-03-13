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

class RetryBuilder private constructor() {

    private var retryCondition: Condition = PROTOTYPE.retryCondition
    private var abortCondition: Condition = PROTOTYPE.abortCondition
    private var backOff: BackOff = PROTOTYPE.backOff
    private var errorHandler: ErrorHandler = PROTOTYPE.errorHandler

    fun retryCondition(retryCondition: Condition) = apply {
        this.retryCondition = retryCondition
    }
    
    fun abortCondition(abortCondition: Condition) = apply {
        this.abortCondition = abortCondition
    }
    
    fun backOff(backOff: BackOff) : RetryBuilder = apply {
        this.backOff = backOff
    }
    
    fun errorHandler(errorHandler: ErrorHandler) = apply {
        this.errorHandler = errorHandler
    }
    
    fun build() : Retry {
        return Retry(retryCondition, abortCondition, backOff, errorHandler)
    }

    companion object {
        
        fun create() : RetryBuilder {
            return RetryBuilder()
        }
        
        private val PROTOTYPE = Retry()
    } 
}