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

import org.junit.jupiter.api.Timeout
import org.mockito.Mockito
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RetrySubmitTest {

    @Test
    @Timeout(1)
    fun testRetrySuccess() {
        val retry = Retry().apply {
            retryCondition = MaxRetries(3)
            backOff = BackOff.NONE
        }
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(3) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        val executor = Executors.newScheduledThreadPool(1)
        assertEquals("done", retry.submit(executor, body = mock).get())
        Mockito.verify(mock, Mockito.times(4)).call()

        executor.shutdownNow()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

    @Test
    @Timeout(1)
    fun testRetryFail() {
        val retry = Retry().apply {
            retryCondition = MaxRetries(2)
            backOff = BackOff.NONE
        }
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(4) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        val executor = Executors.newScheduledThreadPool(1)
        val error = assertFailsWith<ExecutionException> {
            retry.submit(executor, body = mock).get()
        }
        assertTrue(error.cause is IOException)

        Mockito.verify(mock, Mockito.times(3)).call()

        executor.shutdownNow()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

    @Test
    @Timeout(5)
    fun testRetrySuccessWithMultipleSubmits() {
        val retry = Retry().apply {
            retryCondition = MaxRetries(3)
            backOff = BackOff.duration(1, TimeUnit.SECONDS)
        }
        val mocks = Array(100) {
            val mock = Mockito.mock(Callable::class.java)
            Mockito.doThrow(*Array(3) {
                IOException()
            }).doReturn("done").`when`(mock).call()
            mock
        }

        val executor = Executors.newScheduledThreadPool(1)

        val results = Array(100) {
            retry.submit(executor, "call-$it", mocks[it])
        }

        results.forEach {
            assertEquals("done", it.get())
        }

        mocks.forEach {
            Mockito.verify(it, Mockito.times(4)).call()
        }


        executor.shutdownNow()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

}
