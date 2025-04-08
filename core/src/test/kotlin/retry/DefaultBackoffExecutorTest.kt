package retry

import org.assertj.core.api.Assertions.assertThat
import retry.internal.DefaultBackoffExecutor
import java.time.Duration
import kotlin.test.Test

class DefaultBackoffExecutorTest {
    
    @Test
    fun testValidBackoff() {
        var count = 0
        val backoff = DefaultBackoffExecutor {
            count++
        }
        backoff.backoff(Duration.ofSeconds(1))
        assertThat(count).isEqualTo(1)
    }
    
    @Test
    fun testInvalidBackoff() {
        var count = 0
        val backoff = DefaultBackoffExecutor {
            count++
        }
        backoff.backoff(Duration.ofSeconds(-1))
        assertThat(count).isEqualTo(0)
        backoff.backoff(Duration.ofSeconds(0))
        assertThat(count).isEqualTo(0)
    }
}