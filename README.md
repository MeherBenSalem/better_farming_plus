# Better Farming ++

Minecraft farming quality-of-life mod: right-click harvest, harvest indicators, and a farming crate.

## Supported versions

| Folder   | Minecraft | Loaders            | Java |
|----------|-----------|--------------------|------|
| `1.20.1` | 1.20.1    | Fabric, Forge      | 17   |
| `1.21.1` | 1.21.1    | Fabric, NeoForge   | 21   |

Each version folder is an independent MultiLoader Gradle project (`common` + loader modules).

## Features

- Right-click harvest of mature crops with a whitelisted hoe
- Visual harvest indicators on ready crops (configurable)
- Farming crate that auto-harvests nearby mature crops into its inventory
- Farmland trampling protection when holding a whitelisted hoe

## Requirements

- Matching Minecraft + loader
- [JaUML](https://modrinth.com/) for the same loader/version (local jars under `libs/` for builds)

## Building

```bash
# Forge + Fabric 1.20.1 (Java 17)
cd 1.20.1
./gradlew build

# NeoForge + Fabric 1.21.1 (Java 21)
cd 1.21.1
./gradlew build
```

Jars are written under each loader module’s `build/libs/` (for example `forge/build/libs/`, `fabric/build/libs/`).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Security

See [SECURITY.md](.github/SECURITY.md).

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
See [NOTICE](NOTICE) for copyright attribution.
