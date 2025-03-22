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
        Retry retry = new RetryBuilder()
            .retryCondition(Condition.TRUE)
            .retryCondition(Condition.FALSE)
            .retryCondition(new MaxRetries(5))
            .retryCondition(MaxTimeElapsed.seconds(10))
            .retryCondition(new MaxTimeElapsed(Duration.ofSeconds(10)))
            .abortCondition(Condition.TRUE)
            .abortCondition(Condition.FALSE)
            .backOff(BackOff.NONE)
            .backOff(FixedDelay.seconds(1))
            .backOff(new FixedDelay(Duration.ofSeconds(1)))
            .backOff(FixedInterval.seconds(1))
            .backOff(new FixedInterval(Duration.ofSeconds(1)))
            .errorHandler(new DefaultErrorHandler())
            .build();
        retry.callWithNoThrowDeclaration(() -> {
            throw new IOException();
        });
    }
    
    @Test
    public void testBuild() {
        Condition retryCondition = new MaxRetries(5);
        Condition abortCondition = new MaxTimeElapsed(Duration.ofSeconds(10));
        BackOff backOff = FixedDelay.seconds(1);
        ErrorHandler errorHandler = (context, allowRetry, backOffDuration) -> {};
        Retry retry = new RetryBuilder()
            .retryCondition(retryCondition)
            .abortCondition(abortCondition)
            .backOff(backOff)
            .errorHandler(errorHandler)
            .build();
        assertThat(retry.getRetryCondition()).isSameAs(retryCondition);
        assertThat(retry.getAbortCondition()).isSameAs(abortCondition);
        assertThat(retry.getBackOff()).isSameAs(backOff);
        assertThat(retry.getErrorHandler()).isSameAs(errorHandler);
    }
}
