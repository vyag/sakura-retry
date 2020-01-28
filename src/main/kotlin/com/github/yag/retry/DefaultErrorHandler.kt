package com.github.yag.retry

import com.github.yag.config.Value
import org.slf4j.LoggerFactory
import java.time.Duration

open class DefaultErrorHandler @JvmOverloads constructor(@Value private val logSuppressTimeMs: Long = 0) : ErrorHandler {

    override fun handle(retryCount: Int, duration: Duration, error: Throwable) {
        if (duration.toMillis() > logSuppressTimeMs || isUnexpected(error)) {
            LOG.warn("Invocation failed, retryCount: {}, duration: {}ms.", retryCount, duration, error)
        } else if (LOG.isDebugEnabled) {
            LOG.debug("Invocation failed, retryCount: {}, duration: {}ms.", retryCount, duration, error)
        }
    }

    open fun isUnexpected(t: Throwable) = false

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultErrorHandler::class.java)
    }
}