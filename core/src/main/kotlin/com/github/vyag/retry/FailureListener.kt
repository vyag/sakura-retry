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
package com.github.vyag.retry

import java.time.Duration

/**
 * The interface for failure listener.
 */
fun interface FailureListener {

    /**
     * Invoked when an attempt fails. 
     *
     * @param call the call that failed
     * @param context the context of the retry
     * @param allowRetry true if the retry is allowed
     * @param backOffDuration the back off duration
     */
    fun onFailure(call: String?, context: Context, allowRetry: Boolean, backOffDuration: Duration)

}
