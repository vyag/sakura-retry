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

import retry.BackoffPolicies.FixedDelay;
import retry.Rules.MaxAttempts;
import retry.Rules.MaxTimeElapsed;

import org.junit.jupiter.api.Test;

import java.time.Duration;

public class BuilderTest {
    
    @Test
    public void testBuild() {
        Rule retryRule = new MaxAttempts(5);
        BackoffPolicy backoff = new FixedDelay(Duration.ofSeconds(1));
        Rule abortRule = new MaxTimeElapsed(Duration.ofSeconds(10));
        FailureListener failureListener = (context, allowRetry, backOffDuration) -> {};
        RetryPolicy retryPolicy = new RetryPolicy.Builder(retryRule, backoff)
            .abortRule(abortRule)
            .addFailureListener(failureListener)
            .build();
        assertThat(retryPolicy.getRetryRule()).isSameAs(retryRule);
        assertThat(retryPolicy.getAbortRule()).isSameAs(abortRule);
        assertThat(retryPolicy.getBackoffPolicy()).isSameAs(backoff);
        assertThat(retryPolicy.getFailureListeners()).containsAll(new RetryPolicy.Builder(Rules.TRUE, BackoffPolicies.NONE).build().getFailureListeners());
        assertThat(retryPolicy.getFailureListeners()).contains(failureListener);
    }
}
