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

import com.google.common.annotations.VisibleForTesting;
import io.spine.code.fs.SourceCodeDirectory;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.tools.js.fs.FileName;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.tools.mc.js.code.step.CodeGenStep;
import io.spine.tools.mc.js.code.text.Import;
import io.spine.tools.mc.js.fs.FileWriter;

import static io.spine.tools.code.Line.emptyLine;
import static io.spine.tools.js.fs.LibraryFile.INDEX;
import static java.util.stream.Collectors.toSet;

/**
 * The task to generate the {@code index.js} for generated Protobuf types.
 *
 * <p>The index file is used by the Spine Web to register known types and their parsers.
 *
 * <p>The index file provides:
 * <ul>
 *     <li>The map of known types.
 *     <li>The map of parsers for known types.
 * </ul>
 */
public final class GenerateIndexFile extends CodeGenStep {

    public GenerateIndexFile(SourceCodeDirectory jsCodeRoot) {
        super(jsCodeRoot);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        var code = codeFor(fileSet);
        var writer = FileWriter.newInstance(jsCodeRoot(), INDEX.fileName());
        writer.write(code);
    }

    @VisibleForTesting
    static CodeWriter codeFor(FileSet fileSet) {
        var code = new CodeWriter();
        code.append(knownTypesImports(fileSet));
        code.append(emptyLine());
        code.append(new KnownTypes(fileSet).writer());
        code.append(emptyLine());
        code.append(new TypeParsers(fileSet).writer());
        return code;
    }

    /**
     * Generates import statements for all files declaring generated messages.
     */
    private static CodeWriter knownTypesImports(FileSet fileSet) {
        var files = fileSet.files();
        var imports = files.stream()
                .filter(file -> !TypeSet.from(file).isEmpty())
                .map(FileName::from)
                .collect(toSet());
        var importLines = new CodeWriter();
        for (var fileName : imports) {
            var fileImport = Import.fileRelativeToRoot(fileName);
            importLines.append(fileImport);
        }
        return importLines;
    }
}
