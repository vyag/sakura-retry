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
package com.github.vyag.retry

import java.time.Duration

/**
 * Backoff policy.
 */
fun interface BackoffPolicy {

    /**
     * Returns the backoff duration.
     *
     * @param context the context
     * @return the backoff duration
     */
    fun backoff(context: Context): Duration
    
    /**
     * Returns a new backoff policy that is the sum of this backoff policy and the given backoff policy.
     *
     * @param backoffPolicy the backoff policy to add
     * @return a new backoff policy that is the sum of this backoff policy and the given backoff policy
     */
    infix operator fun plus(backoffPolicy: BackoffPolicy): BackoffPolicy {
        val self = this
        return BackoffPolicy { context -> self.backoff(context) + backoffPolicy.backoff(context) }
    }
}


