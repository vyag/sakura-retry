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
import java.io.IOException
import java.time.Duration
import kotlin.test.Test

class ConditionTest {

    @Test
    fun testLogicOperator() {
        assertThat((Condition.FALSE and Condition.TRUE)
            .check(Context(1, Duration.ZERO, IOException())))
            .isFalse()

        assertThat((Condition.FALSE or Condition.TRUE)
            .check(Context(Int.MAX_VALUE, Duration.ofDays(1), IOException())))
            .isTrue();

        assertThat((!Condition.FALSE)
           .check(Context(Int.MAX_VALUE, Duration.ofDays(1), IOException())))
           .isTrue();
    }
}
