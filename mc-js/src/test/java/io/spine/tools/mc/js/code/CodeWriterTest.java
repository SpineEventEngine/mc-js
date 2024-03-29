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

package io.spine.tools.mc.js.code;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.StringSubject;
import io.spine.tools.code.Indent;
import io.spine.tools.code.IndentedLine;
import io.spine.tools.code.Line;
import io.spine.tools.mc.js.code.text.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.code.Indent.of2;
import static io.spine.tools.code.Indent.of4;
import static io.spine.tools.mc.js.code.CodeWriter.lineSeparator;
import static io.spine.tools.mc.js.code.given.Generators.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`CodeWriter` should")
@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
class CodeWriterTest {

    private static final String LINE = "line";
    private static final String METHOD_NAME = "func";
    private static final String FUNCTION_ARG = "arg";
    private static final String VALUE = "someValue";
    private static final String CONDITION = VALUE + " > 0";
    private static final String CUSTOM_BLOCK = "for (let i in values)";

    private CodeWriter jsOutput;

    @BeforeEach
    void setUp() {
        jsOutput = new CodeWriter();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicInstanceMethods(jsOutput);
    }

    @Test
    @DisplayName("support custom indent")
    void setIndent() {
        var indent = Indent.of4();
        var jsOutput = new CodeWriter(indent);
        jsOutput.increaseDepth();
        jsOutput.append(LINE);
        var expected = indent.at(1) + LINE;
        assertThat(jsOutput.toString())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("allow manually")
    class AllowManually {

        @Test
        @DisplayName("increase depth")
        void increaseDepth() {
            jsOutput.increaseDepth();
            assertEquals(1, jsOutput.currentDepth());
        }

        @Test
        @DisplayName("decrease depth")
        void decreaseDepth() {
            jsOutput.increaseDepth();
            jsOutput.decreaseDepth();
            assertEquals(0, jsOutput.currentDepth());
        }
    }

    @Test
    @DisplayName("declare function")
    void declareFunction() {
        jsOutput.enterMethod(METHOD_NAME, FUNCTION_ARG);
        var declaration = METHOD_NAME + " = function(" + FUNCTION_ARG + ") {";
        assertContains(jsOutput, declaration);
        assertEquals(1, jsOutput.currentDepth());
    }

    @Test
    @DisplayName("exit function body")
    void exitFunction() {
        jsOutput.enterMethod(METHOD_NAME, FUNCTION_ARG);
        jsOutput.exitMethod();
        var functionExit = "};";
        assertContains(jsOutput, functionExit);
        assertEquals(0, jsOutput.currentDepth());
    }

    @Nested
    @DisplayName("enter")
    class Enter {

        @Test
        @DisplayName("if block")
        void enterIf() {
            jsOutput.enterIfBlock(CONDITION);
            var ifDeclaration = "if (" + CONDITION + ") {";
            assertContains(jsOutput, ifDeclaration);
            assertEquals(1, jsOutput.currentDepth());
        }

        @Test
        @DisplayName("else block")
        void enterElse() {
            jsOutput.enterIfBlock(CONDITION);
            jsOutput.enterElseBlock();
            var elseDeclaration = "} else {";
            assertContains(jsOutput, elseDeclaration);
            assertEquals(1, jsOutput.currentDepth());
        }

        @Test
        @DisplayName("custom block")
        void enterCustomBlock() {
            jsOutput.enterBlock(CUSTOM_BLOCK);
            var blockDeclaration = CUSTOM_BLOCK + " {";
            assertContains(jsOutput, blockDeclaration);
            assertEquals(1, jsOutput.currentDepth());
        }
    }

    @Nested
    @DisplayName("append code lines")
    class AppendLines {

        private static final String FIRST_PART = "first part";
        private static final String SECOND_PART = "second part";

        @Test
        @DisplayName("of the same depth")
        void sameDepth() {
            var first = GivenWriter.newCodeLines(FIRST_PART);
            var second = GivenWriter.newCodeLines(SECOND_PART);
            first.append(second);
            var expected = FIRST_PART + lineSeparator() + SECOND_PART;
            assertLines(first).isEqualTo(expected);
        }

        @Test
        @DisplayName("only of the same indent")
        void notAllowDifferentIndents() {
            var first = GivenWriter.newCodeLines(of2(), FIRST_PART);
            var second = GivenWriter.newCodeLines(of4(), FIRST_PART);
            assertIllegalArgument(() -> first.append(second));
        }

        @Test
        @DisplayName("and increase depth")
        void increaseDepth() {
            assertMergedAndAligned(2, 0);
        }

        @Test
        @DisplayName("and decrease depth")
        void decreaseDepth() {
            assertMergedAndAligned(0, 2);
        }

        /**
         * Asserts that two {@link CodeWriter} are merged
         * and the depth of the appended lines is adjusted.
         *
         * @param d1
         *         the depth of lines to append to
         * @param d2
         *         the depth of the appended lines
         */
        private void assertMergedAndAligned(int d1, int d2) {
            var first = GivenWriter.withDepth(d1);
            var second = GivenWriter.withSomeCodeIndentedAt(d2);
            first.append(second);
            var expected = GivenWriter.withSomeCodeIndentedAt(d1);
            assertThat(first).isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("append an unaligned line")
    void appendUnalignedLine() {
        var lines = new CodeWriter();
        lines.increaseDepth();
        Line comment = Comment.of("The field...");
        lines.append(comment);
        var expected = lines.indent() + comment.text();
        assertLines(lines).isEqualTo(expected);
    }

    @Test
    @DisplayName("append an indented line")
    void appendIndentedLine() {
        var lines = new CodeWriter();
        var indent = Indent.of4().shiftedRight();
        var indentedLine = IndentedLine.of(indent, "some code line");
        lines.append(indentedLine);
        assertLines(lines)
                .isEqualTo(indentedLine.toString());
    }

    @Test
    @DisplayName("exit block")
    void exitBlock() {
        jsOutput.enterBlock(CUSTOM_BLOCK);
        jsOutput.exitBlock();
        var blockExit = "}";
        assertContains(jsOutput, blockExit);
        assertEquals(0, jsOutput.currentDepth());
    }

    @Test
    @DisplayName("join lines using comma")
    void joinLinesWithComma() {
        var first = Line.of("entry1");
        var second = Line.of("entry2");
        var last = Line.of("entry3");
        var lines = ImmutableList.of(first, second, last);

        var code = CodeWriter.commaSeparated(lines);

        var assertCode = assertLines(code);
        assertCode.contains(first + ",");
        assertCode.contains(second + ",");
        assertCode.contains(last.text());
        assertCode.doesNotContain(last + ",");
    }

    @Test
    @DisplayName("concatenate all lines of code with correct indent in `toString`")
    void provideToString() {
        var jsOutput = GivenWriter.newCodeLines("line 1");
        jsOutput.increaseDepth();
        jsOutput.append("line 2");
        var output = jsOutput.toString();
        var expected = "line 1" + lineSeparator() + "  line 2";
        assertThat(output)
                .isEqualTo(expected);
    }

    private static StringSubject assertLines(CodeWriter lines) {
        return assertThat(lines.toString());
    }
}
