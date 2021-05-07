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

package io.spine.tools.mc.js.code.index;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.js.fs.FileName;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.tools.mc.js.code.task.GenerationTask;
import io.spine.tools.mc.js.code.snippet.Comment;
import io.spine.tools.mc.js.code.snippet.Parser;
import io.spine.tools.mc.js.fs.FileWriter;
import io.spine.tools.mc.js.code.imports.Import;
import io.spine.type.MessageType;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.code.CodeLine.emptyLine;

/**
 * This class writes the {@linkplain Parser code} for
 * parsing of messages generated by Protobuf JS compiler.
 *
 * @see KnownTypes
 */
public final class CreateParsers extends GenerationTask {

    public CreateParsers(Directory generatedRoot) {
        super(checkNotNull(generatedRoot));
    }

    /**
     * Obtains types, which require parsers to be generated.
     *
     * <p>The types with the <a href="https://developers.google.com/protocol-buffers/docs/proto3#json">
     * special JSON mapping</a> should be skipped.
     * Parsers for the types are provided by the Spine Web.
     */
    public static ImmutableCollection<MessageType> targetTypes(FileDescriptor file) {
        if (FileDescriptors.isGoogle(file)) {
            return ImmutableList.of();
        }
        return TypeSet.onlyMessages(file);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        for (FileDescriptor file : fileSet.files()) {
            generateFor(file);
        }
    }

    private void generateFor(FileDescriptor file) {
        if (targetTypes(file).isEmpty()) {
            return;
        }
        CodeWriter code = codeFor(file);
        FileWriter writer = FileWriter.newInstance(generatedRoot(), file);
        writer.append(code);
    }

    @VisibleForTesting
    static CodeWriter codeFor(FileDescriptor file) {
        ImmutableCollection<MessageType> types = targetTypes(file);
        FileName fileName = FileName.from(file);
        CodeWriter lines = new CodeWriter();
        lines.append(emptyLine());
        lines.append(Comment.generatedBySpine());
        lines.append(emptyLine());
        lines.append(imports(fileName));
        lines.append(parses(types));
        return lines;
    }

    /**
     * Generates imports required by the code for parsing of messages.
     *
     * @param targetFile
     *         the file to generate imports for
     */
    private static CodeWriter imports(FileName targetFile) {
        String abstractParserImport = defaultImport(Parser.OBJECT_PARSER_FILE, targetFile)
                .namedAs(Parser.ABSTRACT_PARSER_IMPORT_NAME);
        String parsersImport = defaultImport(Parser.TYPE_PARSERS_FILE, targetFile)
                .namedAs(Parser.TYPE_PARSERS_IMPORT_NAME);
        CodeWriter lines = new CodeWriter();
        lines.append(abstractParserImport);
        lines.append(parsersImport);
        return lines;
    }

    private static Import defaultImport(String importedFile, FileName targetFile) {
        String pathRelativeToTarget = targetFile.pathToRoot() + importedFile;
        return Import.library(pathRelativeToTarget)
                     .toDefault();
    }

    /**
     * Obtains the code with parsers for specified types.
     *
     * @param messageTypes
     *         all messages in a file to generate parser for
     */
    private static CodeWriter parses(Collection<MessageType> messageTypes) {
        CodeWriter snippet = new CodeWriter();
        for (MessageType message : messageTypes) {
            Parser parser = new Parser(message.descriptor());
            snippet.append(emptyLine());
            snippet.append(parser);
        }
        return snippet;
    }
}
