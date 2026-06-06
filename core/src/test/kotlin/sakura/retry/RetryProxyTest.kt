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

package sakura.retry

import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import java.io.IOException
import java.util.concurrent.Callable
import kotlin.test.Test

class RetryProxyTest {

    @Test
    fun testCallAndRetrySuccess() {
        val retry = Mockito.spy(Retry.Builder().build())
        val call = Mockito.mock(Callable::class.java)
        Mockito.doThrow(*Array(9) {
            IOException()
        }).doReturn("done").`when`(call).call()
        val foo = retry.proxy(Callable::class.java, call)
        assertThat(foo.call()).isEqualTo("done")
        Mockito.verify(call, Mockito.times(10)).call()
    }

}
