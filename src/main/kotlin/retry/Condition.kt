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

fun interface Condition {

    fun check(context: Context) : Boolean
    
    fun toString(context: Context): String = toString()

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

    companion object {

        @JvmStatic
        val TRUE = object: Condition { 
            override fun check(context: Context): Boolean {
                return true
            }

            override fun toString(): String {
                return "true"
            }
        }

        @JvmStatic
        val FALSE = object: Condition {
            override fun check(context: Context): Boolean {
                return false
            }

            override fun toString(): String {
                return "false"
            }
        }
    }

}
