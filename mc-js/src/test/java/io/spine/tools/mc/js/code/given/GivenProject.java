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
import io.spine.tools.code.SourceSetName;
import io.spine.tools.fs.SourceDir;
import io.spine.tools.gradle.testing.GradleProject;
import io.spine.tools.js.fs.DefaultJsPaths;

import java.io.File;

import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.testing.TempDir.forClass;
import static io.spine.tools.gradle.task.BaseTaskName.build;

public final class GivenProject {

    private final File projectDir;
    private boolean compiled = false;

    private GivenProject(Class<?> testSuite) {
        this.projectDir = forClass(testSuite);
    }

    public static GivenProject serving(Class<?> testSuite) {
        return new GivenProject(testSuite);
    }

    public FileSet mainFileSet() {
        var mainDescriptorsDir =
                project().buildRoot()
                         .descriptors()
                         .forSourceSet(SourceSetName.main.toString());
        var descriptorSetFile = mainDescriptorsDir.resolve(KNOWN_TYPES);
        return FileSet.parse(descriptorSetFile.toFile());
    }

    public SourceDir generatedMainJsSources() {
        return generated().dir(SourceSetName.main);
    }

    public DefaultJsPaths.GeneratedJs generated() {
        return project().generated();
    }

    private DefaultJsPaths project() {
        compiled();
        var project = DefaultJsPaths.at(projectDir);
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
        var gradleProject = GradleProject.setupAt(projectDir)
                .fromResources("mc-js-test")
                .copyBuildSrc()
                .create();
        gradleProject.executeTask(build);
    }
}
