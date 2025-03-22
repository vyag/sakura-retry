/*
 * Copyright 2025-2025 marks.yag@gmail.com
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

package retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExceptionDeclarationTest {
    
    @Disabled("Do compile failed is expected.")
    @Test
    public void testCallWithNoThrowsDoNotHaveAnExceptionThrowDeclaration() {
        Retry retry = Retry.NONE;
        retry.callWithNoThrowDeclaration(() -> {
            throw new IOException();
        });
    }
    
    @Test
    public void testOriginalExceptionCouldBeThrowByCallWithNoThrows() {
        Retry retry = Retry.NONE;
        IOException exception = new IOException("fun");
        IOException got = assertThrowsExactly(IOException.class, () -> retry.callWithNoThrowDeclaration(() -> {
            throw exception;
        }));
        assertThat(got).isSameAs(exception);
    }

    @Test
    public void testCallWithThrowsDeclaration() {
        Retry retry = Retry.NONE;
        IOException exception = new IOException("fun");
        IOException got = assertThrowsExactly(IOException.class, () -> {
            try {
                retry.call(() -> {
                    throw exception;
                });
            } catch (IOException e) {
                // test there is an exception throw declaration in call() or it will compile failed.
                throw e;
            }
        });
        assertThat(got).isSameAs(exception);
    }
}
