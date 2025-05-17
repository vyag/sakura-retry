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
package retry.demo.kotlin

import retry.BackoffPolicies
import retry.RetryPolicy
import retry.Rules
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable

fun main() {
    val policy = RetryPolicy.Builder(Rules.maxTimeElapsedInSeconds(1), BackoffPolicies.NONE).build()
    val call = policy.proxy(Callable::class.java, Impl())
    println(call.call())
}

class Impl : Callable<Double> {
    var random: Random = Random(System.currentTimeMillis())

    @Throws(IOException::class)
    override fun call(): Double = random.nextDouble(10.0).takeUnless { it < 7 } ?: throw IOException("Too small")
}
