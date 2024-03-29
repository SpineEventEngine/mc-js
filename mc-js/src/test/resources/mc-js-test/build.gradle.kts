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

import com.google.protobuf.gradle.*
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Spine
import io.spine.internal.gradle.standardToSpineSdk

plugins {
    java
    protobuf
}

// NOTE: this file is copied from the root project in the test setup.
apply(from = "$rootDir/test-env.gradle")
val enclosingRootDir: String by extra

repositories {
    standardToSpineSdk()
}

tasks {
    compileJava { enabled = false }
    compileTestJava { enabled = false }
}

val compileProtoToJs by tasks.registering

protobuf {
    generatedFilesBaseDir = "$projectDir/generated"
    protoc {
        artifact = Protobuf.JsRenderingProtoc.compiler
    }
    generateProtoTasks {
        // Copy the task collection to avoid `ConcurrentModificationException`.
        ArrayList(all()).forEach { task ->
            task.builtins {
                remove("java")
                id("js") {
                    option("import_style=commonjs")
                }
            }
            task.generateDescriptorSet = true
            task.descriptorSetOptions.path = "${projectDir}/build/descriptors/${task.sourceSet.name}/known_types.desc"
            task.descriptorSetOptions.includeImports = true
            task.descriptorSetOptions.includeSourceInfo = true

            compileProtoToJs.get().dependsOn(task)
        }
    }
}

tasks.build {
    dependsOn(compileProtoToJs)
}

dependencies {
    // Proto files coming from `base` are to be generated into JS.
    protobuf("io.spine:spine-base:${Spine.ArtifactVersion.base}:proto@jar")
    
    // We want standard Google files to be used for imports.
    implementation(Protobuf.protoSrcLib)

    // See https://github.com/google/protobuf-gradle-plugin#protos-in-dependencies for details.
}
