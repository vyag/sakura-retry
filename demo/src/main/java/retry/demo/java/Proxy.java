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

import retry.BackoffPolicies;
import retry.MaxAttempts;
import retry.RetryTemplate;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;

public class Proxy {

    public static void main(String[] args) throws Exception {
        RetryTemplate policy = new RetryTemplate.Builder(new MaxAttempts(99), BackoffPolicies.NONE).build();
        Callable<?> call = policy.proxy(Callable.class, new Impl());
        System.out.println(call.call());
    }

    public static class Impl implements Callable<Double> {
        Random random = new Random(System.currentTimeMillis());

        @Override
        public Double call() throws IOException {
            double d = random.nextDouble(10);
            if (d < 8) throw new IOException("Too small");
            return d;
        }
    }
}
