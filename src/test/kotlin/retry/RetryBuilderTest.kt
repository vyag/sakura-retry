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
import org.mockito.Mockito
import java.time.Duration
import kotlin.test.Test

class RetryBuilderTest {
    
    private val default = Retry()
    
    @Test
    fun testDefault() {
        val retry = RetryBuilder.create().build()
        assertThat(retry.retryCondition).isEqualTo(default.retryCondition)
        assertThat(retry.abortCondition).isEqualTo(default.abortCondition)
        assertThat(retry.backOff).isEqualTo(default.backOff)
        assertThat(retry.errorHandler).isEqualTo(default.errorHandler)
        assertThat(retry).isNotSameAs(default)
    }
    
    @Test
    fun testBuild() {
        val retryCondition = Condition.TRUE
        val abortCondition = Condition.FALSE
        val backOff = FixedInterval(Duration.ZERO)
        val errorHandler = Mockito.mock(ErrorHandler::class.java)
        
        val retry = RetryBuilder.create()
            .retryCondition(retryCondition)
            .abortCondition(abortCondition)
            .backOff(backOff)
            .errorHandler(errorHandler)
            .build()
        assertThat(retry.retryCondition).isSameAs(retryCondition)
        assertThat(retry.abortCondition).isSameAs(abortCondition)
        assertThat(retry.backOff).isSameAs(backOff)
        assertThat(retry.errorHandler).isSameAs(errorHandler)
    }
}