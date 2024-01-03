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

import com.google.common.annotations.VisibleForTesting;
import io.spine.code.proto.FileSet;
import io.spine.logging.WithLogging;
import io.spine.tools.code.SourceSetName;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.fs.Generated;
import io.spine.tools.js.fs.FileName;
import io.spine.tools.js.fs.JsFiles;
import io.spine.tools.mc.js.fs.JsFile;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A task to resolve imports in generated files.
 *
 * <p>Supports only {@code CommonJS} imports.
 *
 * <p>This step should be performed last among {@linkplain CodeGenStep code generation steps}
 * to ensure that imports won't be modified later.
 */
public final class ResolveImports extends CodeGenStep implements WithLogging {

    private final Path generatedRoot;
    private final ExternalModules modules;

    public ResolveImports(Generated generatedRoot, SourceSetName ssn, ExternalModules modules) {
        super(generatedRoot.dir(ssn));
        this.generatedRoot = generatedRoot.path();
        this.modules = checkNotNull(modules);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        var jsCodeRoot = jsCodeRoot();
        for (var file : fileSet.files()) {
            var fileName = FileName.from(file);
            logger().atDebug()
                    .log(() -> format("Resolving imports in the file `%s`.", fileName));
            var filePath = JsFiles.resolve(jsCodeRoot, fileName);
            resolveInFile(filePath);
        }
    }

    @VisibleForTesting
    void resolveInFile(Path filePath) {
        var file = new JsFile(filePath);
        file.resolveImports(generatedRoot, modules);
    }
}
