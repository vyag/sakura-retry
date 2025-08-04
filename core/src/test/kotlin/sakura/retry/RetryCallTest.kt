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

package sakura.retry

import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import sakura.retry.internal.BackoffExecutor
import java.io.IOException
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetryCallTest {

    @Test
    fun testNoError() {
        val retry = Retry.Builder().setCondition(Conditions.TRUE).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doReturn("done").`when`(mock).call()
        assertThat(retry.call {
            mock.call()
        }).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    fun testRetrySuccess() {
        val retry = Retry.Builder().setCondition(MaxAttempts(10)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(9) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        assertEquals("done", retry.call {
            mock.call()
        })
        Mockito.verify(mock, Mockito.times(10)).call()
    }

    @Test
    fun testRetryFailed() {
        val retry = Retry.Builder().setCondition(MaxAttempts(10)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(IOException()).`when`(mock).call()

        assertFailsWith<IOException> {
            retry.call {
                mock.call()
            }
        }
        Mockito.verify(mock, Mockito.times(10)).call()
    }
    
    @Test
    fun testRetryWithRecovery() {
        val broken = AtomicBoolean(true)
        val retry = Retry.Builder()
            .setCondition(MaxAttempts(10))
            .setBackoffPolicy(BackoffPolicies.NONE)
            .addFailureListener { _, _, _, _ -> 
                broken.set(false)
            }.build()
        var count = 0
        val result = retry.call {
            count++
            if (broken.get()) {
                throw IOException()
            } else {
                "fixed"
            }
        }
        assertThat(result).isEqualTo("fixed")
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun testRetryBackOff() {
        var backoffCount = 0
        val fakeSleeper = BackoffExecutor {
            backoffCount++
        }

        val retry = Retry.Builder().setCondition(MaxAttempts(10)).setBackoffPolicy(FixedDelay(Duration.ofSeconds(1))).build()
        retry.backoffExecutor = fakeSleeper
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(IOException()).`when`(mock).call()
        
        assertFailsWith<IOException> {
            retry.call {
                mock.call()
            }
        }
        Mockito.verify(mock, Mockito.times(10)).call()
        assertThat(backoffCount).isEqualTo(9)
    }

}
