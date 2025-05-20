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
 * RetryPolicy is a boolean expression that can be evaluated in the context of a given [Context].
 */
fun interface RetryPolicy {

    /**
     * Evaluates the policy in the context of the given [Context].
     * @param context the context to evaluate the policy in
     * @return true if the policy is satisfied, false otherwise
     */
    fun check(context: Context) : Boolean
    
    /**
     * Returns a string representation of the policy in the context of the given [Context].
     * @param context the context to evaluate the policy in
     * @return a string representation of the policy in the context of the given [Context]
     */
    fun toString(context: Context): String = toString()

    /**
     * Returns a new policy that is the logical AND of this policy and the given policy.
     * @param cond the policy to AND with this policy
     * @return a new policy that is the logical AND of this policy and the given policy
     */
    infix fun and(cond: RetryPolicy): RetryPolicy {
        val self = this
        return object: RetryPolicy {
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
     * Returns a new policy that is the logical OR of this policy and the given policy.
     * @param cond the policy to OR with this policy
     * @return a new policy that is the logical OR of this policy and the given policy
     */
    infix fun or(cond: RetryPolicy): RetryPolicy {
        val self = this
        return object: RetryPolicy {
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
     * Returns a new policy that is the logical NOT of this policy.
     * @return a new policy that is the logical NOT of this policy
     */
    operator fun not() : RetryPolicy {
        val self = this
        return object: RetryPolicy {
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
