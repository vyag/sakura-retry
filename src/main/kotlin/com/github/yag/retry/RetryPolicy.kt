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

package com.github.yag.retry

import java.time.Duration

interface RetryPolicy {

    fun allowRetry(retryCount: Int, duration: Duration, error: Throwable): Boolean

    infix fun and(policy: RetryPolicy): RetryPolicy {
        return object : RetryPolicy {
            override fun allowRetry(retryCount: Int, duration: Duration, error: Throwable): Boolean {
                return this@RetryPolicy.allowRetry(retryCount, duration, error) && policy.allowRetry(retryCount, duration, error)
            }
        }
    }

    infix fun or(policy: RetryPolicy): RetryPolicy {
        return object : RetryPolicy {
            override fun allowRetry(retryCount: Int, duration: Duration, error: Throwable): Boolean {
                return this@RetryPolicy.allowRetry(retryCount, duration, error) || policy.allowRetry(retryCount, duration, error)
            }
        }
    }

    companion object {

        @JvmStatic
        val ALWAYS = object : RetryPolicy {
            override fun allowRetry(retryCount: Int, duration: Duration, error: Throwable): Boolean {
                return true
            }
        }

        @JvmStatic
        val NONE = object : RetryPolicy {
            override fun allowRetry(retryCount: Int, duration: Duration, error: Throwable): Boolean {
                return false
            }
        }
    }

}
