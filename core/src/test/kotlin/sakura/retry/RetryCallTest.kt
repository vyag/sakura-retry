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
import java.io.IOException
import java.time.Duration
import java.util.concurrent.Callable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds

class RetryCallTest {

    @Test
    fun testCallAndNoError() {
        val retry = Retry.Builder()
            .setCondition(Conditions.TRUE)
            .setBackoffPolicy(FixedDelay(1.seconds))
            .setBackoffExecutor { fail("Shouldn't be called") }
            .addFailureListener { _, _, _, _ -> fail("Shouldn't be called") }
            .build()
        
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doReturn("done").`when`(mock).call()
        assertThat(retry.call {
            mock.call()
        }).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    fun testCallAndRetrySuccess() {
        var duration = Duration.ZERO
        var failureCount = 0
        val retry = Retry.Builder()
            .setCondition(MaxAttempts(10))
            .setBackoffPolicy(FixedDelay(1.seconds))
            .setBackoffExecutor { duration += it }
            .addFailureListener { string, context, bool, duration ->
                failureCount++
                assertThat(string).isEqualTo("foo")
                assertThat(bool).isTrue()
                assertThat(context.failure).isInstanceOf(IOException::class.java)
                assertThat(context.attemptCount).isEqualTo(failureCount)
                assertThat(duration).isEqualTo(Duration.ofSeconds(1))
            }
            .build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(9) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        assertEquals("done", retry.withName("foo") { r ->
            r.call {
                mock.call()
            }
        })
        Mockito.verify(mock, Mockito.times(10)).call()
        assertThat(duration).isEqualTo(Duration.ofSeconds(9))
        assertThat(failureCount).isEqualTo(9)
    }

    @Test
    fun testCallAndRetryFailed() {
        var duration = Duration.ZERO
        var failureCount = 0
        val retry = Retry.Builder()
            .setCondition(MaxAttempts(10))
            .setBackoffPolicy(FixedDelay(1.seconds))
            .setBackoffExecutor { duration += it }
            .addFailureListener { _, context, allowRetry, backOff ->
                failureCount++
                assertThat(context.attemptCount).isEqualTo(failureCount)
                assertThat(context.failure).isInstanceOf(IOException::class.java)
                if (allowRetry) {
                    assertThat(backOff).isEqualTo(Duration.ofSeconds(1))
                } else {
                    assertThat(backOff).isEqualTo(Duration.ZERO)
                }
            }
            .build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(IOException()).`when`(mock).call()

        assertFailsWith<IOException> {
            retry.call(mock::call)
        }
        Mockito.verify(mock, Mockito.times(10)).call()
        assertThat(duration).isEqualTo(Duration.ofSeconds(9))
        assertThat(failureCount).isEqualTo(10)
    }
}
