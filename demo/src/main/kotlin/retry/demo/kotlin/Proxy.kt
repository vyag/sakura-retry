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
import retry.MaxAttempts
import retry.RetryPolicy
import java.io.IOException
import java.util.*

fun main() {
    val policy = RetryPolicy.Builder(MaxAttempts(99), BackoffPolicies.NONE).build()
    val api: Api = policy.proxy(Api::class.java, Impl())
    println(api.execute())
}

interface Api {
    @Throws(IOException::class)
    fun execute(): String
}

class Impl : Api {
    var random: Random = Random(System.currentTimeMillis())

    @Throws(IOException::class)
    override fun execute(): String {
        val r = random.nextInt(10)
        return if (r >= 6) "exe-$r" else throw IOException("exe failed")
    }
}
