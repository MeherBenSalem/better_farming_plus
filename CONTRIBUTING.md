# Contributing

Thanks for contributing to Better Farming ++.

## How to contribute

1. Fork the repository and create a branch from `main`.
2. Make focused changes for one issue or feature.
3. Build the version you touched:
   - `1.20.1` (Fabric + Forge, Java 17): `cd 1.20.1 && ./gradlew build`
   - `1.21.1` (Fabric + NeoForge, Java 21): `cd 1.21.1 && ./gradlew build`
4. Open a pull request with a short description of the change and how you tested it.

## Guidelines

- Prefer small, reviewable pull requests.
- Match existing package layout, naming, and coding style.
- Do not commit build outputs, IDE local config, or secrets.
- Report security issues privately (see [SECURITY.md](.github/SECURITY.md)).

## Licensing

Unless you state otherwise, contributions are licensed under the Apache License, Version 2.0.
