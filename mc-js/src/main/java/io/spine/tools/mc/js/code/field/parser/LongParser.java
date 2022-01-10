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

import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.tools.mc.js.code.text.Let;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * The generator of the code which parses the proto 64-bit numerical values from their JSON
 * representation.
 *
 * <p>Types like {@code int64}, {@code uint64}, {@code fixed64} etc. are encoded in the JSON as a
 * {@code string}.
 *
 * <p>The parser thus applies the {@code parseInt} operation to the given {@code string} to obtain
 * the proto value from it.
 */
final class LongParser extends AbstractParser {

    LongParser(CodeWriter writer) {
        super(writer);
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        writer().append(parsedVariable(variable, value));
    }

    private static Let parsedVariable(String name, String valueToParse) {
        String initializer = format("parseInt(%s)", valueToParse);
        return Let.withValue(name, initializer);
    }
}
