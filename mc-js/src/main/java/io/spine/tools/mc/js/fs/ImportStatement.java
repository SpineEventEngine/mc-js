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

package io.spine.tools.mc.js.fs;

import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.Immutable;
import io.spine.logging.WithLogging;
import io.spine.tools.code.Element;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.fs.FileReference;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * An import line extracted from a source file for being
 * {@linkplain #resolve(Path, ExternalModules) resolved}.
 */
@Immutable
final class ImportStatement implements Element, WithLogging {

    private static final String GOOGLE_PROTOBUF_MODULE = "google-protobuf";
    private static final Pattern GOOGLE_PROTOBUF_MODULE_PATTERN =
            Pattern.compile(GOOGLE_PROTOBUF_MODULE + FileReference.separator());

    private static final String IMPORT_START = "require('";
    private static final String IMPORT_END = "')";

    /**
     * The relative path from the test sources directory to the {@code main} sources directory.
     *
     * <p>Depends on the structure of Spine Web project.
     */
    private static final String TEST_PROTO_RELATIVE_TO_MAIN = "../main/";

    private final Path sourceDirectory;
    private final String text;
    private final FileReference importRef;

    /**
     * Creates a new instance.
     *
     * @param file
     *         the file which declares the import statement
     * @param line
     *         the line with the statement
     */
    ImportStatement(JsFile file, String line) {
        this(requireNonNull(file.parent(), "File has no parent."),
             checkNotNull(line, "Null line passed."));
    }

    private ImportStatement(Path sourceDirectory, String line) {
        this.sourceDirectory = sourceDirectory;
        this.text = ensureImport(line);
        this.importRef = fileRefValue(line);
    }

    private static String ensureImport(String line) {
        checkArgument(
                isDeclaredIn(line),
                "An import statement should be like: `%s ... %s`.", IMPORT_START, IMPORT_END
        );
        return line;
    }

    /**
     * Tells whether the line contains an import statement.
     */
    static boolean isDeclaredIn(String line) {
        return line.contains(IMPORT_START);
    }

    /**
     * Replaces the import from {@code google-protobuf} module by a relative import.
     * Then, the import is resolved among external modules.
     *
     * <p>Such a replacement is required since we want to use own versions
     * of standard types, which are additionally processed by the Model Compiler for JS.
     *
     * <p>The custom versions of standard Protobuf types are provided by
     * the {@linkplain ExternalModule#spineWeb() Spine Web}.
     */
    ImportStatement resolve(Path generatedRoot, ExternalModules modules) {
        var resolved = this;
        if (containsGoogleProtobufType()) {
            resolved = relativizeStandardProtoImport(generatedRoot);
        }
        if (resolved.isUnresolvedRelativeImport()) {
            resolved = resolved.resolveRelativeTo(modules);
        }
        return resolved;
    }

    /**
     * Tells if this statement reference a standard Protobuf type
     * ({@code google-protobuf/google/protobuf/..}).
     */
    private boolean containsGoogleProtobufType() {
        return importRef.value()
                        .startsWith(GOOGLE_PROTOBUF_MODULE + "/google/protobuf/");
    }

    /**
     * Tells if this statement refers to a file which cannot be found on the file system.
     */
    private boolean isUnresolvedRelativeImport() {
        var isRelative = importRef.isRelative();
        var fileDoesNotExist = !importedFileExists();
        return isRelative && fileDoesNotExist;
    }

    /**
     * Attempts to resolve a relative import.
     */
    private ImportStatement resolveRelativeTo(ExternalModules modules) {
        var mainSourceImport = resolveInMainSources();
        if (mainSourceImport.isPresent()) {
            return mainSourceImport.get();
        }
        for (var module : modules.asList()) {
            if (module.provides(importRef)) {
                var fileInModule = module.fileInModule(importRef);
                return replaceRef(fileInModule.value());
            }
        }
        return this;
    }

    /**
     * Attempts to resolve a relative import among main sources.
     */
    private Optional<ImportStatement> resolveInMainSources() {
        var fileReference = importRef.value();
        var delimiter = FileReference.currentDirectory();
        var insertionIndex = fileReference.lastIndexOf(delimiter) + delimiter.length();
        var updatedReference = fileReference.substring(0, insertionIndex)
                + TEST_PROTO_RELATIVE_TO_MAIN
                + fileReference.substring(insertionIndex);
        var updatedImport = replaceRef(updatedReference);
        return updatedImport.importedFileExists()
               ? Optional.of(updatedImport)
               : Optional.empty();
    }

    private ImportStatement relativizeStandardProtoImport(Path generatedRoot) {
        var fileReference = importRef.value();
        var relativePathToRoot = sourceDirectory.relativize(generatedRoot)
                                                .toString();
        var replacement =
                relativePathToRoot.isEmpty()
                ? FileReference.currentDirectory()
                : relativePathToRoot.replace('\\', '/') + FileReference.separator();
        var relativeReference =
                GOOGLE_PROTOBUF_MODULE_PATTERN.matcher(fileReference)
                                              .replaceFirst(replacement);
        return replaceRef(relativeReference);
    }

    /**
     * Obtains the file reference used in this import.
     */
    @VisibleForTesting
    FileReference fileRef() {
        return importRef;
    }

    private static FileReference fileRefValue(String text) {
        var beginIndex = text.indexOf(IMPORT_START) + IMPORT_START.length();
        var endIndex = text.indexOf(IMPORT_END, beginIndex);
        var importPath = text.substring(beginIndex, endIndex);
        return FileReference.of(importPath);
    }

    /**
     * Obtains a new instance with the updated path in the import statement.
     */
    public ImportStatement replaceRef(CharSequence newFileRef) {
        var updatedText = text.replace(importRef.value(), newFileRef);
        return new ImportStatement(sourceDirectory, updatedText);
    }

    @Override
    public String text() {
        return text;
    }

    /**
     * Obtains the text of the import.
     */
    @Override
    public String toString() {
        return text;
    }

    /**
     * Tells whether the imported file is present on a file system.
     */
    private boolean importedFileExists() {
        var filePath = importedFilePath();
        var exists = filePath.toFile()
                             .exists();
        logger().atDebug()
                .log(() -> format("Checking if the imported file `%s` exists, result: %b.",
                                  filePath, exists));
        return exists;
    }

    /**
     * Obtains the absolute path to the imported file.
     */
    Path importedFilePath() {
        var filePath = sourceDirectory.resolve(importRef.value());
        return filePath.normalize();
    }
}
