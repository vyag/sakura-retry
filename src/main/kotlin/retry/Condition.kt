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

package retry

import java.time.Duration

fun interface Condition {

    fun allow(retryCount: Int, duration: Duration, error: Throwable): Boolean

    infix fun and(policy: Condition): Condition {
        return Condition { retryCount, duration, error ->
            this@Condition.allow(retryCount, duration, error) && policy.allow(retryCount, duration, error)
        }
    }

    infix fun or(policy: Condition): Condition {
        return Condition { retryCount, duration, error ->
            this@Condition.allow(retryCount, duration, error) || policy.allow(retryCount, duration, error)
        }
    }

    operator fun not() : Condition {
        return Condition { retryCount, duration, error -> !this@Condition.allow(retryCount, duration, error) }
    }

    companion object {

        @JvmStatic
        val ALWAYS = Condition { _, _, _ -> true }

        @JvmStatic
        val NONE = Condition { _, _, _ -> false }
    }

}
