/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.tools.mc.js.code.text;

import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.mc.js.code.given.GivenMethod.methodReference;
import static java.lang.System.lineSeparator;

@DisplayName("`Method` should")
class MethodTest {

    private static final String NL = lineSeparator();

    @Test
    @DisplayName("assemble an empty no args method")
    void emptyNoArgs() {
        var method = newMethod().build();
        var expected = expectedNoArgsDeclaration() + NL + "};";
        assertThat(method).isEqualTo(expected);
    }

    @Test
    @DisplayName("assemble an empty method with arguments")
    void emptyWithArgs() {
        var argument = "methodArgument";
        var method = newMethod()
                .withParameters(argument)
                .build();
        var expected = methodReference() + " = function(methodArgument) {" + NL + "};";
        assertThat(method).isEqualTo(expected);
    }

    @Test
    @DisplayName("assemble a method with body")
    void nonEmpty() {
        var method = newMethod()
                .appendToBody("statement1;")
                .appendToBody("statement2;")
                .build();
        var expected = expectedNoArgsDeclaration() + NL
                + "  statement1;" + NL
                + "  statement2;" + NL
                + "};";
        assertThat(method).isEqualTo(expected);
    }

    private static String expectedNoArgsDeclaration() {
        return methodReference() + " = function() {";
    }

    private static Method.Builder newMethod() {
        return Method.newBuilder(methodReference());
    }

    private static StringSubject assertThat(Method method) {
        var rawMethod = method.writer()
                              .toString();
        return Truth.assertThat(rawMethod);
    }
}
