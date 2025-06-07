/*
 * Copyright 2025-2025 marks.yag@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.vyag.retry.demo.kotlin

import com.github.vyag.retry.BackoffPolicies.fixedDelayInSeconds
import com.github.vyag.retry.Context
import com.github.vyag.retry.RetryTemplate
import com.github.vyag.retry.RetryPolicies
import java.io.IOException
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors

fun main() {
    val random = Random(System.currentTimeMillis())
    Executors.newScheduledThreadPool(4).use { executor ->
        val policy = RetryTemplate.Builder(RetryPolicies.TRUE, fixedDelayInSeconds(1))
            .addFailureListener { call: String?, context: Context, allowRetry: Boolean, backOffDuration: Duration ->
                println("Call $call, attempt ${context.attemptCount} failed: (${context.failure.message})")
            }.build()
        (0 until 3).map { 
            policy.submit(executor, "call$it") {
                random.nextDouble(10.0).takeUnless { it < 7 } ?: throw IOException("Too small")    
            } 
        }.map { 
            it.thenAccept(::println)
        }.forEach {
            it.join()
        }
    }
}
