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

import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.ExternalModules;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginManager;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.mc.js.gradle.McJsOptions.in;
import static java.util.Collections.emptyList;

@DisplayName("`McJsOptions` should")
class McJsOptionsTest {

    private static final String PLUGIN_ID = "io.spine.mc-js";

    private static final String GROUP_ID = "my.company";
    private static final String VERSION = "42";

    private Project project;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        project = ProjectBuilder.builder()
                .withProjectDir(tempDirPath.toFile())
                .build();
        PluginManager pluginManager = project.getPluginManager();
        pluginManager.apply("java");
        pluginManager.apply(PLUGIN_ID);

        project.setGroup(GROUP_ID);
        project.setVersion(VERSION);
    }

    @Test
    @DisplayName("add custom modules to resolve")
    void setModulesToResolve() {
        String moduleName = "foo-bar";
        Map<String, List<String>> modulesExt = pluginExtension().modules;
        modulesExt.put(moduleName, emptyList());
        ExternalModules modules = pluginExtension().combinedModules();
        assertThat(modules.asList())
                .contains(new ExternalModule(moduleName, emptyList()));
    }

    private McJsOptions pluginExtension() {
        return in(project);
    }
}
