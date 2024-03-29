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

package io.spine.tools.mc.js.code.field.precondition;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Value;
import io.spine.tools.mc.js.code.CodeWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The precondition for the proto fields of {@code message} types.
 */
final class MessagePrecondition extends FieldPrecondition {

    private final FieldDescriptor field;

    /**
     * Creates a new {@code MessagePrecondition} for the given {@code field}.
     *
     * @param field
     *         the processed field
     * @param writer
     *         the writer to accumulate the generated code
     */
    MessagePrecondition(FieldDescriptor field, CodeWriter writer) {
        super(writer);
        this.field = field;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For messages, if the parsed value equals to {@code null}, the message value is also set
     * to {@code null} via the {@code mergeFieldFormat}. The further parsing does not happen in
     * this case.
     *
     * <p>The only exception is the Protobuf {@link Value} type, where the check does not take
     * place and the {@code null} is allowed to reach the parser, which later converts it to the
     * {@link com.google.protobuf.NullValue}.
     */
    @Override
    public void performNullCheck(String value, String mergeFieldFormat) {
        checkNotNull(value);
        checkNotNull(mergeFieldFormat);
        if (isProtobufValueType()) {
            return;
        }
        var writer = writer();
        writer.ifNull(value);
        var mergeNull = String.format(mergeFieldFormat, "null");
        writer.append(mergeNull);
        writer.enterElseBlock();
    }

    @Override
    public void exitNullCheck() {
        if (!isProtobufValueType()) {
            writer().exitBlock();
        }
    }

    /**
     * Checks if the processed {@code field} is of the Protobuf {@link Value} type.
     */
    private boolean isProtobufValueType() {
        var valueType = Value.getDescriptor()
                             .getFullName();
        var fieldType = field.getMessageType()
                             .getFullName();
        var isValueType = fieldType.equals(valueType);
        return isValueType;
    }
}
