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

import io.spine.code.fs.SourceCodeDirectory;
import io.spine.code.proto.ProtoBelongsToModule;
import io.spine.code.proto.SourceFile;
import io.spine.tools.js.fs.FileName;
import io.spine.tools.js.fs.JsFiles;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A predicate determining if the given Protobuf file was compiled to JavaScript
 * and belongs to the specified module.
 */
final class CompiledProtoBelongsToModule extends ProtoBelongsToModule {

    private final SourceCodeDirectory jsCodeRoot;

    /**
     * Creates a new instance.
     *
     * @param jsCodeRoot
     *         the root directory for generated Protobufs
     */
    CompiledProtoBelongsToModule(SourceCodeDirectory jsCodeRoot) {
        super();
        checkNotNull(jsCodeRoot);
        this.jsCodeRoot = jsCodeRoot;
    }

    @Override
    protected Path resolve(SourceFile file) {
        FileName fileName = FileName.from(file.descriptor());
        Path filePath = JsFiles.resolve(jsCodeRoot, fileName);
        return filePath;
    }
}
