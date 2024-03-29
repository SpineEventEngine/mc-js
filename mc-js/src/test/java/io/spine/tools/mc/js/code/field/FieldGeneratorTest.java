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

package io.spine.tools.mc.js.code.field;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.js.code.FieldName;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.tools.mc.js.code.field.FieldGenerator.FIELD_VALUE;
import static io.spine.tools.mc.js.code.field.MapFieldGenerator.ATTRIBUTE;
import static io.spine.tools.mc.js.code.field.MapFieldGenerator.MAP_KEY;
import static io.spine.tools.mc.js.code.field.RepeatedFieldGenerator.LIST_ITEM;
import static io.spine.tools.mc.js.code.field.given.Given.mapField;
import static io.spine.tools.mc.js.code.field.given.Given.repeatedField;
import static io.spine.tools.mc.js.code.field.given.Given.singularField;
import static io.spine.tools.mc.js.code.given.Generators.assertContains;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("`FieldGenerator` should")
class FieldGeneratorTest {

    private static final String JS_OBJECT = "jsObject";
    private static final String MESSAGE_NAME = "messageName";
    private static final String OBJECT_NAME = "objectName";

    private CodeWriter jsOutput;

    private SingularFieldGenerator singularGenerator;
    private RepeatedFieldGenerator repeatedGenerator;
    private MapFieldGenerator mapGenerator;

    @BeforeEach
    void setUp() {
        jsOutput = new CodeWriter();
        singularGenerator = singularGenerator();
        repeatedGenerator = repeatedGenerator();
        mapGenerator = mapGenerator();
    }

    @Test
    @DisplayName("acquire field value by field JSON name")
    void acquireJsObject() {
        var fieldValue = singularGenerator.acquireFieldValue();
        var expected = OBJECT_NAME + '.' + singularField().getJsonName();
        assertEquals(expected, fieldValue);
    }

    @Nested
    @DisplayName("iterate")
    class Iterate {

        @Test
        @DisplayName("JS list items in case of repeated field")
        void repeated() {
            repeatedGenerator.iterateListValues(JS_OBJECT);
            var forEach = JS_OBJECT + ".forEach";
            assertContains(jsOutput, forEach);
            var forEachItems = '(' + LIST_ITEM + ", index, array)";
            assertContains(jsOutput, forEachItems);
        }

        @Test
        @DisplayName("JS object own properties in case of map field")
        void map() {
            var value = mapGenerator.iterateOwnAttributes(JS_OBJECT);
            var iteration = "for (let " + ATTRIBUTE + " in " + JS_OBJECT + ')';
            assertContains(jsOutput, iteration);
            var ownPropertyCheck = "hasOwnProperty(" + ATTRIBUTE + ')';
            assertContains(jsOutput, ownPropertyCheck);
            var expected = JS_OBJECT + '[' + ATTRIBUTE + ']';
            assertEquals(expected, value);
        }
    }

    @Test
    @DisplayName("call field value precondition to check field value for null")
    void callPrecondition() {
        var fieldValue = singularGenerator.acquireFieldValue();
        singularGenerator.generate();
        var nullCheck = "if (" + fieldValue + " === null)";
        assertContains(jsOutput, nullCheck);
    }

    @Test
    @DisplayName("call field value parser to parse field value")
    void callParser() {
        var fieldValue = singularGenerator.acquireFieldValue();
        singularGenerator.generate();
        var typeUrl = TypeUrl.from(singularField().getMessageType());
        var parserCall = format("TypeParsers.parserFor('%s').fromObject(%s);", typeUrl, fieldValue);
        assertContains(jsOutput, parserCall);
    }

    @Test
    @DisplayName("parse object attribute value to obtain key in case of map field")
    void parseMapKey() {
        mapGenerator.generate();
        var parseAttribute = MAP_KEY + " = parseInt(" + ATTRIBUTE + ')';
        assertContains(jsOutput, parseAttribute);
    }

    @Test
    @DisplayName("set singular field")
    void setSingular() {
        singularGenerator.generate();
        var fieldName = FieldName.from(singularField());
        var setterCall = format("%s.set%s(%s)",
                                singularGenerator.targetVariable(), fieldName, FIELD_VALUE);
        assertContains(jsOutput, setterCall);
    }

    @Test
    @DisplayName("add value to repeated field")
    void addToRepeated() {
        repeatedGenerator.generate();
        var fieldName = FieldName.from(repeatedField());
        var addCall = format("%s.add%s(%s)",
                             repeatedGenerator.targetVariable(), fieldName, FIELD_VALUE);
        assertContains(jsOutput, addCall);
    }

    @Test
    @DisplayName("add value to map field")
    void addToMap() {
        mapGenerator.generate();
        var fieldName = FieldName.from(mapField());
        var getMapCall = "get" + fieldName + "Map()";
        var addToMapCall = "set(" + MAP_KEY + ", " + FIELD_VALUE + ')';
        var addCall = mapGenerator.targetVariable() + '.' + getMapCall + '.' + addToMapCall;
        assertContains(jsOutput, addCall);
    }

    private SingularFieldGenerator singularGenerator() {
        return (SingularFieldGenerator) fieldGenerator(singularField());
    }

    private RepeatedFieldGenerator repeatedGenerator() {
        return (RepeatedFieldGenerator) fieldGenerator(repeatedField());
    }

    private MapFieldGenerator mapGenerator() {
        return (MapFieldGenerator) fieldGenerator(mapField());
    }

    private FieldGenerator fieldGenerator(FieldDescriptor descriptor) {
        var fieldToParse = new FieldToParse(descriptor, OBJECT_NAME, MESSAGE_NAME);
        return FieldGenerators.createFor(fieldToParse, jsOutput);
    }
}
