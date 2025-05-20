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
    
    private val default = RetryPolicy.Builder(Rules.TRUE, BackoffPolicies.NONE).build()
    
    @Test
    fun testDefault() {
        val retry = RetryPolicy.Builder(Rules.TRUE, BackoffPolicies.NONE).build()
        assertThat(retry.retryRule).isEqualTo(default.retryRule)
        assertThat(retry.backoffPolicy).isEqualTo(default.backoffPolicy)
        assertThat(retry.failureListeners).isEqualTo(default.failureListeners)
        assertThat(retry).isNotSameAs(default)
    }
    
    @Test
    fun testBuild() {
        val retryRule = Rules.TRUE
        val backoff = FixedDelay(Duration.ZERO)
        val abortRule = Rules.FALSE
        val failureListener = Mockito.mock(FailureListener::class.java)
        
        val retry = RetryPolicy.Builder(retryRule, backoff)
            .addFailureListener(failureListener)
            .build()
        assertThat(retry.retryRule).isSameAs(retryRule)
        assertThat(retry.backoffPolicy).isSameAs(backoff)
        assertThat(retry.failureListeners).containsAll(default.failureListeners)
        assertThat(retry.failureListeners).contains(failureListener)
    }
}