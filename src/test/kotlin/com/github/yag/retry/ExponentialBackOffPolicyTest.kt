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
import kotlin.test.Test
import kotlin.test.assertEquals

class ExponentialBackOffPolicyTest {

    private val duration = Duration.ZERO

    private val error = Exception()

    @Test
    fun testMaxInterval() {
        val backOff = ExponentialBackOffPolicy(1, 5)
        listOf(0 to 1, 1 to 2, 2 to 4, 3 to 5, 4 to 5).forEach {
            assertEquals(it.second, backOff.backOff(it.first, duration, error).toMillis().toInt())
        }
    }

    @Test
    fun testOverflow() {
        val backOff = ExponentialBackOffPolicy(Long.MAX_VALUE / 2 + 1, Long.MAX_VALUE / 2 + 2)
        assertEquals(Long.MAX_VALUE / 2 + 1, backOff.backOff(0, duration, error).toMillis())
        assertEquals(Long.MAX_VALUE / 2 + 2, backOff.backOff(1, duration, error).toMillis())
    }
}
