/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.mc.js.code.field.parser;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.js.code.TypeName;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.tools.mc.js.code.given.Generators;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.mc.js.code.field.given.Given.enumField;
import static io.spine.tools.mc.js.code.field.given.Given.messageField;
import static io.spine.tools.mc.js.code.field.given.Given.primitiveField;
import static io.spine.tools.mc.js.code.field.given.Given.timestampField;
import static io.spine.tools.mc.js.code.field.parser.Parser.createFor;
import static io.spine.tools.mc.js.code.snippet.Parser.PARSE_METHOD;
import static java.lang.String.format;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("FieldParser should")
class ParserTest {

    private static final String VALUE = "value";
    private static final String VARIABLE = "variable";

    private Parser parser;
    private CodeWriter writer;

    private void assertInstanceOf(Class<?> cls) {
        assertThat(parser).isInstanceOf(cls);
    }

    /**
     * Asserts that the code generated by the parser contains the passed code.
     */
    private void assertContains(String code) {
        Generators.assertContains(writer, code);
    }

    @BeforeEach
    void setUp() {
        writer = new CodeWriter();
    }

    @Test
    @DisplayName("reject null passed to factory method")
    void nullCheck() {
        new NullPointerTester()
                .setDefault(FieldDescriptor.class, messageField())
                .testAllPublicStaticMethods(Parser.class);
    }

    @Test
    @DisplayName("create parser for primitive field")
    void createParserForPrimitive() {
        parser = createFor(primitiveField(), writer);
        assertInstanceOf(PrimitiveTypeParser.class);
    }

    @Test
    @DisplayName("create parser for enum field")
    void createParserForEnum() {
        parser = createFor(enumField(), writer);
        assertInstanceOf(EnumParser.class);
    }

    @Test
    @DisplayName("create parser for message field with custom type")
    void createParserForMessage() {
        parser = createFor(messageField(), writer);
        assertInstanceOf(MessageParser.class);
    }

    @Test
    @DisplayName("create parser for message field with standard type")
    void createParserForWellKnown() {
        parser = createFor(timestampField(), writer);
        assertInstanceOf(MessageParser.class);
    }

    @Test
    @DisplayName("parse primitive field via predefined code")
    void parsePrimitive() {
        parser = createFor(primitiveField(), writer);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String code = "let " + VARIABLE + " = parseInt(" + VALUE + ')';
        assertContains(code);
    }

    @Test
    @DisplayName("parse enum field via JS enum object attribute")
    void parseEnum() {
        parser = createFor(enumField(), writer);
        parser.parseIntoVariable(VALUE, VARIABLE);
        EnumDescriptor enumType = enumField().getEnumType();
        TypeName typeName = TypeName.from(enumType);
        String code = "let " + VARIABLE + " = " + typeName + '[' + VALUE + ']';
        assertContains(code);
    }

    @Test
    @DisplayName("parse message field with custom type via recursive call to `fromObject`")
    void parseMessage() {
        parser = createFor(messageField(), writer);
        parser.parseIntoVariable(VALUE, VARIABLE);
        assertContains(PARSE_METHOD);
    }

    @Test
    @DisplayName("parse a message field")
    void parseWellKnown() {
        parser = createFor(messageField(), writer);
        parser.parseIntoVariable(VALUE, VARIABLE);
        TypeUrl typeUrl = TypeUrl.from(messageField().getMessageType());
        String code = format("TypeParsers.parserFor('%s').fromObject(%s);", typeUrl, VALUE);
        assertContains(code);
    }
}
