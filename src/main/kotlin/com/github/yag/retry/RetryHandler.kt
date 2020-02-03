/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.github.yag.retry

import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class RetryHandler<T>(private val retry: Retry, private val target: T, private val name: String) : InvocationHandler {

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