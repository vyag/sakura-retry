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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExponentialTest {

    private val duration = Duration.ZERO

    private val error = Exception()

    @Test
    fun testMaxInterval() {
        val backOff = Exponential(1, maxIntervalMs = 5)
        val data = listOf(
            0 to 1,
            1 to 2,
            2 to 4,
            3 to 5,
            4 to 5)
        for (it in data) {
            assertThat(backOff.backOff(Context(it.first, duration, error)).toMillis().toInt()).isEqualTo(it.second)
        }
    }

    @Test
    fun testOverflowProtection() {
        val backOff = Exponential(Long.MAX_VALUE / 2 + 1, maxIntervalMs = Long.MAX_VALUE / 2 + 2)
        assertThat(backOff.backOff(Context(0, duration, error)).toMillis()).isEqualTo(Long.MAX_VALUE / 2 + 1)
        assertThat(backOff.backOff(Context(1, duration, error)).toMillis()).isEqualTo(Long.MAX_VALUE / 2 + 2)
    }
}
