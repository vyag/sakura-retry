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
import retry.Rules.maxAttempts
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

object Async {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val random = Random(System.currentTimeMillis())

        Executors.newScheduledThreadPool(4).use { executor ->
            val policy = RetryPolicy.Builder(maxAttempts(99), BackoffPolicies.NONE).build()
            
            val result1 = policy.submit<String>(executor) {
                val r = random.nextInt(10)
                if (r >= 6) "foo-$r" else throw IOException("foo failed")
            }

            val result2 = policy.submit<String>(executor) {
                val r = random.nextInt(10)
                if (r >= 6) "bar-$r" else throw IOException("bar failed")
            }
            println(result1.get())
            println(result2.get())
        }
    }
}
