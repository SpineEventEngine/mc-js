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

package io.spine.tools.mc.js.code.step;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.js.generate.TaskCount;
import io.spine.js.generate.TaskServiceProto;
import io.spine.js.generate.typeurl.OuterMessage;
import io.spine.js.generate.typeurl.OuterMessage.NestedEnum;
import io.spine.js.generate.typeurl.OuterMessage.NestedMessage;
import io.spine.js.generate.typeurl.TopLevelEnum;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.type.MessageType;
import io.spine.type.ServiceType;
import io.spine.type.Type;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.mc.js.code.given.Given.enumType;
import static io.spine.tools.mc.js.code.given.Given.messageType;
import static io.spine.tools.mc.js.code.step.AppendTypeUrlGetter.typeUrlMethod;
import static io.spine.tools.mc.js.code.step.AppendTypeUrlGetter.typeUrlMethods;
import static java.lang.String.format;

@DisplayName("AppendTypeUrlGetter should")
class AppendTypeUrlGetterTest {

    private final FileDescriptor file = OuterMessage.getDescriptor()
                                                    .getFile();

    @Nested
    @DisplayName("look up messages")
    class LookUpMessages {

        @Test
        @DisplayName("declared at the top level")
        void topLevel() {
            assertTypeUrl(OuterMessage.getDescriptor());
        }

        @Test
        @DisplayName("nested in a message")
        void nested() {
            assertTypeUrl(NestedMessage.getDescriptor());
        }

        private void assertTypeUrl(Descriptor message) {
            var typeUrl = TypeUrl.from(message);
            assertHasTypeUrl(typeUrl);
        }
    }

    @Nested
    @DisplayName("look up enums")
    class LookUpEnums {

        @Test
        @DisplayName("declared at the top level")
        void topLevel() {
            assertOutHasTypeUrl(TopLevelEnum.getDescriptor());
        }

        @Test
        @DisplayName("nested in a message")
        void nested() {
            assertOutHasTypeUrl(NestedEnum.getDescriptor());
        }

        private void assertOutHasTypeUrl(EnumDescriptor enumDescriptor) {
            var typeUrl = TypeUrl.from(enumDescriptor);
            assertHasTypeUrl(typeUrl);
        }
    }

    @Nested
    @DisplayName("generate the method")
    class GenerateMethod {

        @Test
        @DisplayName("for a message class")
        void forMessageClass() {
            var method = typeUrlMethod(messageType());
            var codeLines = method.writer();
            checkMethodForTypePresent(codeLines, messageType());
        }

        @Test
        @DisplayName("for an enum class")
        void forEnumClass() {
            var method = typeUrlMethod(enumType());
            var codeLines = method.writer();
            checkMethodForTypePresent(codeLines, enumType());
        }
    }

    @Test
    @DisplayName("skip service definitions")
    void skipServiceDefinitions() {
        var file = TaskServiceProto.getDescriptor();
        var service = file.findServiceByName("TaskService");
        var output = typeUrlMethods(file);

        var serviceType = ServiceType.of(service);
        checkMethodForTypeNotPresent(output, serviceType);

        var messageType = new MessageType(TaskCount.getDescriptor());
        checkMethodForTypePresent(output, messageType);
    }

    private static void checkMethodForTypePresent(CodeWriter codeLines, Type<?, ?> type) {
        var methodDeclaration = methodDeclaration(type);
        var returnStatement = format("return '%s';", type.url());
        var endOfMethod = "};";
        var code = codeLines.toString();
        var assertCode = assertThat(code);
        assertCode.contains(methodDeclaration);
        assertCode.contains(returnStatement);
        assertCode.contains(endOfMethod);
    }

    private static void checkMethodForTypeNotPresent(CodeWriter codeLines, Type<?, ?> type) {
        var declaration = methodDeclaration(type);
        var code = codeLines.toString();
        assertThat(code).doesNotContain(declaration);
    }

    private static String methodDeclaration(Type<?, ?> type) {
        var result = format("proto.%s.typeUrl = function() {", type.name());
        return result;
    }

    private void assertHasTypeUrl(TypeUrl typeUrl) {
        var out = AppendTypeUrlGetter.typeUrlMethods(file);
        assertThat(out.toString()).contains(typeUrl.value());
    }
}
