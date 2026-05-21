# Multiloader Mod Template (Multi-Version)

[![Java Version](https://img.shields.io/badge/Java-21%20/%2025-orange.svg)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Stonecutter](https://img.shields.io/badge/Powered%20By-Stonecutter-blueviolet.svg)](https://github.com/kikugie/stonecutter)

Template for building multi-platform Minecraft mods targeting multiple Minecraft versions simultaneously. It utilizes **Stonecutter** for version orchestration, allowing you to write your mod's logic once and build it for both **Fabric** and **NeoForge** across various Minecraft versions (e.g., standard obfuscated versions like `1.21.1` and new versions like `26.1`).

---

## 📂 Project Structure

This template organizes code using a shared code directory and version-specific target builds managed by Stonecutter:
- **`src/`**: Contains the main source code folders.
  - **`common/`**: Shared codebase (Java and resources) used across all versions and loaders.
  - **`fabric/`**: Fabric-specific shared codebase.
  - **`neoforge/`**: NeoForge-specific shared codebase.
- **`versions/`**: Holds target directories where Stonecutter processes and generates the active workspaces for compiling specific versions (e.g. `1.21.1-fabric`, `26.1-neoforge`).
- **`gradle/targets/`**: Configuration files defining version-specific versions and dependencies (e.g., `1.21.1.properties`, `26.1.properties`).

```text
├── gradle/
│   └── targets/              # Version configuration profiles (1.21.1.properties, etc.)
├── src/                      # Shared code directories
│   ├── common/               # Core mod classes and resources
│   ├── fabric/               # Shared Fabric entrypoints & mixes
│   └── neoforge/             # Shared NeoForge event handlers & configuration
├── versions/                 # Stonecutter target output workspaces
│   ├── 1.21.1-fabric/
│   ├── 1.21.1-neoforge/
│   ├── 26.1-fabric/
│   └── 26.1-neoforge/
├── buildSrc/                 # Shared publishing and build logic
├── gradle.properties         # Central mod metadata
├── stonecutter.gradle.kts    # Stonecutter project setup
└── settings.gradle.kts       # Dynamic settings & project loading
```

## ⚙️ Configuration

### 1. Mod Metadata
Update the following properties in `gradle.properties` at the root of the project:
```properties
mod.id=examplemod               # Unique ID of your mod
mod.name=Example Mod             # Display name of your mod
mod.version=1.0.0               # Your mod's version
mod.group=com.example.examplemod # Base Java package
```

### 2. Minecraft Target Properties
To configure dependencies for a specific Minecraft version, edit its property file inside `gradle/targets/` (e.g. `1.21.1.properties`).
Do not edit the `[VERSIONED]` placeholders in the root `gradle.properties`.

## 🪄 Preprocessor Directives

We use Stonecutter's preprocessor to write version-specific or loader-specific code inside the shared `src/` directories.

Examples:
```java
//#if fabric
import net.fabricmc.api.ModInitializer;
//#else
import net.neoforged.fml.common.Mod;
//#endif

//#if mc >= 26.1
// Code specific to Minecraft 26.1+
//#else
// Code for older versions
//#endif
```

## 🛠️ Building & Running

### 1. Build All Targets
To compile and assemble jars for all loaders and target Minecraft versions, simply run:
```bash
./gradlew build
```
Jars will be compiled and collected into:
- Fabric: `build/libs/[version]/fabric/`
- NeoForge: `build/libs/[version]/neoforge/`

### 2. Build Specific Loader/Version
To build a specific configuration target:
```bash
./gradlew :1.21.1-fabric:build
./gradlew :26.1-neoforge:build
```

### 3. Run Specific Target
To launch the client/server for debugging:
* **Fabric Client (1.21.1)**: `./gradlew :1.21.1-fabric:runClient`
* **NeoForge Client (26.1)**: `./gradlew :26.1-neoforge:runClient`

## 🚀 Publishing

Publishing parameters are located in `gradle.properties` (`publish.github.repository`, `publish.modrinth.project_id`, `publish.curseforge.project_id`).

Run the publishing tasks with your API tokens set as environment variables:
- `MODRINTH_API_KEY`
- `CURSEFORGE_API_KEY`
- `GITHUB_TOKEN`

## 📄 License

This template is licensed under the **MIT License**. See [LICENSE](LICENSE) for more details.