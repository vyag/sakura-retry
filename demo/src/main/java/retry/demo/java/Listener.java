/*
 * Copyright 2025-2025 marks.yag@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package retry.demo.java;

import org.jetbrains.annotations.NotNull;
import retry.BackoffPolicies;
import retry.Context;
import retry.FailureListener;
import retry.RetryPolicy;

import java.io.IOException;
import java.time.Duration;

import static retry.Rules.maxAttempts;

public class Listener {
    
    static class AlarmSender implements FailureListener {
        @Override
        public void onFailure(@NotNull Context context, boolean allowRetry, Duration backOffDuration) {
            if (!allowRetry) {
                System.out.println("Send to alarm center");
            }
        }
    }

    static class Cleaner implements FailureListener {
        @Override
        public void onFailure(@NotNull Context context, boolean allowRetry, Duration backOffDuration) {
            if (!(context.getFailure() instanceof IOException)) {
                System.out.println("Send to alarm center");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RetryPolicy policy = new RetryPolicy.Builder(maxAttempts(3), BackoffPolicies.NONE)
            .addFailureListener(new AlarmSender()).build();
        policy.call(
            () -> {
                throw new IOException();
            }
        );
    }
}
