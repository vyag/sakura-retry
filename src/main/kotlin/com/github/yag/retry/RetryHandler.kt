package com.github.yag.retry

import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class RetryHandler<T>(private val retry: Retry, val target: T, val name: String) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? {
        return retry.call("$name.${method.name}") {
            try {
                if (args == null) {
                    method.invoke(target)
                } else {
                    method.invoke(target, *args)
                }
            } catch (e: InvocationTargetException) {
                throw e.cause ?: e
            }
        }
    }
}