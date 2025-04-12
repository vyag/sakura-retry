package retry

import org.assertj.core.api.Assertions.assertThat
import retry.internal.DefaultBackoffExecutor
import java.time.Duration
import kotlin.test.Test

class DefaultBackoffExecutorTest {
    
    @Test
    fun testValidBackoff() {
        val durations = ArrayList<Duration>()
        val backoff = DefaultBackoffExecutor {
            durations.add(it)
        }
        backoff.backoff(Duration.ofSeconds(1))
        assertThat(durations).containsExactly(Duration.ofSeconds(1))
    }
    
    @Test
    fun testInvalidBackoff() {
        val durations = ArrayList<Duration>()
        val backoff = DefaultBackoffExecutor {
            durations.add(it)
        }
        backoff.backoff(Duration.ofSeconds(-1))
        assertThat(durations).isEmpty()
        backoff.backoff(Duration.ofSeconds(0))
        assertThat(durations).isEmpty()
    }
    
    @Test
    fun testVeryLongBackoff() {
        val durations = ArrayList<Duration>()
        val backoff = DefaultBackoffExecutor {
            durations.add(it)
        }
        backoff.backoff(Duration.ofSeconds(Long.MAX_VALUE))
        assertThat(durations).hasSize(1000)
        repeat(1000) {
            assertThat(durations[it]).isEqualTo(Duration.ofMillis(Long.MAX_VALUE))
        }
    }
}