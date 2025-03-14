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
import org.junit.jupiter.api.Timeout
import org.mockito.Mockito
import java.io.IOException
import java.util.concurrent.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RetrySubmitTest {
    
    private lateinit var executor: ScheduledExecutorService
    
    @BeforeTest
    fun init() {
        executor = Executors.newScheduledThreadPool(5)
    }
    
    @AfterTest
    fun cleanup() {
        executor.shutdown()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

    @Test
    @Timeout(1)
    fun testRetrySuccess() {
        val retry = Retry(
            retryCondition = MaxRetries(3),
            backOff = BackOff.NONE
        )
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(3) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        assertThat(retry.submit(executor, function = { mock.call() }).get()).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(4)).call()
    }

    @Test
    @Timeout(1)
    fun testRetryFail() {
        val retry = Retry(
            retryCondition = MaxRetries(2),
            backOff = BackOff.NONE
        )
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(4) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        val error = assertFailsWith<ExecutionException> {
            retry.submit(executor, function = { mock.call() }).get()
        }
        assertThat(error.cause).isInstanceOf(IOException::class.java)

        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(5)
    fun testRetrySuccessWithMultipleSubmits() {
        val retry = Retry(
            retryCondition = MaxRetries(3),
            backOff = BackOff.seconds(1)
        )
        val mocks = Array(100) {
            val mock = Mockito.mock(Callable::class.java)
            Mockito.doThrow(*Array(3) {
                IOException()
            }).doReturn("done").`when`(mock).call()
            mock
        }

        val results = Array(100) {
            retry.submit(executor, "call-$it") { 
                mocks[it].call() 
            }
        }

        for (it in results) {
            assertThat(it.get()).isEqualTo("done")
        }

        for (it in mocks) {
            Mockito.verify(it, Mockito.times(4)).call()
        }
    }

}
