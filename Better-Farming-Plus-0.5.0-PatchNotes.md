# Better Farming ++ v0.5.0

### New Features
* MultiLoader support: Fabric and Forge on Minecraft 1.20.1; Fabric and NeoForge on Minecraft 1.21.1

### Improvements
* Shared common gameplay module with loader-specific entry points
* Apache License 2.0 OSS packaging (LICENSE, NOTICE, contributing docs)
* JaUML dependency updated to 2.1.1

### Bug Fixes
* NeoForge client menu screen registration no longer races common setup

### Compatibility
* Minecraft 1.20.1 — Fabric, Forge
* Minecraft 1.21.1 — Fabric, NeoForge
* Requires JaUML for the matching loader and Minecraft version

### Upgrade Notes
* Replace previous single-loader jars with the matching loader jar from this release
* Config files under `betterfarming/global` are unchanged
