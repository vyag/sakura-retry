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
        RetryPolicy retryPolicy = new RetryBuilder()
            .retryCondition(Conditions.TRUE)
            .retryCondition(Conditions.FALSE)
            .retryCondition(new MaxRetries(5))
            .retryCondition(new MaxTimeElapsed(Duration.ofSeconds(10)))
            .abortCondition(Conditions.TRUE)
            .abortCondition(Conditions.FALSE)
            .backOff(Backoffs.IMMEDIATELY)
            .backOff(new FixedDelay(Duration.ofSeconds(1)))
            .backOff(new FixedInterval(Duration.ofSeconds(1)))
            .errorHandler(new SimpleLoggingStrategy())
            .build();
        retryPolicy.callWithNoThrowDeclaration(() -> {
            throw new IOException();
        });
    }
    
    @Test
    public void testBuild() {
        Condition retryCondition = new MaxRetries(5);
        Condition abortCondition = new MaxTimeElapsed(Duration.ofSeconds(10));
        Backoff backOff = new FixedDelay(Duration.ofSeconds(1));
        LoggingStrategy errorHandler = (context, allowRetry, backOffDuration) -> {};
        RetryPolicy retryPolicy = new RetryBuilder()
            .retryCondition(retryCondition)
            .abortCondition(abortCondition)
            .backOff(backOff)
            .errorHandler(errorHandler)
            .build();
        assertThat(retryPolicy.getRetryCondition()).isSameAs(retryCondition);
        assertThat(retryPolicy.getAbortCondition()).isSameAs(abortCondition);
        assertThat(retryPolicy.getBackOff()).isSameAs(backOff);
        assertThat(retryPolicy.getLoggingStrategy()).isSameAs(errorHandler);
    }
}
