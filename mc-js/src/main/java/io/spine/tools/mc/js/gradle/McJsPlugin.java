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

import io.spine.tools.gradle.task.GradleTask;
import io.spine.tools.mc.gradle.LanguagePlugin;
import io.spine.tools.mc.js.code.index.CreateParsers;
import io.spine.tools.mc.js.code.index.GenerateIndexFile;
import io.spine.tools.mc.js.code.step.AppendTypeUrlGetter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import static io.spine.tools.gradle.task.BaseTaskName.build;
import static io.spine.tools.mc.js.gradle.McJsTaskName.generateJsonParsers;
import static kotlin.jvm.JvmClassMappingKt.getKotlinClass;

/**
 * The Gradle plugin which performs additional code generation for Protobuf types.
 *
 * <p>To run the plugin, add the {@code "io.spine.tools:spine-mc-js:$spineBaseVersion"}
 * to the {@code classpath} configuration and add
 * {@code apply plugin: 'io.spine.mc-js'} to the module generating JS messages.
 *
 * <p>In particular, the plugin:
 * <ul>
 *     <li>Generates a {@linkplain AppendTypeUrlGetter getter}
 *         to obtain a {@code TypeUrl} for each type.
 *     <li>Generates {@linkplain CreateParsers parsers} for types
 *         with standard JSON mapping.
 *     <li>{@linkplain GenerateIndexFile Exposes} all the messages and enums
 *         as well as generated parsers (to be used by the Spine Web).
 * </ul>
 *
 * <p>The main plugin action may be retrieved and configured as necessary via the
 * {@linkplain McJsOptions "protoJs" extension}. By default, the action is a dependency of the
 * {@linkplain io.spine.tools.gradle.task.BaseTaskName#build build} task.
 *
 * <p>This plugin currently relies on the set of the hard-coded Gradle settings which have to be
 * set to the required values in a project willing to use the plugin. These settings are:
 * <ol>
 *     <li>CommonJS import style for all generated code:
 *         {@code js {option "import_style=commonjs"}};
 * </ol>
 *
 * <p>The {@code build.gradle} file located under the {@code test/resources} folder of this module
 * can be used as an example of the required project configuration.
 */
public class McJsPlugin extends LanguagePlugin {

    @SuppressWarnings("unchecked")
    public McJsPlugin() {
        super(McJsOptions.NAME, getKotlinClass(McJsOptions.class));
    }

    @Override
    public void apply(Project project) {
        super.apply(project);
        ProtocConfig.applyTo(project);
        McJsOptions extension = McJsOptions.createIn(project);
        Task task = createTaskIn(project);
        extension.setGenerateParsersTask(task);
    }

    private static Task createTaskIn(Project project) {
        Action<Task> action = GenerateJsonParsers.newAction(project);
        GradleTask newTask = GradleTask.newBuilder(generateJsonParsers, action)
                .insertBeforeTask(build)
                .applyNowTo(project);
        Task task = newTask.getTask();
        return task;
    }
}
