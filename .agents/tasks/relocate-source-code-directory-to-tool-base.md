---
slug: relocate-source-code-directory-to-tool-base
branch: relocate-source-code-directory-to-tool-base
owner: claude
status: blocked
started: 2026-06-04
---

## Goal

Follow the upstream relocation of `SourceCodeDirectory` (and
`AbstractDirectory`) out of the published `spine-base` artifact into
`tool-base`. In `mc-js` this means re-pointing the import
`io.spine.code.fs.SourceCodeDirectory` →
`io.spine.tools.fs.SourceCodeDirectory` in the 9 files that use it.

## Status — BLOCKED

The 9-file import rewrite is **committed on this branch** but **does not
compile yet**, and won't until `mc-js` is modernized to the new
dependency baseline.

Rewritten + committed (import FQN + import order):
- `mc-js/.../code/index/CreateParsers.java`
- `mc-js/.../code/index/GenerateIndexFile.java`
- `mc-js/.../code/step/AppendTypeUrlGetter.java`
- `mc-js/.../code/step/CodeGenStep.java`
- `mc-js/.../code/step/CompiledProtoBelongsToModule.java`
- `mc-js/.../fs/FileWriter.java`
- `mc-js/.../code/given/TestCodeGenStep.java` (test)
- `mc-js/.../code/index/GenerateIndexFileTest.java` (test)
- `mc-js/.../code/step/CodeGenStepTest.java` (test)

## Why it is blocked

`mc-js` (master and feature branches) is on the legacy
`io.spine.internal.dependency.Spine` baseline:

- `spine-base` `2.0.0-SNAPSHOT.195`, `spine-tool-base` `.192`,
  `spine-model-compiler` + `spine-mc-java-plugins` `.175`.
- `config` submodule on the **v1** line.

The moved class only exists in `spine-base 2.0.0-SNAPSHOT.400` /
`tool-base 2.0.0-SNAPSHOT.390`, which:

- use the new `io.spine.dependency` layout, **and**
- **drop the `spine-` prefix** for the tools group
  (`spine-tool-base` → `tool-base`, `spine-plugin-base` → `plugin-base`).

`mc-js` is built **on top of** `model-compiler` / `mc-java`, so the
upgrade cascades — a version bump alone cannot resolve it.

## Prerequisite chain to unblock (separate, multi-repo project)

1. Modernize and publish `model-compiler` and `mc-java` to `spine-base`
   `.400`.
2. Migrate `mc-js`'s `config` submodule (v1 → current) and `buildSrc`
   dependency layout to `io.spine.dependency`.
3. Bump `mc-js` coordinates to `spine-base .400` / `tool-base .390`
   (note the artifact renames) and reconcile the base API drift across
   ~200 snapshot versions.
4. The committed import rewrite then compiles → `./gradlew build`.

## Notes

- `tool-base 2.0.0-SNAPSHOT.390` is already published to Maven Local with
  the moved classes (`io/spine/tools/fs/{AbstractDirectory,
  SourceCodeDirectory}.class`).
- Upstream move (delete from `base`, add to `tool-base`) is tracked in the
  `base-libraries` repo.
