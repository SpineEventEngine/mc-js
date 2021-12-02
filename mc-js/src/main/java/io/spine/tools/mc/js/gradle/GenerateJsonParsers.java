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

package io.spine.tools.mc.js.gradle;

import com.google.common.collect.ImmutableList;
import io.spine.code.proto.FileSet;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.gradle.ProtoFiles;
import io.spine.tools.gradle.SourceSetName;
import io.spine.tools.js.fs.DefaultJsPaths;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.mc.js.code.index.CreateParsers;
import io.spine.tools.mc.js.code.index.GenerateIndexFile;
import io.spine.tools.mc.js.code.step.AppendTypeUrlGetter;
import io.spine.tools.mc.js.code.step.CodeGenStep;
import io.spine.tools.mc.js.code.step.ResolveImports;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.project.Projects.getSourceSetNames;

/**
 * Generates JSON parsers in the JavaScript code generated by Protobuf Compiler.
 *
 * @see #newAction(Project)
 */
final class GenerateJsonParsers implements Action<Task> {

    private final Project project;
    private final ExternalModules modules;

    private GenerateJsonParsers(Project project) {
        this.project = checkNotNull(project);
        this.modules = McJsOptions.in(project).combinedModules();
    }

    /**
     * Creates an {@code Action} to perform the additional generation of code
     * for working with Protobuf types.
     *
     * <p>The action handles all source sets in the given project.
     *
     * <p>The paths to the generated JS messages location, as well as to the descriptor set file,
     * are currently hard-coded.
     *
     * <p>Please see {@link DefaultJsPaths} for the expected layout of directories.
     */
    static Action<Task> newAction(Project project) {
        return new GenerateJsonParsers(project);
    }

    @Override
    public void execute(Task task) {
        List<SourceSetName> sourceSetNames = getSourceSetNames(project);
        for (SourceSetName ssn : sourceSetNames) {
            generateFor(ssn);
        }
    }

    private void generateFor(SourceSetName ssn) {
        Supplier<FileSet> files = ProtoFiles.collect(project, ssn);
        ImmutableList<CodeGenStep> steps = createSteps(ssn);
        FileSet suppliedFiles = files.get();
        for (CodeGenStep step : steps) {
            step.performFor(suppliedFiles);
        }
    }

    private ImmutableList<CodeGenStep> createSteps(SourceSetName ssn) {
        Directory generatedRoot = generatedRootFor(ssn);
        ImmutableList<CodeGenStep> steps = ImmutableList.of(
                new CreateParsers(generatedRoot),
                new AppendTypeUrlGetter(generatedRoot),
                new GenerateIndexFile(generatedRoot),
                new ResolveImports(generatedRoot, modules)
        );
        return steps;
    }

    private Directory generatedRootFor(SourceSetName ssn) {
        DefaultJsPaths jsPaths = DefaultJsPaths.at(project.getProjectDir());
        Path subDir = jsPaths.generated()
                             .path()
                             .resolve(ssn.getValue());
        Directory generatedRoot = Directory.at(subDir);
        return generatedRoot;
    }
}