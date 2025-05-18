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
import retry.internal.Utils.toReadableString
import java.time.Duration

object FailureListeners {
    
    @JvmStatic
    @JvmOverloads
    fun logging(logEnabled: Rule = Rules.TRUE, stackEnabled: Rule = Rules.FALSE) = SimpleLoggingFailureListener(logEnabled, stackEnabled)
    
}

/**
 * A simple logging failure listener.
 *
 * @param logRule if true, logs invocation errors
 * @param stackRule if true, logs stack trace of invocation errors
 */
data class SimpleLoggingFailureListener(
    private val logRule: Rule,
    private val stackRule: Rule
) : FailureListener {

    override fun onFailure(call: String?, context: Context, allowRetry: Boolean, backOffDuration: Duration) {
        if (logRule.check(context)) {
            LOG.info(
                "Invocation failed, context: {}, retry: {}, backOff: {}.",
                *arrayListOf(context, allowRetry, backOffDuration.toReadableString()).let {
                    if (stackRule.check(context)) {
                        it.add(context.failure)
                    }
                    it.toTypedArray()
                }
            )
        }
    }

    private companion object {
        private val LOG = LoggerFactory.getLogger(SimpleLoggingFailureListener::class.java)
    }
}
