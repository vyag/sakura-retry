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
import org.junit.jupiter.api.Timeout
import org.mockito.Mockito
import java.io.IOException
import java.util.concurrent.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class RetrySubmit2WithExecutorTest {
    
    private lateinit var retryExecutor: ScheduledExecutorService
    
    private lateinit var bizExecutor: ExecutorService
    
    @BeforeTest
    fun init() {
        retryExecutor = ScheduledThreadPoolExecutor(5) { r ->
            Thread(r, "retry-thread")
        }


        bizExecutor = ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, LinkedBlockingQueue()) { r ->
            Thread(r, "biz-thread")
        }
    }
    
    @AfterTest
    fun cleanup() {
        retryExecutor.close()
        bizExecutor.close()
    }

    @Test
    @Timeout(1)
    fun testRetrySuccess() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(2) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        val result = retry.callAsyncWithSupplier(retryExecutor) {
            CompletableFuture.supplyAsync(mock::call, bizExecutor)
        }
        assertThat(result).succeedsWithin(1.seconds.toJavaDuration()).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(1)
    fun testRetrySuccess2() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(2) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        val result = retry.callAsyncWithSupplier(retryExecutor) {
            CompletableFuture.supplyAsync {
                mock.call()
            }
        }
        assertThat(result).succeedsWithin(1.seconds.toJavaDuration()).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(1)
    fun testRetryFail() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(4) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        val error = assertFailsWith<ExecutionException> {
            retry.callAsyncWithSupplier(retryExecutor) {
                CompletableFuture.supplyAsync {
                    mock.call()
                }
            }.get()
        }
        assertThat(error.cause).isInstanceOf(IOException::class.java)

        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(5)
    fun testRetrySuccessWithMultipleSubmits() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(FixedDelay(1.seconds)).build()
        val mocks = Array(100) {
            val mock = Mockito.mock(Callable::class.java)
            Mockito.doThrow(*Array(2) {
                IOException()
            }).doReturn("done").`when`(mock).call()
            mock
        }

        val results = Array(100) {
            retry.withName("call-$it").callAsyncWithSupplier(retryExecutor) {
                CompletableFuture.supplyAsync {
                    mocks[it].call()
                }
            }
        }

        for (it in results) {
            assertThat(it.get()).isEqualTo("done")
        }

        for (it in mocks) {
            Mockito.verify(it, Mockito.times(3)).call()
        }
    }

}
