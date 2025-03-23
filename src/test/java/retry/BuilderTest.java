/*
 * Copyright 2025-2025 marks.yag@gmail.com
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

package retry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

public class BuilderTest {
    
    @Test
    @Disabled("Do compile failed is expected.")
    public void testBuildCompiledWithJvmFields() {
        RetryPolicy retryPolicy = new RetryPolicyBuilder()
            .retryCondition(Conditions.TRUE)
            .retryCondition(Conditions.FALSE)
            .retryCondition(new MaxRetries(5))
            .retryCondition(new MaxTimeElapsed(Duration.ofSeconds(10)))
            .abortCondition(Conditions.TRUE)
            .abortCondition(Conditions.FALSE)
            .backoffPolicy(BackoffPolicies.NONE)
            .backoffPolicy(new FixedDelay(Duration.ofSeconds(1)))
            .backoffPolicy(new FixedInterval(Duration.ofSeconds(1)))
            .loggingPolicy(LoggingPolicies.EVERYTHING)
            .loggingPolicy(new SimpleLoggingPolicy(Conditions.TRUE, Conditions.FALSE))
            .build();
        retryPolicy.callWithNoThrowDeclaration(() -> {
            throw new IOException();
        });
    }
    
    @Test
    public void testBuild() {
        Condition retryCondition = new MaxRetries(5);
        Condition abortCondition = new MaxTimeElapsed(Duration.ofSeconds(10));
        BackoffPolicy backOff = new FixedDelay(Duration.ofSeconds(1));
        LoggingPolicy errorHandler = (context, allowRetry, backOffDuration) -> {};
        RetryPolicy retryPolicy = new RetryPolicyBuilder()
            .retryCondition(retryCondition)
            .abortCondition(abortCondition)
            .backoffPolicy(backOff)
            .loggingPolicy(errorHandler)
            .build();
        assertThat(retryPolicy.getRetryCondition()).isSameAs(retryCondition);
        assertThat(retryPolicy.getAbortCondition()).isSameAs(abortCondition);
        assertThat(retryPolicy.getBackoffPolicy()).isSameAs(backOff);
        assertThat(retryPolicy.getLoggingPolicy()).isSameAs(errorHandler);
    }
}
