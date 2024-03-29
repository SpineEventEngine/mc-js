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

package io.spine.tools.mc.js.code.field.parser;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.mc.js.code.CodeWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The generator of the JS code parsing a value from its JSON representation.
 *
 * <p>The parsed value can be either of a primitive type, or an object.
 * The value is then stored into the specified variable.
 *
 * <p>Please see <a href="https://developers.google.com/protocol-buffers/docs/proto3#json">
 * Protobuf JSON Mapping</a> for more details.
 */
public interface Parser {

    /**
     * Generates the code which parses the field value from some object and assigns it to the
     * variable.
     *
     * <p>The parsed value is then assigned to the specified variable.
     *
     * @param value
     *         the name of the variable holding the value to parse
     * @param variable
     *         the name of the variable to receive the parsed value
     */
    void parseIntoVariable(String value, String variable);

    /**
     * The writer for the generated code.
     */
    CodeWriter writer();

    /**
     * Creates a {@code FieldParser} for the given field.
     *
     * @param field
     *         the descriptor of the field to create the parser for
     * @param writer
     *         the lines to accumulate the generated code
     * @return the {@code FieldParser} of the appropriate type
     */
    static Parser createFor(FieldDescriptor field, CodeWriter writer) {
        checkNotNull(field);
        checkNotNull(writer);
        var fdecl = new FieldDeclaration(field);
        if (fdecl.isMessage()) {
            return new MessageParser(field, writer);
        }
        if (fdecl.isEnum()) {
            return new EnumParser(field, writer);
        }
        return new PrimitiveTypeParser(field, writer);
    }
}
