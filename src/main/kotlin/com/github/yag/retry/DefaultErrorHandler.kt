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

package com.github.yag.retry

import com.github.yag.config.Value
import org.slf4j.LoggerFactory
import java.time.Duration

open class DefaultErrorHandler @JvmOverloads constructor(@Value private val logSuppressTimeMs: Long = 0) :
    ErrorHandler {

    override fun handle(retryCount: Int, duration: Duration, error: Throwable, allowRetry: Boolean, backOffDuration: Duration) {
        val durationMs = duration.toMillis()
        if (durationMs > logSuppressTimeMs || isUnexpected(error)) {
            if (allowRetry) {
                LOG.info("Invocation failed, retryCount: {}, duration: {}ms, will retry in {}ms.", retryCount,
                    durationMs, backOffDuration.toMillis(), error)
            } else {
                LOG.info("Invocation failed, retryCount: {}, duration: {}ms.", retryCount, durationMs, error)
            }
        } else {
            LOG.debug("Invocation failed, retryCount: {}, duration: {}ms.", retryCount, durationMs, error)
        }
    }

    open fun isUnexpected(t: Throwable) = false

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultErrorHandler::class.java)
    }
}