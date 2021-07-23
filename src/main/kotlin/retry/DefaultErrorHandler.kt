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

package retry

import org.slf4j.LoggerFactory
import java.time.Duration

class DefaultErrorHandler(
    private val logSuppressTimeMs: Long = 0,
    private val unexpectedErrorCondition: Condition = Condition.NONE,
    private val printStackCondition: Condition = Condition.ALWAYS,
    private val callback: (Throwable) -> Unit = {}
) : ErrorHandler {

    override fun handle(
        retryCount: Int,
        duration: Duration,
        error: Throwable,
        allowRetry: Boolean,
        backOffDuration: Duration
    ) {
        callback(error)
        val printStack = printStackCondition.match(retryCount, duration, error)
        val durationMs = duration.toMillis()
        if (unexpectedErrorCondition.match(retryCount, duration, error)) {
            if (printStack) {
                LOG.warn("Unexpected invocation failed, retryCount: {}, duration: {}ms.", retryCount, durationMs, error)
            } else {
                LOG.warn("Unexpected invocation failed: $error retryCount: {}, duration: {}ms.", retryCount, durationMs)
            }
        } else {
            if (durationMs > logSuppressTimeMs) {
                if (allowRetry) {
                    if (printStack) {
                        LOG.info(
                            "Invocation failed, retryCount: {}, duration: {}ms, will retry in {}ms.", retryCount,
                            durationMs, backOffDuration.toMillis(), error
                        )
                    } else {
                        LOG.info(
                            "Invocation failed: $error retryCount: {}, duration: {}ms, will retry in {}ms.", retryCount,
                            durationMs, backOffDuration.toMillis()
                        )
                    }
                } else {
                    if (printStack) {
                        LOG.info("Invocation failed, retryCount: {}, duration: {}ms.", retryCount, durationMs, error)
                    } else {
                        LOG.info("Invocation failed: $error retryCount: {}, duration: {}ms.", retryCount, durationMs)
                    }
                }
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultErrorHandler::class.java)
    }
}
