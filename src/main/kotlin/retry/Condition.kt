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

@file:JvmName("Conditions")
package retry

/**
 * Condition is a boolean expression that can be evaluated in the context of a given [Context].
 */
fun interface Condition {

    /**
     * Evaluates the condition in the context of the given [Context].
     * @param context the context to evaluate the condition in
     * @return true if the condition is satisfied, false otherwise
     */
    fun check(context: Context) : Boolean
    
    /**
     * Returns a string representation of the condition in the context of the given [Context].
     * @param context the context to evaluate the condition in
     * @return a string representation of the condition in the context of the given [Context]
     */
    fun toString(context: Context): String = toString()

    /**
     * Returns a new condition that is the logical AND of this condition and the given condition.
     * @param cond the condition to AND with this condition
     * @return a new condition that is the logical AND of this condition and the given condition
     */
    infix fun and(cond: Condition): Condition {
        val self = this
        return object: Condition {
            override fun check(context: Context): Boolean {
                return self.check(context) && cond.check(context)
            }

            override fun toString(): String {
                return "(($self) && ($cond))"
            }

            override fun toString(context: Context): String {
                return "((${self.toString(context)}) && (${cond.toString(context)}))"
            }
        }
    }

    /**
     * Returns a new condition that is the logical OR of this condition and the given condition.
     * @param cond the condition to OR with this condition
     * @return a new condition that is the logical OR of this condition and the given condition
     */
    infix fun or(cond: Condition): Condition {
        val self = this
        return object: Condition {
            override fun check(context: Context): Boolean {
                return self.check(context) || cond.check(context)
            }

            override fun toString(): String {
                return "(($self) || ($cond))"
            }

            override fun toString(context: Context): String {
                return "((${self.toString(context)}) || (${cond.toString(context)}))"
            }
        }
    }

    /**
     * Returns a new condition that is the logical NOT of this condition.
     * @return a new condition that is the logical NOT of this condition
     */
    operator fun not() : Condition {
        val self = this
        return object: Condition {
            override fun check(context: Context): Boolean {
                return !self.check(context)
            }

            override fun toString(): String {
                return "!($self)"
            }
            
            override fun toString(context: Context): String {
                return "!(${self.toString(context)})"
            }    
        }
    }
}

/**
 * A condition that always returns true.
 */
@JvmField
val TRUE = object: Condition {
    override fun check(context: Context): Boolean {
        return true
    }

    override fun toString(): String {
        return "true"
    }
}

/**
 * A condition that always returns false.
 */
@JvmField
val FALSE = object: Condition {
    override fun check(context: Context): Boolean {
        return false
    }

    override fun toString(): String {
        return "false"
    }
}
