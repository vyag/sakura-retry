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

@file:JvmName("Rules")
package retry

/**
 * Rule is a boolean expression that can be evaluated in the context of a given [Context].
 */
fun interface Rule {

    /**
     * Evaluates the rule in the context of the given [Context].
     * @param context the context to evaluate the rule in
     * @return true if the rule is satisfied, false otherwise
     */
    fun check(context: Context) : Boolean
    
    /**
     * Returns a string representation of the rule in the context of the given [Context].
     * @param context the context to evaluate the rule in
     * @return a string representation of the rule in the context of the given [Context]
     */
    fun toString(context: Context): String = toString()

    /**
     * Returns a new rule that is the logical AND of this rule and the given rule.
     * @param cond the rule to AND with this rule
     * @return a new rule that is the logical AND of this rule and the given rule
     */
    infix fun and(cond: Rule): Rule {
        val self = this
        return object: Rule {
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
     * Returns a new rule that is the logical OR of this rule and the given rule.
     * @param cond the rule to OR with this rule
     * @return a new rule that is the logical OR of this rule and the given rule
     */
    infix fun or(cond: Rule): Rule {
        val self = this
        return object: Rule {
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
     * Returns a new rule that is the logical NOT of this rule.
     * @return a new rule that is the logical NOT of this rule
     */
    operator fun not() : Rule {
        val self = this
        return object: Rule {
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
