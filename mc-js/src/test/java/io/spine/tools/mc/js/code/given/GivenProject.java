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

package io.spine.tools.mc.js.code.given;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.code.proto.FileSet;
import io.spine.tools.gradle.testing.GradleProject;
import io.spine.tools.js.fs.DefaultJsPaths;
import io.spine.tools.js.fs.Directory;

import java.io.File;
import java.nio.file.Path;

import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.testing.TempDir.forClass;
import static io.spine.tools.gradle.BaseTaskName.build;

public final class GivenProject {

    private static final String TASK_PROTO = "task.proto";
    private static final String PROJECT_NAME = "mc-js-test";

    private final File projectDir;
    private boolean compiled = false;

    private GivenProject(Class<?> testSuite) {
        this.projectDir = forClass(testSuite);
    }

    public static GivenProject serving(Class<?> testSuite) {
        return new GivenProject(testSuite);
    }

    public FileSet mainFileSet() {
        Path mainDescriptorsDir = project().buildRoot()
                                           .descriptors()
                                           .mainDescriptors();
        Path descriptorSetFile = mainDescriptorsDir.resolve(KNOWN_TYPES);
        return FileSet.parse(descriptorSetFile.toFile());
    }

    public Directory mainProtoSources() {
        return project().generated()
                        .mainJs();
    }

    private DefaultJsPaths project() {
        compiled();
        DefaultJsPaths project = DefaultJsPaths.at(projectDir);
        return project;
    }

    @CanIgnoreReturnValue
    private GivenProject compiled() {
        if (!compiled) {
            compile();
            compiled = true;
        }
        return this;
    }

    private void compile() {
        GradleProject gradleProject = GradleProject.newBuilder()
                .setProjectName(PROJECT_NAME)
                .setProjectFolder(projectDir)
                .addProtoFile(TASK_PROTO)
                .build();
        gradleProject.executeTask(build);
    }
}