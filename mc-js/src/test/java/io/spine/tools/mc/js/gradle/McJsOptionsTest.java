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

package io.spine.tools.mc.js.gradle;

import com.google.common.collect.ImmutableList;
import io.spine.tools.fs.DirectoryPattern;
import io.spine.tools.fs.ExternalModule;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`McJsOptions` should")
class McJsOptionsTest {

    // As defined by `resources/META-INF/gradle-plugins/io.spine.mc-js.properties`.
    private static final String PLUGIN_ID = "io.spine.mc-js";

    private static final String GROUP_ID = "my.company";
    private static final String VERSION = "42";

    private Project project;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        project = ProjectBuilder.builder()
                .withProjectDir(tempDirPath.toFile())
                .build();
        var pluginManager = project.getPluginManager();
        pluginManager.apply("java");
        pluginManager.apply(PLUGIN_ID);

        project.setGroup(GROUP_ID);
        project.setVersion(VERSION);
    }

    @Test
    @DisplayName("add custom modules to resolve")
    void setModulesToResolve() {
        var moduleName = "foo-bar-module";
        var options = McJsOptions.in(project);
        var modules = options.modules;
        var customModules = ImmutableList.of(
                "foo",
                "bar",
                "baz"
        );
        modules.put(moduleName, customModules);

        var combinedModules = options.combinedModules();

        var patterns = customModules.stream()
                .map(DirectoryPattern::of)
                .collect(Collectors.toList());
        var expected = new ExternalModule(moduleName, patterns);
        assertThat(combinedModules.asList())
                .contains(expected);
    }
}
