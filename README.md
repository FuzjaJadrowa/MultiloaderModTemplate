# Multiloader Mod Template (Single-Version)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue.svg)](https://minecraft.net)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern, clean, and developer-friendly template for building multi-platform Minecraft mods targeting a single Minecraft version. It allows you to write your mod's logic once in a shared project and build it for both **Fabric** and **NeoForge**.

---

## 📂 Project Structure

This template uses a standard three-project structure:
- **`common`**: Contains the main mod logic, assets, and code that is independent of any specific mod loader.
- **`fabric`**: The Fabric-specific project. It handles entry points, loader-specific registration, and packs the common code into the Fabric jar.
- **`neoforge`**: The NeoForge-specific project. It handles NeoForge registration, events, and configuration.

```text
├── buildSrc/                 # Custom build logic and utilities
├── common/                   # Shared mod code and resources
│   └── src/main/
│       ├── java/             # Shared Java classes
│       └── resources/        # Shared assets and data (lang, textures, mixins, etc.)
├── fabric/                   # Fabric-specific code and packaging
├── neoforge/                 # NeoForge-specific code and packaging
├── gradle.properties         # Central configuration for mod metadata and dependencies
└── settings.gradle.kts       # Gradle project structures
```

---

## ⚙️ Configuration

All configuration is centralized inside the `gradle.properties` file at the root of the project.

### 1. Mod Metadata
Update the following properties to match your mod's info:
```properties
mod.id=examplemod               # Unique ID of your mod (lowercase, alphanumeric, underscores)
mod.name=Example Mod             # Display name of your mod
mod.version=1.0.0               # Your mod's version (Semantic Versioning)
mod.group=com.example           # Java package prefix
```

### 2. Dependency Versions
Modify target versions for Minecraft, loaders, and libraries:
```properties
mod.mc_title=1.21.1             # Target Minecraft version
mod.java_version=21             # Required Java compatibility (21 for Minecraft 1.20.5+)

dep.fabric_loader=0.15.11       # Fabric Loader version
dep.fabric_api_version=0.102.0+1.21.1
dep.neoforge_loader=21.1.227    # NeoForge Loader version
```

---

## 🛠️ Getting Started & Building

1. **Clone/Use Template**: Initialize your repository using this template.
2. **Import**: Import the root project directory into your IDE (IntelliJ IDEA recommended). Make sure to let Gradle import and configure the workspace.
3. **Build**: Run the Gradle `build` task to build jars for all loaders:
   ```bash
   ./gradlew build
   ```
   The built jars will be placed in the respective loader's build output folders:
   - Fabric: `fabric/build/libs/`
   - NeoForge: `neoforge/build/libs/`

4. **Run Client / Server**:
   To test the mod locally, run the loader-specific run configurations:
   * **Fabric Client**: `./gradlew :fabric:runClient`
   * **NeoForge Client**: `./gradlew :neoforge:runClient`

---

## 🚀 Publishing

This template includes publishing tasks configured in `buildSrc/src/main/kotlin/PublishTools.kt`. Update the following keys in `gradle.properties` to enable publishing to Modrinth, CurseForge, and GitHub:

```properties
publish.github.repository=YourUsername/ExampleMod
publish.modrinth.project_id=examplemod
publish.curseforge.project_id=examplemod
```

Provide the API keys via environment variables when running publishing tasks:
- `MODRINTH_API_KEY`
- `CURSEFORGE_API_KEY`
- `GITHUB_TOKEN`

---

## 📄 License

This template is available under the **MIT License**. See [LICENSE](LICENSE) for more details.
