package com.github.yag.retry

import org.slf4j.LoggerFactory
import java.lang.reflect.Proxy
import java.time.Duration
import java.time.Period

class Retry(private val retryPolicy: RetryPolicy, private val backOffPolicy: BackOffPolicy, private  val errorHandler: ErrorHandler = DefaultErrorHandler()) {

    fun <T> call(msg: String = "call", body: () -> T) : T {
        var retryCount = 0
        val startTime = System.nanoTime()

        while (true) {
            try {
                retryPolicy.check()
                val result =  body()
                if (retryCount > 0) {
                    LOG.debug("Finally {} success after {} retries.", msg, retryCount)
                }
                return result
            } catch (t: Throwable) {
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                errorHandler.handle(retryCount, duration, t)
                if (retryPolicy.allowRetry(retryCount, duration, t)) {
                    backOffPolicy.backOff(retryCount, duration, t)
                    retryCount++
                    continue
                }
                LOG.warn("Give up {} after {} retries, error: {}.", msg, retryCount, t.toString())
                throw t
            }
        }
    }

    fun <T> proxy(clazz: Class<T>, target: T, name: String = target.toString()) : T {
        @Suppress("UNCHECKED_CAST")
        return (Proxy.newProxyInstance(
            Retry::class.java.classLoader, arrayOf(clazz),
            RetryHandler<T>(this, target, name)
        ) as T)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Retry::class.java)
    }
}