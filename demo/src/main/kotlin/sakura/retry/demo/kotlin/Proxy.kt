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
package sakura.retry.demo.kotlin

import sakura.retry.Conditions.maxAttempts
import sakura.retry.Retry
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable

/**
 * Demonstrates the retry proxy pattern using [Retry.proxy].
 *
 * A [java.util.concurrent.Callable] implementation is wrapped so that every
 * call to [Callable.call] is automatically retried on failure. This allows
 * transparent retry without modifying the original object.
 */
fun main() {
    val retry = Retry.Builder()
        .setCondition(maxAttempts(99))
        .build()
    val call = retry.proxy(Callable::class.java, Impl())
    println(call.call())
}

class Impl : Callable<Double> {
    var random: Random = Random(System.currentTimeMillis())

    @Throws(IOException::class)
    override fun call(): Double = random.nextDouble(10.0).takeUnless { it < 7 } ?: throw IOException("Too small")
}
