# SDD Stack Contract

> This file declares the technology stack and architectural constraints of this project.
> It is read by skills and subagents to make stack-aware decisions.
> Fill in each section and commit this file to version control.

---

## Stack Identity

- **Project Name:** `trimfx` (Trim FX — Armor Trim Set & Material Bonuses)
- **Primary Language:** Java 25
- **Runtime / Platform:** JVM 25 (`C:/Users/jaca/.jdks/openjdk-25.0.1`) / Minecraft `26.3`
- **Framework:** NeoForge `26.3.0.95` (`neo_form_version=26.3-20261021.000000`) / ModDevGradle `2.0.148`
- **Package Manager:** Gradle (ModDevGradle `net.neoforged.moddev`) & Maven (`mvn clean test -f pom-test.xml` for standalone JUnit 5 domain test execution)

---

## Project Structure

- **Source Root:** `src/main/java/com/trimfx/`
- **Resources Root:** `src/main/resources/`
- **Test Root:** `src/test/java/com/trimfx/`
- **Entry Point:** `src/main/java/com/trimfx/TrimFxMod.java`
- **Build Output:** `build/`

---

## Test Runner

- **Command:** `.\gradlew.bat test` (and `mvn clean test -f pom-test.xml` for fast pure-Java domain unit tests)
- **Test Framework:** JUnit Jupiter 5 (JUnit 5)
- **Coverage Command:** `.\gradlew.bat check`

---

## Code Style

- **Linter:** Java Compiler (`-Xlint:all`) + `minecraft-clean-code` checklist
- **Formatter:** Standard Java 25 Mojmap conventions (4-space indentation, `record` DTOs, guard clauses)
- **Format Command:** `.\gradlew.bat compileJava compileTestJava`

---

## API Contract (if applicable)

- **API Type:** Minecraft 26.3 & NeoForge 26.3.0.95 Event Bus, `DataComponents.TRIM` (`ArmorTrim`), `AttributeModifier` (`Identifier`), and `ModConfigSpec`
- **Spec Location:** `openspec/specs/armor-trim-effects.md`
- **Base URL (dev):** N/A (Minecraft Mod Client/Server Runtime)

---

## Database (if applicable)

- **Engine:** Minecraft Data Components (`DataComponents.TRIM`) & NeoForge `ModConfigSpec` (`trimfx-common.toml`)
- **Migration Tool:** N/A
- **Migration Command:** N/A

---

## Notes

- **Minecraft 26.3 Invariants:**
  - Always use `net.minecraft.resources.Identifier` (`Identifier.fromNamespaceAndPath(MOD_ID, path)`). Never use `ResourceLocation`.
  - Use top-level `@EventBusSubscriber(modid = TrimFxMod.MOD_ID)` annotations without the removed `bus = ...` parameter.
  - Isolate all client-only classes (`ItemTooltipEvent` client formatting, particles/sounds helpers) from server logic (`if (level.isClientSide()) return;`).
