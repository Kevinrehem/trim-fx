# Tasks: Bootstrap TrimFX Mod (Armor Trim Set & Material Bonuses)

> **Change ID:** `bootstrap-trimfx-mod`
> **Linked Proposal:** [proposal.md](./proposal.md)
> **Status:** complete
>
> All tasks must be `[x]` before this change can be archived via `sdd-spec-archive`.

---

## Implementation Tasks

- [x] **T-01:** Create Gradle & Maven build files (`gradle.properties`, `settings.gradle`, `build.gradle`, `pom-test.xml`) targeting Minecraft `26.3`, NeoForge `26.3.0.95`, ModDevGradle `2.0.148`, and Java 25.
- [x] **T-02:** Create mod metadata (`src/main/resources/META-INF/neoforge.mods.toml`) and entrypoint `TrimFxMod.java` (`@Mod("trimfx")`) with `TrimFxConfig` (`ModConfigSpec`).
- [x] **T-03:** Implement pure domain model (`TrimTier`, `ArmorSlotTrim`, `TrimSetSummary`, `SetBonusCalculator`) supporting independent 2/3/4 piece counting for Patterns and Materials.
- [x] **T-04:** Implement `MaterialBonusCatalog` and `PatternBonusCatalog` containing immutable scaling definitions and calculation helpers for all 11 Vanilla Trim Materials and 18 Vanilla Trim Patterns.
- [x] **T-05:** Implement `TrimEquipmentExtractor` and `MaterialAttributeApplier` using Minecraft 26.3 `DataComponents.TRIM` and `AttributeModifier(Identifier, double, Operation)` to manage all 11 Material attribute buffs and continuous Pattern attribute buffs (`silence`, `wayfinder`, `coast`).
- [x] **T-06:** Implement `PatternEffectService` and `TrimCommonEvents` for the 18 Trim Pattern set bonuses (combat procs, damage mitigation, healing/food buffs, durability conservation, XP/item magnetism, Piglin/Enderman AI interactions, Sculk vibration suppression, and `ServerLevel#sendParticles` / sound feedback).
- [x] **T-07:** Implement client-only `TrimClientTooltipEvents` (`ItemTooltipEvent`) for dynamic `[X/4]` set status on trimmed armor, `[Shift]` Tier I/II/III breakdown, and Smithing Template previews.
- [x] **T-08:** Create complete localization files (`assets/trimfx/lang/en_us.json` and `assets/trimfx/lang/pt_br.json`).
- [x] **T-09:** Add Biomes O' Plenty `biomesoplenty:rose_quartz` compatibility (`Attributes.MAX_ABSORPTION` `+2/+4/+6` + `8%/15%/25%` melee Lifesteal with overheal absorption, optional dependency in `neoforge.mods.toml`, localization, and unit tests).

## Security Validation Tasks

- [x] **SV-01:** Verify all gameplay mutations (attributes, damage, healing, XP, durability) execute strictly on the logical server (`!level.isClientSide()`) and zero `net.minecraft.client.*` imports exist in common/server classes.
- [x] **SV-02:** Verify reentrancy guard prevents infinite damage loops in `tide` (Thorns reflection) and `bolt` (chain lightning), and verify all `ModConfigSpec` entries use bounded ranges (`defineInRange`).
- [x] **SV-03:** Confirm zero hardcoded secrets/credentials exist and tick handlers are throttled via modulo game time without static `Entity`/`Level` retention.

## Shift-Left Test Suite Tasks

### Happy Path
- [x] **TP-01:** Write unit tests in `SetBonusCalculatorTest` verifying 2, 3, and 4 matching pieces resolve to `TIER_1`, `TIER_2`, and `TIER_3` independently for Patterns and Materials.
- [x] **TP-02:** Write unit tests in `SetBonusCalculatorTest` verifying hybrid armor sets (e.g., 4 `silence` + 2 `gold` + 2 `quartz`, and `2 dune + 2 tide`) activate all matching tiers simultaneously.
- [x] **TP-03:** Write unit tests in `MaterialBonusCatalogTest` verifying all 11 Vanilla materials exist and scale strictly (`TIER_1 < TIER_2 < TIER_3`).
- [x] **TP-04:** Write unit tests in `PatternBonusCatalogTest` verifying all 18 Vanilla patterns exist and compute accurate Tier I, II, and III scaling values.

### Edge Cases & Failure Modes
- [x] **TE-01:** Write unit tests in `SetBonusCalculatorTest` verifying 0 or 1 matching pieces return `TrimTier.NONE`, and null/empty/oversized inputs are safely handled.
- [x] **TE-02:** Write unit tests in `MaterialBonusCatalogTest` and `PatternBonusCatalogTest` verifying unknown/modded `Identifier`s return `Optional.empty()` safely and out-of-range piece counts clamp properly.

## Documentation Tasks

- [x] **D-01:** Update `README.md` with complete TrimFX documentation (18 Pattern bonuses, 11 Material bonuses, build/test commands).
- [x] **D-02:** Finalize canonical spec stub `openspec/changes/bootstrap-trimfx-mod/specs/armor-trim-effects.md`.

## Review Gate

- [x] All implementation tasks complete
- [x] All security validation tasks complete
- [x] All tests passing (`mvn clean test -f pom-test.xml` and `.\gradlew.bat compileJava test`)
- [x] Proposal acceptance criteria verified (run `sdd-spec-validate`)
- [x] Ready for `sdd-spec-archive`
