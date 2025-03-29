/*
 * Copyright 2025-2025 marks.yag@gmail.com
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
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class BackoffCombinationTest {

    private val error = Exception()

    @Test
    fun testBackoffCombination() {
        val backoff = BackoffPolicies.FixedDelay(Duration.ofSeconds(1)) + BackoffPolicies.FixedDelay(Duration.ofSeconds(1))
        val backoffDuration = backoff.backoff(Context(Instant.MIN, Instant.MIN, 1, error))
        assertThat(backoffDuration).isEqualTo(Duration.ofSeconds(2))
    }
}
