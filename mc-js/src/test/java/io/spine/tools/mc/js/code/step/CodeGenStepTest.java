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

package io.spine.tools.mc.js.code.step;

import com.google.common.collect.ImmutableSet;
import io.spine.code.fs.SourceCodeDirectory;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;
import io.spine.tools.js.fs.DefaultJsPaths;
import io.spine.tools.mc.js.code.given.GivenProject;
import io.spine.tools.mc.js.code.given.TestCodeGenStep;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.code.SourceSetName.main;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`CodeGenStep` should")
class CodeGenStepTest {

    private static final String MISSING_PATH = "non-existent";

    private static SourceCodeDirectory generatedJsDir = null;
    private static FileSet mainFileSet = null;

    private TestCodeGenStep task;

    @BeforeAll
    static void compileProject() {
        var project = GivenProject.serving(CodeGenStepTest.class);
        generatedJsDir = project.generated().dir(main);
        mainFileSet = project.mainFileSet();
    }

    @BeforeEach
    void createTask() {
        task = new TestCodeGenStep(generatedJsDir);
    }

    @Test
    @DisplayName("check if there are files to process")
    void checkFilesToProcess() {
        assertPerformed(task, mainFileSet);
    }

    @Test
    @DisplayName("recognize there are no generated files to process")
    void recognizeThereAreNoFiles() {
        var nonExistentRoot =
                DefaultJsPaths.at(Paths.get(MISSING_PATH))
                              .generated()
                              .dir(main);
        var task = new TestCodeGenStep(nonExistentRoot);
        assertNotPerformed(task, mainFileSet);
    }

    @Test
    @DisplayName("recognize there are no known types to process")
    void recognizeThereAreNoTypes() {
        var emptyFileSet = FileSet.of(ImmutableSet.of());
        assertNotPerformed(task, emptyFileSet);
    }

    @Test
    @DisplayName("process files compiled to JavaScript")
    void processCompiledJsFiles() {
        var passedFiles = mainFileSet;
        task.performFor(passedFiles);
        var processedFiles = task.processedFileSet();
        // It is expected that standard Protobuf types won't be generated (see test build script).
        var expectedFilteredFiles = passedFiles
                .filter(FileDescriptors::isGoogle)
                .files();
        int expectedProcessedFiles = passedFiles.size() - expectedFilteredFiles.size();
        assertThat(processedFiles.size()).isEqualTo(expectedProcessedFiles);
    }

    @Test
    @DisplayName("skip files not compiled to JavaScript")
    void skipNotCompiledJsFiles(@TempDir Path tempDir) {
        var emptyDirectory = DefaultJsPaths.at(tempDir).generated().dir(main);
        var task = new TestCodeGenStep(emptyDirectory);
        var passedFiles = mainFileSet;
        // Check the file set is originally not empty.
        assertFalse(passedFiles.isEmpty());
        // Check all passed files were filtered out since they were not compiled to JS.
        assertNotPerformed(task, passedFiles);
        assertTrue(task.areFilesFiltered());
    }

    private static void assertPerformed(TestCodeGenStep task, FileSet fileSet) {
        assertPerformed(task, fileSet, true);
    }

    private static void assertNotPerformed(TestCodeGenStep task, FileSet fileSet) {
        assertPerformed(task, fileSet, false);
    }

    private static void assertPerformed(TestCodeGenStep task,
                                        FileSet fileSet,
                                        boolean expectedToBePerformed) {
        task.performFor(fileSet);
        assertEquals(expectedToBePerformed, task.areSourcesProcessed());
    }
}
