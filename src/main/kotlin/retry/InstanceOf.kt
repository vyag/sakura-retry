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

/**
 * The condition check if the error is an instance of one of the given classes.
 *
 * @param errors the classes of the errors
 */
data class InstanceOf(val errors: Set<Class<out Throwable>>) : Condition {

    /**
     * The condition check if the error is an instance of one of the given classes.
     *
     * @param errors the classes of the errors
     */
    constructor(vararg errors: Class<out Throwable>) : this(errors.toSet())

    override fun check(context: Context): Boolean {
        val error = context.error
        return errors.contains(error.javaClass) || errors.any { it.isInstance(error) }
    }

    override fun toString(): String {
        return "context.error is in $errors"
    }

    override fun toString(context: Context): String {
        return "context.error=${context.error} is in $errors"
    }
}
