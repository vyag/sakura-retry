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
package sakura.retry

import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import java.time.Duration
import kotlin.test.Test

class RetryBuilderTest {
    
    private val default = Retry.Builder().setCondition(Conditions.TRUE).setBackoffPolicy(BackoffPolicies.NONE).build()
    
    @Test
    fun testDefault() {
        val retry = Retry.Builder().setCondition(Conditions.TRUE).setBackoffPolicy(BackoffPolicies.NONE).build()
        assertThat(retry.condition).isEqualTo(default.condition)
        assertThat(retry.backoffPolicy).isEqualTo(default.backoffPolicy)
        assertThat(retry.failureListeners).isEqualTo(default.failureListeners)
        assertThat(retry).isNotSameAs(default)
    }
    
    @Test
    fun testBuild() {
        val retryPolicy = Conditions.TRUE
        val backoff = FixedDelay(Duration.ZERO)
        val failureListener = Mockito.mock(FailureListener::class.java)
        
        val retry = Retry.Builder().setCondition(retryPolicy).setBackoffPolicy(backoff)
            .addFailureListener(failureListener)
            .build()
        assertThat(retry.condition).isSameAs(retryPolicy)
        assertThat(retry.backoffPolicy).isSameAs(backoff)
        assertThat(retry.failureListeners).containsAll(default.failureListeners)
        assertThat(retry.failureListeners).contains(failureListener)
    }
}