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
    
    private val default = RetryPolicy()
    
    @Test
    fun testDefault() {
        val retry = RetryPolicyBuilder().build()
        assertThat(retry.retryCondition).isEqualTo(default.retryCondition)
        assertThat(retry.abortCondition).isEqualTo(default.abortCondition)
        assertThat(retry.backOff).isEqualTo(default.backOff)
        assertThat(retry.loggingPolicy).isEqualTo(default.loggingPolicy)
        assertThat(retry).isNotSameAs(default)
    }
    
    @Test
    fun testBuild() {
        val retryCondition = Conditions.TRUE
        val abortCondition = Conditions.FALSE
        val backOff = FixedInterval(Duration.ZERO)
        val loggingPolicy = Mockito.mock(LoggingPolicy::class.java)
        
        val retry = RetryPolicyBuilder()
            .retryCondition(retryCondition)
            .abortCondition(abortCondition)
            .backoffPolicy(backOff)
            .errorHandler(loggingPolicy)
            .build()
        assertThat(retry.retryCondition).isSameAs(retryCondition)
        assertThat(retry.abortCondition).isSameAs(abortCondition)
        assertThat(retry.backOff).isSameAs(backOff)
        assertThat(retry.loggingPolicy).isSameAs(loggingPolicy)
    }
}