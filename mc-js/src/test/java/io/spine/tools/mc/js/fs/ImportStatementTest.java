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

package io.spine.tools.mc.js.fs;

import com.google.common.truth.Truth8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.mc.js.fs.Given.importWithPath;
import static io.spine.tools.mc.js.fs.Given.relativeImportPath;

@DisplayName("`ImportStatement` should")
class ImportStatementTest {

    private final File importOrigin = Paths.get("folder/nested/some-file.js").toFile();
    private final ImportStatement statement = importWithPath(relativeImportPath(), importOrigin);

    @Test
    @DisplayName("extract the import path")
    void extractImportPath() {
        var fileReference = statement.fileRef();
        assertThat(fileReference.value())
                .isEqualTo(relativeImportPath());
    }

    @Test
    @DisplayName("replace the import path")
    void replaceImportPath() {
        var newPath = "b";
        var updatedStatement = statement.replaceRef(newPath);
        var updatedPath = updatedStatement.fileRef();
        assertThat(updatedPath.value())
                .isEqualTo(newPath);
    }

    @Test
    @DisplayName("know about the absolute path to the imported file")
    void obtainImportedFilePath() {
        var importedFilePath = statement.importedFilePath();
        var expectedRoot = importOrigin.getParentFile().toPath();
        var expectedPath = expectedRoot.resolve(relativeImportPath()).normalize();
        Truth8.assertThat(importedFilePath)
             .isEqualTo(expectedPath);
    }
}
