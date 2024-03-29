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
import io.spine.tools.mc.js.code.field.parser.Parser;
import io.spine.tools.mc.js.code.field.precondition.FieldPrecondition;
import io.spine.tools.mc.js.code.CodeWriter;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.proto.FieldTypes.isMap;
import static io.spine.code.proto.FieldTypes.isRepeated;
import static io.spine.code.proto.FieldTypes.keyDescriptor;
import static io.spine.code.proto.FieldTypes.valueDescriptor;
import static io.spine.tools.mc.js.code.field.precondition.FieldPrecondition.preconditionFor;

/**
 * The helper class which provides the {@link FieldGenerator} implementation for the given
 * {@linkplain FieldDescriptor field}.
 */
public final class FieldGenerators {

    /** Prevents instantiation of this utility class. */
    private FieldGenerators() {
    }

    /**
     * Creates a {@code FieldGenerator} for the given field.
     *
     * @param field
     *         the field to parse and set for a message
     * @param writer
     *         the {@code JsOutput} to accumulate all the generated code
     * @return the new {@code FieldGenerator} of the appropriate type
     */
    public static FieldGenerator createFor(FieldToParse field, CodeWriter writer) {
        checkNotNull(field);
        checkNotNull(writer);
        var descriptor = field.descriptor();
        if (isMap(descriptor)) {
            return mapGenerator(field, writer);
        }
        if (isRepeated(descriptor)) {
            return repeatedGenerator(field, writer);
        }
        return singularGenerator(field, writer);
    }

    /**
     * Creates a {@linkplain MapFieldGenerator generator} for the {@code map} field.
     *
     * @implNote
     * The creation logic is different from all other generators.
     *
     * <p>As the {@code map} field is always a {@code message} of type {@code ...Entry}, we create
     * {@link FieldPrecondition} and {@link Parser} for it's field with name {@code "value"}
     * (whose type corresponds to the {@code map} value type).
     *
     * <p>The key also has to be parsed via the separate {@code FieldParser}, as in JSON it is
     * always converted to a {@code string}. So we create additional {@code FieldParser} for
     * the {@code ...Entry} {@code "key"} field.
     */
    private static FieldGenerator mapGenerator(FieldToParse field, CodeWriter writer) {
        var descriptor = field.descriptor();
        var keyParser = mapKeyParser(descriptor, writer);
        var valueParser = mapValueParser(descriptor, writer);
        var valuePrecondition = mapValuePrecondition(descriptor, writer);

        FieldGenerator generator = MapFieldGenerator.newBuilder()
                .setField(field)
                .setPrecondition(valuePrecondition)
                .setKeyParser(keyParser)
                .setParser(valueParser)
                .setWriter(writer)
                .build();
        return generator;
    }

    /**
     * Creates a {@linkplain RepeatedFieldGenerator generator} for the {@code repeated} proto field.
     */
    private static FieldGenerator repeatedGenerator(FieldToParse field, CodeWriter writer) {
        var descriptor = field.descriptor();
        var precondition = preconditionFor(descriptor, writer);
        var parser = Parser.createFor(descriptor, writer);
        FieldGenerator generator = RepeatedFieldGenerator.newBuilder()
                .setField(field)
                .setPrecondition(precondition)
                .setParser(parser)
                .setWriter(writer)
                .build();
        return generator;
    }

    /**
     * Creates a {@linkplain SingularFieldGenerator generator} for the ordinary proto field.
     */
    private static FieldGenerator singularGenerator(FieldToParse field, CodeWriter writer) {
        var descriptor = field.descriptor();
        var precondition = preconditionFor(descriptor, writer);
        var parser = Parser.createFor(descriptor, writer);
        FieldGenerator generator = SingularFieldGenerator.newBuilder()
                .setField(field)
                .setPrecondition(precondition)
                .setParser(parser)
                .setWriter(writer)
                .build();
        return generator;
    }

    /**
     * Creates a {@code FieldPrecondition} for the value of the map field.
     */
    private static FieldPrecondition
    mapValuePrecondition(FieldDescriptor field, CodeWriter writer) {
        var valueDescriptor = valueDescriptor(field);
        var precondition = preconditionFor(valueDescriptor, writer);
        return precondition;
    }

    /**
     * Creates a {@code FieldParser} for the key of the map field.
     */
    private static Parser mapKeyParser(FieldDescriptor field, CodeWriter writer) {
        var keyDescriptor = keyDescriptor(field);
        var parser = Parser.createFor(keyDescriptor, writer);
        return parser;
    }

    /**
     * Creates a {@code FieldParser} for the value of the map field.
     */
    private static Parser mapValueParser(FieldDescriptor field, CodeWriter writer) {
        var valueDescriptor = valueDescriptor(field);
        var parser = Parser.createFor(valueDescriptor, writer);
        return parser;
    }
}
