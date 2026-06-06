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
import org.junit.jupiter.api.fail
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import sakura.retry.internal.DefaultDelaySubmitter
import sun.rmi.runtime.Log
import java.time.Duration
import java.util.concurrent.*
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
        sakuraExecutor = Executors.newScheduledThreadPool(5) { r -> Thread(sakuraThreadGroup, r) }
        bizExecutor = Executors.newFixedThreadPool(5) { r -> Thread(bizThreadGroup, r) }
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
            LOG.info("Call with error left: {}", left)
            if (left-- > 0) {
                throw ThreadGroupException(Thread.currentThread().threadGroup)
            }
            return Thread.currentThread().threadGroup
        }
    }

    @Test
    fun testCallAsyncAndNoError() {
        val retry = Retry.Builder()
            .setCondition(Conditions.TRUE)
            .setBackoffPolicy(FixedDelay(1.seconds))
            .setBackoffExecutor { fail("Shouldn't be called") }
            .addFailureListener { _, _, _, _ -> fail("Shouldn't be called") }
            .build()
        val mock = Mockito.spy(GetThreadGroup())
        assertThat(retry.callAsync(sakuraExecutor) {
            mock.call()
        }).succeedsWithin(Duration.ofSeconds(1)).isSameAs(sakuraThreadGroup)
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    fun testWrapAsyncAndNoError() {
        val retry = Retry.Builder()
            .setCondition(Conditions.TRUE)
            .setBackoffPolicy(FixedDelay(1.seconds))
            .setBackoffExecutor { fail("Shouldn't be called") }
            .addFailureListener { _, _, _, _ -> fail("Shouldn't be called") }
            .build()
        val mock = Mockito.spy(GetThreadGroup())
        assertThat(retry.wrapAsync(sakuraExecutor) {
            CompletableFuture.supplyAsync(mock::call, bizExecutor) 
        }).succeedsWithin(Duration.ofSeconds(1)).isSameAs(bizThreadGroup)
        Mockito.verify(mock, Mockito.times(1)).call()
    }

    @Test
    @Timeout(1)
    fun testCallAsyncAndRetrySuccess() {
        var failureCount = 0
        var assertFailures = ConcurrentLinkedQueue<Throwable>()
        val retry = Retry.Builder()
            .setCondition(MaxAttempts(3))
            .setBackoffExecutor { fail("Shouldn't be called") }
            .setBackoffPolicy(FixedDelay(1.seconds))
            .setDelaySubmitter { service, runnable, duration -> 
                try {
                    assertThat(duration).isEqualTo(Duration.ofSeconds(1))
                } catch (e: Throwable) {
                    assertFailures += e
                }
                DefaultDelaySubmitter.submit(service, runnable, Duration.ZERO)
            }
            .addFailureListener { string, context, bool, duration ->
                failureCount++
                try {
                    assertThat(string).isEqualTo("foo")
                    assertThat(bool).isTrue()
                    assertThat(context.failure).isInstanceOf(ThreadGroupException::class.java)
                    assertThat(context.attemptCount).isEqualTo(failureCount)
                    assertThat(duration).isEqualTo(Duration.ofSeconds(1))
                } catch (e: Throwable) {
                    assertFailures += e
                }
                LOG.info("string: $string, duration: $duration")
            }
            .setName("foo")
            .build()
        val mock = Mockito.spy(ThrowThreadGroupException(2))
        
        try {
            assertThat(retry.callAsync(sakuraExecutor, mock::call))
                .succeedsWithin(Duration.ofSeconds(1)).isSameAs(sakuraThreadGroup)
            Mockito.verify(mock, Mockito.times(3)).call()
        } catch (e: Throwable) {
            assertFailures.forEach { throw it }
            throw e
        }
    }

    @Test
    @Timeout(2)
    fun testCallAsyncAndRetryFail() {
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
    fun testWrapAsyncAndRetrySuccess() {
        var failureCount = 0
        var assertFailures = ConcurrentLinkedQueue<Throwable>()
        val retry = Retry.Builder()
            .setCondition(MaxAttempts(3))
            .setBackoffExecutor { fail("Shouldn't be called") }
            .setBackoffPolicy(FixedDelay(1.seconds))
            .setDelaySubmitter { service, runnable, duration ->
                try {
                    assertThat(duration).isEqualTo(Duration.ofSeconds(1))
                } catch (e: Throwable) {
                    assertFailures += e
                }
                DefaultDelaySubmitter.submit(service, runnable, Duration.ZERO)
            }
            .addFailureListener { string, context, bool, duration ->
                failureCount++
                try {
                    assertThat(string).isEqualTo("foo")
                    assertThat(bool).isTrue()
                    assertThat(context.failure).isInstanceOf(ThreadGroupException::class.java)
                    assertThat(context.attemptCount).isEqualTo(failureCount)
                    assertThat(duration).isEqualTo(Duration.ofSeconds(1))
                } catch (e: Throwable) {
                    assertFailures += e
                }
                LOG.info("string: $string, duration: $duration")
            }
            .setName("foo")
            .build()
        val mock = Mockito.spy(ThrowThreadGroupException(2))

        try {
            assertThat(retry.wrapAsync(sakuraExecutor) {
                CompletableFuture.supplyAsync(mock::call, bizExecutor)
            }).succeedsWithin(Duration.ofSeconds(1)).isSameAs(bizThreadGroup)
            Mockito.verify(mock, Mockito.times(3)).call()
        } catch (e: Throwable) {
            assertFailures.forEach { throw it }
            throw e
        }
    }
    
    @Test
    @Timeout(1)
    fun testWrapAsyncAndRetryFail() {
        val retry = Retry.Builder().setCondition(MaxAttempts(3)).setBackoffPolicy(BackoffPolicies.NONE).build()
        val mock = Mockito.spy(ThrowThreadGroupException(4))

        assertThat(retry.wrapAsync(sakuraExecutor) { CompletableFuture.supplyAsync(mock::call, bizExecutor) })
            .failsWithin(Duration.ofSeconds(1))
            .withThrowableOfType(ExecutionException::class.java)
            .withCause(ThreadGroupException(bizThreadGroup))
        Mockito.verify(mock, Mockito.times(3)).call()
    }
    
    companion object {
        private val LOG = LoggerFactory.getLogger(RetryCallAsyncTest::class.java)
    }

}
