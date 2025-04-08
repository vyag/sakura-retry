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
import retry.Rules.MaxAttempts
import retry.Rules.MaxTimeElapsed
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class RuleTest {

    @Test
    fun testLogicOperator() {
        assertThat((Rules.FALSE and Rules.TRUE)
            .check(Context(Instant.MIN, Instant.MIN, 1, IOException())))
            .isFalse()

        assertThat((Rules.FALSE or Rules.TRUE)
            .check(Context(Instant.MIN, Instant.MAX, Int.MAX_VALUE, IOException())))
            .isTrue()

        assertThat((!Rules.FALSE)
           .check(Context(Instant.MIN, Instant.MAX, Int.MAX_VALUE, IOException())))
           .isTrue()
    }
    
    @Test
    fun testToString() {
        assertThat((Rules.FALSE and (!MaxAttempts(5))).toString())
            .isEqualTo("((false) && (!(context.attemptCount < 5)))")
        assertThat(((Rules.TRUE or (!MaxAttempts(5))).toString(Context(Instant.MIN, Instant.MAX, 1, IOException()))))
            .isEqualTo("((true) || (!(context.attemptCount=1 < 5)))")
        assertThat(((Rules.TRUE and (!MaxTimeElapsed(Duration.ofMinutes(1)))).toString(Context(Instant.MIN, Instant.MIN, 1, IOException()))))
            .isEqualTo("((true) && (!(context.duration=PT0S < PT1M)))")
    }
}
