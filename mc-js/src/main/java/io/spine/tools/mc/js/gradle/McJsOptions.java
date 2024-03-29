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

import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.ExternalModules;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.tools.fs.ExternalModule.predefinedModules;

/**
 * An extension for the {@link McJsPlugin} which allows to obtain the {@code generateJsonParsers}
 * task to configure when it will be executed during the build lifecycle.
 */
@SuppressWarnings("PublicField" /* Expose fields as a Gradle extension */)
public class McJsOptions {

    /**
     * The name of the extension as it appears in a Gradle script under {@code modelCompiler}.
     */
    static final String NAME = "js";

    /**
     * Names of JavaScript modules and directories they provide.
     *
     * <p>Information about modules is used to resolve imports in generated Protobuf files.
     *
     * <p>Additionally to modules specified via the property,
     * the {@linkplain ExternalModule#predefinedModules() predefined Spine} modules are used.
     *
     * <p>An example of the definition:
     * <pre>{@code
     * modules = [
     *      // The module provides `company/client` directory (not including subdirectories).
     *      // So, an import path like {@code ../company/client/file.js}
     *      // becomes {@code client/company/client/file.js}.
     *      'client' : ['company/client'],
     *
     *      // The module provides `company/server` directory (including subdirectories).
     *      // So, an import path like {@code ../company/server/nested/file.js}
     *      // becomes {@code server/company/server/nested/file.js}.
     *      'server' : ['company/server/*'],
     *
     *      // The module provides 'proto/company` directory.
     *      // So, an import pah like {@code ../company/file.js}
     *      // becomes {@code common-types/proto/company/file.js}.
     *      'common-types' : ['proto/company']
     * ]
     * }</pre>
     */
    @SuppressWarnings(
            "UnrecognisedJavadocTag" /* ... `{@code }` within the code block example above. */
    )
    public Map<String, List<String>> modules = new HashMap<>();

    private Task generateParsersTask;

    /**
     * Creates the extension in the given project.
     */
    static McJsOptions createIn(Project project) {
        var extensions = project.getExtensions();
        var extension = extensions.create(NAME, McJsOptions.class);
        return extension;
    }

    static McJsOptions in(Project project) {
        return (McJsOptions)
                project.getExtensions()
                       .getByName(NAME);
    }

    ExternalModules combinedModules() {
        var combined = new ExternalModules(modules)
                .with(predefinedModules());
        return combined;
    }

    /**
     * Returns the {@code generateJsonParsers} task configured by the {@link McJsPlugin}.
     */
    @SuppressWarnings("unused") // Used in project applying the plugin.
    public Task generateParsersTask() {
        checkState(generateParsersTask != null,
                   "The 'generateJsonParsers' task was not configured by the ProtoJS plugin");
        return generateParsersTask;
    }

    /**
     * Makes the extension read-only for all plugin users.
     */
    void setGenerateParsersTask(Task generateParsersTask) {
        this.generateParsersTask = generateParsersTask;
    }
}
