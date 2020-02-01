package com.github.yag.retry

import org.slf4j.LoggerFactory
import java.lang.reflect.Proxy
import java.time.Duration

class Retry(
    private val retryPolicy: RetryPolicy,
    private val backOffPolicy: BackOffPolicy,
    private val errorHandler: ErrorHandler = DefaultErrorHandler()
) {

    fun <T> call(name: String = "call", body: () -> T): T {
        var retryCount = 0
        val startTime = System.nanoTime()

        while (true) {
            try {
                retryPolicy.check()
                val result = body()
                if (retryCount > 0) {
                    LOG.debug("Finally {} success after {} retries.", name, retryCount)
                }
                return result
            } catch (t: Throwable) {
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                val allowRetry = retryPolicy.allowRetry(retryCount, duration, t)
                val backOff = if (allowRetry) backOffPolicy.backOff(retryCount, duration, t) else Duration.ZERO

                errorHandler.handle(retryCount, duration, t, allowRetry, backOff)
                if (allowRetry) {
                    Thread.sleep(backOff.toMillis())
                    retryCount++
                    continue
                }
                LOG.warn("Give up {} after {} retries, error: {}.", name, retryCount, t.toString())
                throw t
            }
        }
    }

    fun <T> proxy(clazz: Class<T>, target: T, name: String = target.toString()): T {
        @Suppress("UNCHECKED_CAST")
        return (Proxy.newProxyInstance(
            Retry::class.java.classLoader, arrayOf(clazz),
            RetryHandler(this, target, name)
        ) as T)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Retry::class.java)
    }
}