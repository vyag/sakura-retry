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
import org.mockito.Mockito
import retry.internal.BackOffExecutor
import java.io.IOException
import java.time.Duration
import java.util.concurrent.Callable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetryCallTest {

    @Test
    fun testNoError() {
        val retry = NONE
        val mock = Mockito.mock(Callable::class.java)
        retry.call {
            mock.call()
        }
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    fun testRetrySuccess() {
        val retryPolicy = RetryPolicy(
            retryCondition = MaxRetries(10),
            backOff = IMMEDIATELY
        )
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(10) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        assertEquals("done", retryPolicy.call {
            mock.call()
        })
        Mockito.verify(mock, Mockito.times(11)).call()
    }

    @Test
    fun testRetryFailed() {
        val retryPolicy = RetryPolicy(
            retryCondition = MaxRetries(10),
            backOff = IMMEDIATELY
        )
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(IOException()).`when`(mock).call()

        assertFailsWith<IOException> {
            retryPolicy.call {
                mock.call()
            }
        }
        Mockito.verify(mock, Mockito.times(11)).call()
    }

    @Test
    fun testRetryBackOff() {
        var invocationCount = 0
        var totalSleepMs = 0L
        val fakeSleeper = BackOffExecutor {
            invocationCount++
            totalSleepMs += it.toMillis()
        }

        val retryPolicy = RetryPolicy(
            retryCondition = MaxRetries(10),
            backOff = FixedDelay(Duration.ofSeconds(1))
        )
        retryPolicy.backOffExecutor = fakeSleeper
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(IOException()).`when`(mock).call()

        assertFailsWith<IOException> {
            retryPolicy.call {
                mock.call()
            }
        }
        Mockito.verify(mock, Mockito.times(11)).call()
        assertThat(invocationCount).isEqualTo(10)
        assertThat(totalSleepMs).isEqualTo(10000)
    }

}
