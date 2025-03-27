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
import java.util.Collections;

public class BuilderTest {
    
    @Test
    @Disabled("Do compile failed is expected.")
    public void testBuildCompiledWithJvmFields() {
        RetryPolicy retryPolicy = new RetryPolicyBuilder()
            .retryCondition(Conditions.TRUE)
            .retryCondition(Conditions.FALSE)
            .retryCondition(new MaxRetries(5))
            .retryCondition(new MaxTimeElapsed(Duration.ofSeconds(10)))
            .updateRetryCondition(condition -> condition.and(new MaxRetries(3)))
            .abortCondition(Conditions.TRUE)
            .abortCondition(Conditions.FALSE)
            .updateAbortCondition(condition -> condition.and(new MaxTimeElapsed(Duration.ofSeconds(10))))
            .backoffPolicy(BackoffPolicies.NONE)
            .backoffPolicy(new FixedDelay(Duration.ofSeconds(1)))
            .backoffPolicy(new FixedInterval(Duration.ofSeconds(1)))
            .addFailureListener(FailureListeners.logging(Conditions.TRUE, Conditions.FALSE))
            .addFailureListener(new SimpleLoggingFailureListener(Conditions.TRUE, Conditions.FALSE))
            .failureListeners(Collections.emptyList())
            .clearFailureListeners()
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
        FailureListener errorHandler = (context, allowRetry, backOffDuration) -> {};
        RetryPolicy retryPolicy = new RetryPolicyBuilder()
            .retryCondition(retryCondition)
            .abortCondition(abortCondition)
            .backoffPolicy(backOff)
            .addFailureListener(errorHandler)
            .build();
        assertThat(retryPolicy.getRetryCondition()).isSameAs(retryCondition);
        assertThat(retryPolicy.getAbortCondition()).isSameAs(abortCondition);
        assertThat(retryPolicy.getBackoffPolicy()).isSameAs(backOff);
        assertThat(retryPolicy.getFailureListeners()).containsAll(new RetryPolicy().getFailureListeners());
        assertThat(retryPolicy.getFailureListeners()).contains(errorHandler);
    }
}
