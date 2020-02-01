package com.github.yag.retry

import com.github.yag.config.Value
import org.slf4j.LoggerFactory
import java.time.Duration

open class DefaultErrorHandler @JvmOverloads constructor(@Value private val logSuppressTimeMs: Long = 0) :
    ErrorHandler {

    override fun handle(retryCount: Int, duration: Duration, error: Throwable, allowRetry: Boolean, backOffDuration: Duration) {
        if (duration.toMillis() > logSuppressTimeMs || isUnexpected(error)) {
            if (allowRetry) {
                LOG.warn("Invocation failed, retryCount: {}, duration: {}ms, will retry in {}ms.", retryCount, duration.toMillis(), backOffDuration.toMillis(), error)
            } else {
                LOG.warn("Invocation failed, retryCount: {}, duration: {}ms.", retryCount, duration.toMillis(), error)
            }
        } else if (LOG.isDebugEnabled) {
            LOG.debug("Invocation failed, retryCount: {}, duration: {}ms.", retryCount, duration.toMillis(), error)
        }
    }

    open fun isUnexpected(t: Throwable) = false

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultErrorHandler::class.java)
    }
}