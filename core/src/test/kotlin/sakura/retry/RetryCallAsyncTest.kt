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
import java.util.concurrent.ThreadFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds


class RetryCallAsyncTest {

    private val sakuraThreadGroup = ThreadGroup("sakura-thread-group")

    private val bizThreadGroup = ThreadGroup("biz-thread-group")

    private lateinit var sakuraExecutor: ScheduledExecutorService
    
    private lateinit var bizExecutor: ExecutorService
    
    @BeforeTest
    fun init() {
        sakuraExecutor = Executors.newScheduledThreadPool(5, ThreadFactory { r -> Thread(sakuraThreadGroup, r) })
        bizExecutor = Executors.newFixedThreadPool(5, ThreadFactory { r -> Thread(bizThreadGroup, r) })
    }
    
    @AfterTest
    fun cleanup() {
        sakuraExecutor.close()
        bizExecutor.close()
    }
    
    open class GetThreadGroup : Callable<ThreadGroup> {
        override fun call(): ThreadGroup = Thread.currentThread().threadGroup
    }
    
    class ThreadGroupException(val threadGroup: ThreadGroup) : Exception()
    
    open class ThrowThreadGroupException(count: Int) : Callable<ThreadGroup> {
        
        private var left = count
        
        override fun call(): ThreadGroup {
            if (left-- > 0) {
                throw ThreadGroupException(Thread.currentThread().threadGroup)
            }
            return Thread.currentThread().threadGroup
        }
    }

    @Test
    fun testNoError() {
        val retry = Retry.Builder().setCondition(Conditions.TRUE).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.spy(GetThreadGroup())
        assertThat(retry.callAsync(sakuraExecutor) {
            mock.call()
        }).succeedsWithin(Duration.ofSeconds(1)).isSameAs(sakuraThreadGroup)
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    fun testNoErrorWithSupplier() {
        val retry = Retry.Builder().setCondition(Conditions.TRUE).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.spy(GetThreadGroup())
        assertThat(retry.callAsyncWithSupplier(sakuraExecutor) {
            CompletableFuture.supplyAsync(mock::call, bizExecutor) 
        }).succeedsWithin(Duration.ofSeconds(1)).isSameAs(bizThreadGroup)
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    @Timeout(1)
    fun testRetrySuccess() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.spy(ThrowThreadGroupException(2))

        assertThat(retry.callAsync(sakuraExecutor, mock::call))
            .succeedsWithin(Duration.ofSeconds(1)).isSameAs(sakuraThreadGroup)
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(1)
    fun testRetrySuccessWithSupplier() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.spy(ThrowThreadGroupException(2))

        assertThat(retry.callAsyncWithSupplier(sakuraExecutor) {
            CompletableFuture.supplyAsync(mock::call, bizExecutor)
        }).succeedsWithin(Duration.ofSeconds(1)).isSameAs(bizThreadGroup)
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(1)
    fun testRetryFail() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.spy(ThrowThreadGroupException(4))

        assertThat(retry.callAsync(sakuraExecutor, mock::call))
            .failsWithin(Duration.ofSeconds(1))
            .withThrowableOfType(ExecutionException::class.java)
            .withCause(ThreadGroupException(sakuraThreadGroup))
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(1)
    fun testRetryFailWithSupplier() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.spy(ThrowThreadGroupException(4))

        assertThat(retry.callAsyncWithSupplier(sakuraExecutor) { CompletableFuture.supplyAsync(mock::call, bizExecutor) })
            .failsWithin(Duration.ofSeconds(1))
            .withThrowableOfType(ExecutionException::class.java)
            .withCause(ThreadGroupException(bizThreadGroup))
        Mockito.verify(mock, Mockito.times(3)).call()
    }

    @Test
    @Timeout(5)
    fun testRetrySuccessWithMultipleSubmits() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(FixedDelay(1.seconds)).build()
        val mocks = Array(100) {
            Mockito.spy(ThrowThreadGroupException(2))
        }

        val results = Array(100) {
            retry.withName("call-$it").callAsync(sakuraExecutor, mocks[it]::call)
        }

        for (it in results) {
            assertThat(it).succeedsWithin(Duration.ofSeconds(5)).isSameAs(sakuraThreadGroup)
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
            Mockito.spy(ThrowThreadGroupException(2))
        }

        val results = Array(100) {
            retry.withName("call-$it").callAsyncWithSupplier(sakuraExecutor) {
                CompletableFuture.supplyAsync(mocks[it]::call, bizExecutor)
            }
        }

        for (it in results) {
            assertThat(it).succeedsWithin(Duration.ofSeconds(5)).isSameAs(bizThreadGroup)
        }

        for (it in mocks) {
            Mockito.verify(it, Mockito.times(3)).call()
        }
    }

}
