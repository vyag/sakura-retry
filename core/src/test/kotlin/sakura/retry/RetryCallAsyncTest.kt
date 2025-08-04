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
import java.time.Duration
import java.util.concurrent.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

class RetryCallAsyncTest {
    
    private lateinit var retryExecutor: ScheduledExecutorService
    
    private lateinit var bizExecutor: ExecutorService
    
    @BeforeTest
    fun init() {
        retryExecutor = Executors.newScheduledThreadPool(5)
        bizExecutor = Executors.newFixedThreadPool(5)
    }
    
    @AfterTest
    fun cleanup() {
        retryExecutor.close()
        bizExecutor.close()
    }

    @Test
    fun testNoError() {
        val retry = Retry.Builder().setCondition(Conditions.TRUE).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doReturn("done").`when`(mock).call()
        assertThat(retry.callAsync(retryExecutor) {
            mock.call()
        }).succeedsWithin(Duration.ofSeconds(1)).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    fun testNoErrorWithSupplier() {
        val retry = Retry.Builder().setCondition(Conditions.TRUE).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doReturn("done").`when`(mock).call()
        assertThat(retry.callAsyncWithSupplier(retryExecutor) {
            CompletableFuture.supplyAsync(mock::call, bizExecutor) 
        }).succeedsWithin(Duration.ofSeconds(1)).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    @Timeout(1)
    fun testRetrySuccess() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(2) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        assertThat(retry.callAsync(retryExecutor, mock::call))
            .succeedsWithin(Duration.ofSeconds(1)).isEqualTo("done")
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(1)
    fun testRetrySuccessWithSupplier() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(2) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        assertThat(retry.callAsyncWithSupplier(retryExecutor) {
            CompletableFuture.supplyAsync(mock::call, bizExecutor)
        }).succeedsWithin(Duration.ofSeconds(1)).isEqualTo("done")
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

        assertThat(retry.callAsync(retryExecutor, mock::call))
            .failsWithin(Duration.ofSeconds(1))
            .withThrowableOfType(ExecutionException::class.java)
            .withCauseInstanceOf(IOException::class.java)
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(1)
    fun testRetryFailWithSupplier() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(4) {
            IOException()
        }).doReturn("done").`when`(mock).call()

        assertThat(retry.callAsyncWithSupplier(retryExecutor) { CompletableFuture.supplyAsync(mock::call, bizExecutor) })
            .failsWithin(Duration.ofSeconds(1))
            .withThrowableOfType(ExecutionException::class.java)
            .withCauseInstanceOf(IOException::class.java)
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
            retry.withName("call-$it").callAsync(retryExecutor, mocks[it]::call)
        }

        for (it in results) {
            assertThat(it).succeedsWithin(Duration.ofSeconds(5)).isEqualTo("done")
        }

        for (it in mocks) {
            Mockito.verify(it, Mockito.times(3)).call()
        }
    }

    @Test
    @Timeout(5)
    fun testRetrySuccessWithMultipleSubmitsWithSupplier() {
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
                CompletableFuture.supplyAsync(mocks[it]::call, bizExecutor)
            }
        }

        for (it in results) {
            assertThat(it).succeedsWithin(Duration.ofSeconds(5)).isEqualTo("done")
        }

        for (it in mocks) {
            Mockito.verify(it, Mockito.times(3)).call()
        }
    }

}
