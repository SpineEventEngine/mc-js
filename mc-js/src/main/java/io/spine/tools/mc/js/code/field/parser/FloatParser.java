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

/**
 * The generator of the code parsing the floating point values from their JSON representation.
 *
 * <p>The parser uses the {@code parseFloat} operation on the value to obtain the original floating
 * point number.
 */
final class FloatParser extends AbstractParser {

    FloatParser(CodeWriter writer) {
        super(writer);
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        writer().append(parsedVariable(variable, value));
    }

    private static Let parsedVariable(String name, String valueToParse) {
        var initializer = "parseFloat(" + valueToParse + ')';
        return Let.withValue(name, initializer);
    }
}
