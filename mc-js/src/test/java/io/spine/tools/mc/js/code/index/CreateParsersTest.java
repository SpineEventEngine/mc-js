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

package io.spine.tools.mc.js.code.index;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.js.generate.TaskId;
import io.spine.option.OptionsProto;
import io.spine.tools.code.SourceSetName;
import io.spine.tools.fs.SourceDir;
import io.spine.tools.js.code.TypeName;
import io.spine.tools.js.fs.FileName;
import io.spine.tools.js.fs.JsFiles;
import io.spine.tools.mc.js.code.given.GivenProject;
import io.spine.tools.mc.js.code.text.Comment;
import io.spine.tools.mc.js.code.text.Import;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.mc.js.code.given.FileWriters.assertFileContains;
import static io.spine.tools.mc.js.code.given.Generators.assertContains;
import static io.spine.tools.mc.js.code.text.Parser.OBJECT_PARSER_FILE;
import static io.spine.tools.mc.js.code.text.Parser.OBJECT_PARSER_IMPORT_NAME;
import static io.spine.tools.mc.js.code.text.Parser.TYPE_PARSERS_FILE;
import static io.spine.tools.mc.js.code.text.Parser.TYPE_PARSERS_IMPORT_NAME;

@DisplayName("`CreateParsers` should")
class CreateParsersTest {

    private static final FileDescriptor file = TaskId.getDescriptor().getFile();
    
    private static FileSet fileSet = null;
    private static SourceDir generatedProtoDir = null;
    private static CreateParsers writer = null;

    @BeforeAll
    static void compileProject() {
        var project = GivenProject.serving(CreateParsersTest.class);
        fileSet = project.mainFileSet();
        generatedProtoDir = project.generatedMainJsSources();
        writer = new CreateParsers(project.generated().dir(SourceSetName.main));
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().setDefault(SourceDir.class, generatedProtoDir)
                               .setDefault(FileSet.class, fileSet)
                               .testAllPublicStaticMethods(CreateParsers.class);
    }

    @Test
    @DisplayName("generate explaining comment")
    void generateComment() {
        var code = CreateParsers.codeFor(file);
        var expectedComment = Comment.generatedBySpine();
        assertContains(code, expectedComment.text());
    }

    @Test
    @DisplayName("generate imports")
    void generateImports() {
        var code = CreateParsers.codeFor(file);
        var importPrefix = FileName.from(file)
                                   .pathToRoot();
        var abstractParserImport =
                Import.library(importPrefix + OBJECT_PARSER_FILE)
                      .toDefault()
                      .namedAs(OBJECT_PARSER_IMPORT_NAME);
        var typeParsersImport =
                Import.library(importPrefix + TYPE_PARSERS_FILE)
                      .toDefault()
                      .namedAs(TYPE_PARSERS_IMPORT_NAME);
        assertContains(code, abstractParserImport);
        assertContains(code, typeParsersImport);
    }

    @Test
    @DisplayName("write code for parsing")
    void writeParsingCode() throws IOException {
        writer.generateFor(fileSet);
        checkProcessedFiles(fileSet);
    }

    @Test
    @DisplayName("write code for parsing of Spine options")
    void writeOptionsParseCode() {
        var optionsFile = OptionsProto.getDescriptor()
                                      .getFile();
        Collection<MessageType> targets = CreateParsers.targetTypes(optionsFile);
        assertThat(targets).isNotEmpty();
    }

    @Test
    @DisplayName("not write parsing code for standard Protobuf types")
    void skipStandard() {
        Collection<MessageType> targets = CreateParsers.targetTypes(Any.getDescriptor()
                                                                       .getFile());
        assertThat(targets).isEmpty();
    }

    private static void checkProcessedFiles(FileSet fileSet) throws IOException {
        Collection<FileDescriptor> fileDescriptors = fileSet.files();
        for (var file : fileDescriptors) {
            var messageTypes = file.getMessageTypes();
            if (!FileDescriptors.isGoogle(file) && !messageTypes.isEmpty()) {
                checkParseCodeAdded(file);
            }
        }
    }

    private static void checkParseCodeAdded(FileDescriptor file) throws IOException {
        var jsFilePath = JsFiles.resolve(generatedProtoDir, FileName.from(file));
        for (var messageType : TypeSet.onlyMessages(file)) {
            var parserTypeName = TypeName.ofParser(messageType.descriptor());
            assertFileContains(jsFilePath, parserTypeName.value());
        }
    }
}
