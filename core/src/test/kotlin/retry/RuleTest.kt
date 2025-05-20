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
import retry.RetryPolicies.maxAttempts
import retry.RetryPolicies.maxTimeElapsed
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class RetryPolicyTest {

    @Test
    fun testLogicOperator() {
        assertThat((RetryPolicies.FALSE and RetryPolicies.TRUE)
            .check(Context(Instant.MIN, Instant.MIN, 1, IOException())))
            .isFalse()

        assertThat((RetryPolicies.FALSE or RetryPolicies.TRUE)
            .check(Context(Instant.MIN, Instant.MAX, Int.MAX_VALUE, IOException())))
            .isTrue()

        assertThat((!RetryPolicies.FALSE)
           .check(Context(Instant.MIN, Instant.MAX, Int.MAX_VALUE, IOException())))
           .isTrue()
    }
    
    @Test
    fun testToString() {
        assertThat((RetryPolicies.FALSE and (!maxAttempts(5))).toString())
            .isEqualTo("((false) && (!(context.attemptCount < 5)))")
        assertThat(((RetryPolicies.TRUE or (!maxAttempts(5))).toString(Context(Instant.MIN, Instant.MAX, 1, IOException()))))
            .isEqualTo("((true) || (!(context.attemptCount=1 < 5)))")
        assertThat(((RetryPolicies.TRUE and (!maxTimeElapsed(Duration.ofMinutes(1)))).toString(Context(Instant.MIN, Instant.MIN, 1, IOException()))))
            .isEqualTo("((true) && (!(context.duration=PT0S < PT1M)))")
    }
}
