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

import com.google.common.truth.IterableSubject;
import io.spine.tools.code.SourceSetName;
import io.spine.tools.fs.DirectoryPattern;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.fs.Generated;
import io.spine.tools.mc.js.code.given.GivenProject;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.fs.ExternalModule.spineUsers;
import static io.spine.tools.fs.ExternalModule.spineWeb;
import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.asList;

@DisplayName("`ResolveImports` task should")
class ResolveImportsTest {

    private static final ExternalModule module = new ExternalModule(
            "test-module", DirectoryPattern.listOf("root-dir")
    );
    private static @MonotonicNonNull Generated generatedDir = null;
    private static @MonotonicNonNull Path jsFile = null;

    @BeforeAll
    static void compileProject() {
        var project = GivenProject.serving(ResolveImportsTest.class);
        generatedDir = project.generated();
        jsFile = generatedDir.path().resolve("js/with-imports.js");
    }

    @Test
    @DisplayName("replace a relative import of a missing file")
    void resolveMissingFileImport() throws IOException {
        var task = newTask(module);
        writeFile(jsFile, "require('./root-dir/missing.js');");
        afterResolve(jsFile, task).containsExactly("require('test-module/root-dir/missing.js');");
    }

    @Test
    @DisplayName("not replace a relative import of an existing file")
    void notResolveExistingFile() throws IOException {
        var task = newTask(module);
        var originalImport = "require('./root-dir/not-missing.js');";
        createFile("js/root-dir/not-missing.js");
        writeFile(jsFile, originalImport);
        afterResolve(jsFile, task).containsExactly(originalImport);
    }

    @Test
    @DisplayName("not clash Spine Web and Spine Users modules")
    void notClashUsersWithWeb() throws IOException {
        var modules = new ExternalModules(spineWeb(), spineUsers());
        var task = new ResolveImports(generatedDir, SourceSetName.main, modules);
        writeFile(jsFile, "require('../../spine/users/identifiers_pb.js');");
        afterResolve(jsFile, task)
                .containsExactly("require('spine-users/spine/users/identifiers_pb.js');");
    }

    @Test
    @DisplayName("resolve in main sources before external modules")
    void resolveMainSourcesFirstly() throws IOException {
        var task = newTask(module);
        writeFile(jsFile, "require('./root-dir/main.js');");
        createFile("main/root-dir/main.js");
        afterResolve(jsFile, task).containsExactly("require('./../main/root-dir/main.js');");
    }

    @Test
    @DisplayName("not replace a relative import if not matches patterns")
    void notReplaceIfNotProvided() throws IOException {
        var task = newTask(module);
        var originalImport = "require('./abcdef/missing.js');";
        writeFile(jsFile, originalImport);
        afterResolve(jsFile, task).containsExactly(originalImport);
    }

    @Test
    @DisplayName("relativize imports of standard Protobuf types")
    void relativizeStandardProtoImports() throws IOException {
        var task = newTask(module);
        writeFile(jsFile, "require('google-protobuf/google/protobuf/compiler/plugin_pb.js');");
        afterResolve(jsFile, task)
                .containsExactly("require('../google/protobuf/compiler/plugin_pb.js');");
    }

    @Test
    @DisplayName("relativize imports of standard Protobuf types in the same directory")
    void relativizeStandardProtoImportsInSameDir() throws IOException {
        var task = newTask(module);
        var file = generatedDir.path().resolve("google/protobuf/imports.js");
        writeFile(file, "require('google-protobuf/google/protobuf/type_pb.js');");
        afterResolve(file, task).containsExactly("require('../../google/protobuf/type_pb.js');");
    }

    @Test
    @DisplayName("relativize imports of standard Protobuf types in the root directory")
    void relativizeStandardProtoImportsInRoot() throws IOException {
        var task = newTask(module);
        var file = generatedDir.path().resolve("root.js");
        writeFile(file, "require('google-protobuf/google/protobuf/empty_pb.js');");
        afterResolve(file, task).containsExactly("require('./google/protobuf/empty_pb.js');");
    }

    @Test
    @DisplayName("resolve relative imports of standard Protobuf types in Spine Web")
    void resolveRelativeImportsOfStandardProtos() throws IOException {
        var task = newTask(spineWeb());
        var file = generatedDir.path().resolve("imports.js");
        writeFile(file, "require('google-protobuf/google/protobuf/timestamp_pb.js');");
        afterResolve(file, task)
                .containsExactly("require('spine-web/proto/google/protobuf/timestamp_pb.js');");
    }

    private static void createFile(String name) throws IOException {
        var filePath = generatedDir.path().resolve(name);
        createDirectories(filePath.getParent());
        Files.createFile(filePath);
    }

    private static void writeFile(Path file, String... lines) throws IOException {
        createDirectories(file.getParent());
        Files.write(file, asList(lines));
    }

    private static IterableSubject afterResolve(Path file, ResolveImports task) throws IOException {
        task.resolveInFile(file);
        List<String> lines = Files.readAllLines(file);
        return assertThat(lines);
    }

    private static ResolveImports newTask(ExternalModule module) {
        return new ResolveImports(generatedDir, SourceSetName.main, new ExternalModules(module));
    }
}
