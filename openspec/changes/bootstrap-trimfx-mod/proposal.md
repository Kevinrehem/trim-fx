# Proposal: Bootstrap TrimFX Mod (Armor Trim Set & Material Bonuses)

> **Change ID:** `bootstrap-trimfx-mod`
> **Status:** approved
> **Author:** @jaca
> **Created:** 2026-10-23
> **Last Updated:** 2026-10-23

---

## Summary

Bootstrap and implement the **TrimFX** (`trimfx`) mod for **Minecraft 26.3** and **NeoForge 26.3.0.95** (Java 25, ModDevGradle `2.0.148`). TrimFX gives gameplay utility to all 18 Vanilla Armor Trim Patterns (`TrimPattern`), all 11 Vanilla Armor Trim Materials (`TrimMaterial`), and **Biomes O' Plenty's Rose Quartz (`biomesoplenty:rose_quartz`)** via an independent tiered set bonus system (**2 pieces = Tier I**, **3 pieces = Tier II**, **4 pieces = Tier III**). Each piece of trimmed armor contributes independently to its Pattern count and its Material count, granting both a thematic **Pattern Set Bonus** (passive/conditional status effects, combat procs, and utility mechanics) and a pure **Material Passive Attribute Bonus** (`AttributeModifier`s). All values and feature toggles are configurable via `trimfx-common.toml` (`ModConfigSpec`), and players receive dynamic tooltips on trimmed armor and Smithing Templates in both Portuguese (`pt_br`) and English (`en_us`), accompanied by subtle Vanilla audiovisual feedback on combat procs.

## Motivation

In Vanilla Minecraft, Armor Trims require rare exploration across dangerous structures (Ancient Cities, Trial Chambers, Bastions, Nether Fortresses, End Cities, Ocean Monuments, Trail Ruins) and consume valuable materials, yet offer only cosmetic changes. Players want meaningful build diversity and progression rewards for collecting and combining Armor Trims without forcing monochromatic material choices. By evaluating **Pattern** and **Material** piece counts independently across thresholds of 2, 3, and 4 pieces, players can mix or match up to two 2-piece sets or commit to a 3-piece or 4-piece full set for stronger scaling benefits.

## Scope

### In Scope
- **Build Toolchain & Mod Bootstrap:** `gradle.properties`, `settings.gradle`, `build.gradle` (ModDevGradle `2.0.148`, Minecraft `26.3`, NeoForge `26.3.0.95`, Java 25), `META-INF/neoforge.mods.toml`, and `TrimFxMod` entrypoint.
- **Core Domain Engine (`SetBonusCalculator` & `PlayerTrimState`):**
  - Pure, unit-testable domain evaluator that inspects equipped armor slots (`HEAD`, `CHEST`, `LEGS`, `FEET`), extracts pattern `Identifier` and material `Identifier` from `DataComponents.TRIM` (`ArmorTrim`), counts pieces per Pattern and per Material independently, and resolves active Tiers (`NONE`, `TIER_1` at 2 pieces, `TIER_2` at 3 pieces, `TIER_3` at 4 pieces).
- **11 Trim Material Passive Bonuses (`MaterialAttributeService` / Strategy Pattern):**
  - 100% native `AttributeModifier`s applied on `LivingEquipmentChangeEvent` using deterministic `Identifier`s (`Identifier.fromNamespaceAndPath("trimfx", "material_<material>_<attr>")`):
    1. `minecraft:iron` — `Attributes.ARMOR`: `+1.0 / +2.0 / +3.0`
    2. `minecraft:copper` — `Attributes.MOVEMENT_SPEED` (`ADD_MULTIPLIED_TOTAL`): `+4% / +8% / +12%`
    3. `minecraft:gold` — `Attributes.ATTACK_SPEED` (`ADD_MULTIPLIED_TOTAL`): `+5% / +10% / +15%`
    4. `minecraft:diamond` — `Attributes.ARMOR_TOUGHNESS`: `+1.0 / +2.0 / +3.5`
    5. `minecraft:netherite` — `Attributes.KNOCKBACK_RESISTANCE`: `+0.10 / +0.20 / +0.35` & `Attributes.BURNING_TIME` (`ADD_MULTIPLIED_TOTAL`): `-15% / -30% / -50%`
    6. `minecraft:redstone` — `Attributes.BLOCK_BREAK_SPEED` (`ADD_MULTIPLIED_TOTAL`): `+8% / +16% / +25%`
    7. `minecraft:lapis` — `Attributes.OXYGEN_BONUS`: `+1.0 / +2.0 / +3.0` & `Attributes.WATER_MOVEMENT_EFFICIENCY`: `+0.25 / +0.50 / +0.80`
    8. `minecraft:emerald` — `Attributes.LUCK`: `+1.0 / +2.0 / +3.0`
    9. `minecraft:quartz` — `Attributes.ATTACK_DAMAGE`: `+1.0 / +2.0 / +3.0`
    10. `minecraft:amethyst` — `Attributes.BLOCK_INTERACTION_RANGE` & `Attributes.ENTITY_INTERACTION_RANGE`: `+0.5 / +1.0 / +1.5`
    11. `minecraft:resin` — `Attributes.MAX_HEALTH`: `+2.0 / +4.0 / +6.0`
    12. `biomesoplenty:rose_quartz` (also matches `minecraft:rose_quartz` / path `rose_quartz` for modpack compatibility) — **Vitalidade Rosa**: `Attributes.MAX_ABSORPTION`: `+2.0 / +4.0 / +6.0` (`+1 / +2 / +3` Golden Hearts capacity) & Melee Lifesteal (`8% / 15% / 25%` of melee damage dealt heals the attacker, with excess healing overflowing into Golden Absorption Hearts up to `MAX_ABSORPTION`).
- **18 Trim Pattern Set Bonuses (`TrimPatternEffect` Strategy Registry & Event Handlers):**
  - **Group 1 — Overworld, Ocean & Trial Chambers:**
    1. `minecraft:sentry` — Ranged projectile damage bonus (`+10% / +20% / +35%`) + applies `Glowing` (5s) on Tier III ranged hit.
    2. `minecraft:vex` — Phantom dodge chance (`8% / 15% / +25%`) cancelling incoming attack damage with subtle phantom particle/sound.
    3. `minecraft:wild` — Poison duration/damage reduction (`50% / 75% / 100% immunity`) + grants `Regeneration I` (`3s / 5s / 8s`) on mob kill.
    4. `minecraft:dune` — Reduces Fire & Explosion damage (`15% / 25% / 40%`) + grants `Absorption` shield (`2 / 4 / 6` hearts for 10s, 30s cooldown) when hit below 40% health.
    5. `minecraft:coast` — Continuous `Water Breathing`, submerged mining speed (`+25% / +50% / +100%`), and `Dolphin's Grace` in water at Tier III.
    6. `minecraft:tide` — Hydraulic Thorns reflecting `15% / 25% / 40%` of taken damage back to attacker + grants `Conduit Power` when wet/in water at Tier III.
    7. `minecraft:flow` — Fall damage reduction (`40% / 70% / 100%`) + continuous `Jump Boost` (`I / II / III`).
    8. `minecraft:bolt` — Melee hits have `10% / 18% / 30%` chance (doubled in thunder/rain) to trigger chain electric shock (`2.0 / 4.0 / 6.0` damage) to nearby hostiles with electric spark particles.
  - **Group 2 — Deep Dark & Trail Ruins:**
    9. `minecraft:ward` — Immunity to `Darkness` effect + reduces incoming damage in low light (`light <= 7`) by `10% / 18% / 28%` + grants `Night Vision` in darkness at Tier III.
    10. `minecraft:silence` — Sneaking speed bonus (`+25% / +50% / +80%`), reduces hostile mob detection visibility (`-20% / -35% / -50%`), and suppresses Sculk/Warden movement & fall vibrations at Tier III.
    11. `minecraft:wayfinder` — Reduces movement/sprint exhaustion (`-25% / -50% / -75%`) + grants `+0.5` `STEP_HEIGHT` (step up full blocks automatically).
    12. `minecraft:raiser` — Increases incoming healing (`+15% / +30% / +50%`) + restores `+1 / +2 / +3` extra hunger/saturation when eating food.
    13. `minecraft:shaper` — Grants `15% / 30% / 45%` chance to prevent durability loss when breaking blocks or taking armor damage.
    14. `minecraft:host` — Increases experience orb value (`+20% / +40% / +70%`) + pulls nearby item entities and XP orbs within `2.0 / 3.5 / 5.0` blocks.
  - **Group 3 — Nether & End:**
    15. `minecraft:snout` — Makes Piglins neutral starting at Tier I (2 pieces), grants `+12% / +22% / +35%` damage in the Nether or against Piglin/Hoglin targets, and grants `25%` double barter drop chance at Tier III.
    16. `minecraft:rib` — Reduces `Wither` damage (`50% / 80% / 100% immunity`), melee attacks apply `Wither` (`3s / 5s / 6s`), and killing a Wither-afflicted target heals `2.0` HP (1 heart) at Tier III.
    17. `minecraft:eye` — Prevents Enderman stare aggro, negates Ender Pearl fall/teleport damage with `-25% / -50% / -75%` pearl cooldown, and increases critical hit damage by `+10% / +20% / +35%`.
    18. `minecraft:spire` — Converts hostile `Levitation` into `Slow Falling`, melee hits have `12% / 20% / 30%` chance to levitate targets (`1.5s`), and crouching in mid-air grants controlled `Slow Falling`.
- **Configuration (`TrimFxConfig`):**
  - NeoForge `ModConfigSpec` (`trimfx-common.toml`) allowing server admins/players to toggle individual pattern or material bonuses, global damage/attribute multipliers, and audiovisual effects.
- **Smart Dynamic Tooltips & Localization (`TrimTooltipHandler`):**
  - Displays active Pattern and Material set progress (`[2/4]`, `[3/4]`, `[4/4]`) and current Tier bonus on trimmed armor pieces, expands full Tier I/II/III progression when holding `[Shift]`, and previews Pattern bonuses directly on Smithing Template items.
  - Full `en_us.json` and `pt_br.json` localization files.

### Out of Scope
- Custom non-vanilla Armor Trim patterns or materials (may be added in a future change).
- Custom persistent HUD overlays on screen (all UI feedback is delivered via dynamic item/template tooltips and subtle Vanilla particles/sounds).
- Modifying Vanilla Smithing Table recipes or loot tables.

## Proposed Solution

### Approach
We will structure **TrimFX** following Clean Architecture and the `minecraft-design-patterns`, `minecraft-clean-code`, and `minecraft-neoforge-tech-spec` skills:
1. **Pure Domain Layer (`com.trimfx.domain`):**
   - `TrimTier` enum (`NONE(0)`, `TIER_1(2)`, `TIER_2(3)`, `TIER_3(4)`) with helper resolution `TrimTier.fromPieceCount(int count)` and scaling factor look-ups.
   - `TrimSetSummary` immutable Java `record` holding `Map<Identifier, Integer> patternCounts`, `Map<Identifier, Integer> materialCounts`, `Map<Identifier, TrimTier> activePatterns`, and `Map<Identifier, TrimTier> activeMaterials`.
   - `SetBonusCalculator` stateless service that accepts a collection of `(Identifier patternId, Identifier materialId)` pairs (up to 4 armor slots), validates non-null entries, and computes the `TrimSetSummary` in $O(1)$ time (4 slots max).
   - `MaterialBonusSpec` and `PatternBonusSpec` immutable records defining the exact scaling parameters for all 11 materials and 18 patterns so they can be unit-tested independently of the Minecraft runtime.
2. **Adapter / Gameplay Service Layer (`com.trimfx.service` & `com.trimfx.event`):**
   - `TrimEquipmentExtractor`: Extracts `(patternId, materialId)` from a `LivingEntity`'s 4 armor slots (`EquipmentSlot.HEAD`, `CHEST`, `LEGS`, `FEET`) via `stack.get(DataComponents.TRIM)` and `ArmorTrim#pattern()` / `ArmorTrim#material()`.
   - `MaterialAttributeApplier`: Listens to `LivingEquipmentChangeEvent`, removes stale `trimfx:material_*` `AttributeModifier`s, and applies the active Tier modifiers for both materials and continuous pattern attributes (`silence` sneaking speed, `wayfinder` step height, `coast` submerged mining speed).
   - `PatternCombatEventHandler` & `PatternTickEventHandler`: `@EventBusSubscriber(modid = TrimFxMod.MOD_ID)` listeners handling `LivingIncomingDamageEvent`, `LivingDamageEvent.Pre`, `LivingDamageEvent.Post`, `LivingDeathEvent`, `LivingHealEvent`, `LivingEntityUseItemEvent.Finish`, `PlayerXpEvent.PickupXp`, `VanillaGameEvent` (Sculk vibration suppression for `silence` Tier III), and throttled `PlayerTickEvent.Post` (every 20 ticks for conditional passives like `coast`, `tide`, `flow`, `ward`, and every 5 ticks for `host` item/XP magnet and `spire` mid-air sneak slow-falling).
3. **Client Tooltip Layer (`com.trimfx.client`):**
   - Isolated client-only `@EventBusSubscriber(modid = TrimFxMod.MOD_ID, value = Dist.CLIENT)` listening to `ItemTooltipEvent` to render active `[X/4]` Pattern & Material set bonuses and `[Shift]` tier breakdowns on trimmed armor and Smithing Templates.

### Impacted Files
| File | Change Type | Notes |
|------|-------------|-------|
| `README.md` | Modify | Document mod features, 18 pattern bonuses, 11 material bonuses, build, and test instructions |
| `gradle.properties` | Create | Minecraft 26.3, NeoForge 26.3.0.95, ModDevGradle configuration |
| `settings.gradle` | Create | Gradle plugin repositories and root project name `trimfx` |
| `build.gradle` | Create | ModDevGradle `2.0.148`, Java 25 toolchain, JUnit 5 test runner |
| `pom-test.xml` | Create | Standalone Maven configuration for fast pure-Java JUnit 5 unit tests |
| `src/main/resources/META-INF/neoforge.mods.toml` | Create | NeoForge mod metadata for `trimfx` targeting MC `26.3` / NeoForge `26.3.0.95` |
| `src/main/java/com/trimfx/TrimFxMod.java` | Create | `@Mod("trimfx")` entrypoint registering `TrimFxConfig` |
| `src/main/java/com/trimfx/config/TrimFxConfig.java` | Create | `ModConfigSpec` configuration for pattern/material toggles and multipliers |
| `src/main/java/com/trimfx/domain/TrimTier.java` | Create | Tier enum (`NONE`, `TIER_1`, `TIER_2`, `TIER_3`) with threshold logic |
| `src/main/java/com/trimfx/domain/ArmorSlotTrim.java` | Create | Immutable `record(Identifier patternId, Identifier materialId)` |
| `src/main/java/com/trimfx/domain/TrimSetSummary.java` | Create | Immutable `record` storing piece counts and active `TrimTier`s |
| `src/main/java/com/trimfx/domain/SetBonusCalculator.java` | Create | Stateless domain calculator for independent Pattern & Material matching |
| `src/main/java/com/trimfx/domain/MaterialBonusCatalog.java` | Create | Pure domain definitions and scaling values for all 11 Trim Materials |
| `src/main/java/com/trimfx/domain/PatternBonusCatalog.java` | Create | Pure domain definitions and scaling values for all 18 Trim Patterns |
| `src/main/java/com/trimfx/service/TrimEquipmentExtractor.java` | Create | Extracts `ArmorSlotTrim` from player armor slots via `DataComponents.TRIM` |
| `src/main/java/com/trimfx/service/MaterialAttributeApplier.java` | Create | Applies/removes deterministic `AttributeModifier`s on equipment change |
| `src/main/java/com/trimfx/service/PatternEffectService.java` | Create | Executes combat procs, conditional buffs, and audiovisual feedback for 18 patterns |
| `src/main/java/com/trimfx/event/TrimCommonEvents.java` | Create | Server/common NeoForge event bus listeners delegating to services |
| `src/main/java/com/trimfx/client/TrimClientTooltipEvents.java` | Create | Client-only `ItemTooltipEvent` listener for armor & Smithing Template tooltips |
| `src/main/resources/assets/trimfx/lang/en_us.json` | Create | English localization for all 18 patterns, 11 materials, and UI tooltips |
| `src/main/resources/assets/trimfx/lang/pt_br.json` | Create | Brazilian Portuguese localization for all 18 patterns, 11 materials, and UI tooltips |
| `src/test/java/com/trimfx/domain/SetBonusCalculatorTest.java` | Create | Unit tests for 2/3/4 piece independent matching, hybrid sets, and edge cases |
| `src/test/java/com/trimfx/domain/MaterialBonusCatalogTest.java` | Create | Unit tests verifying all 11 materials and tier scaling invariants |
| `src/test/java/com/trimfx/domain/PatternBonusCatalogTest.java` | Create | Unit tests verifying all 18 patterns, combat formulas, and tier scaling invariants |

### Data Model Changes (if applicable)
- Reads Vanilla `DataComponents.TRIM` (`net.minecraft.world.item.equipment.trim.ArmorTrim`) on `ItemStack`s.
- Uses transient entity persistent data / cooldown timestamps on `Player` (`player.getPersistentData()`) for Dune absorption shield cooldowns so no custom network protocol is required to connect to vanilla servers.

## Acceptance Criteria

- [x] **Given** a player equips 2, 3, or 4 armor pieces with the same `TrimPattern` (regardless of their `TrimMaterial`), **When** `SetBonusCalculator` evaluates the equipped armor, **Then** the Pattern activates `TIER_1` (2 pieces), `TIER_2` (3 pieces), or `TIER_3` (4 pieces) respectively.
- [x] **Given** a player equips 4 armor pieces with `minecraft:silence` pattern where 2 pieces use `minecraft:gold` material and 2 pieces use `minecraft:quartz` material, **When** equipment evaluation runs, **Then** the player simultaneously receives `TIER_3` of `silence`, `TIER_1` of `gold`, and `TIER_1` of `quartz`.
- [x] **Given** any of the 11 Vanilla Trim Materials (`iron`, `copper`, `gold`, `diamond`, `netherite`, `redstone`, `lapis`, `emerald`, `quartz`, `amethyst`, `resin`) reaches Tier I, II, or III, **When** `LivingEquipmentChangeEvent` fires on the server, **Then** the corresponding Vanilla `AttributeModifier`s are applied with deterministic `Identifier`s and cleanly removed when the armor is unequipped.
- [x] **Given** any of the 18 Vanilla Trim Patterns (`sentry`, `vex`, `wild`, `dune`, `coast`, `tide`, `flow`, `bolt`, `ward`, `silence`, `wayfinder`, `raiser`, `shaper`, `host`, `snout`, `rib`, `eye`, `spire`) reaches Tier I, II, or III, **When** its corresponding gameplay condition or event occurs, **Then** the scaled effect triggers on the logical server (`!level.isClientSide()`) and plays subtle audiovisual feedback when applicable.
- [x] **Given** a player hovers over a trimmed armor piece or a Smithing Template in their inventory, **When** `ItemTooltipEvent` fires on the client, **Then** the tooltip displays the localized (`pt_br` / `en_us`) Pattern and Material bonus descriptions, current equipped piece count `[X/4]`, and full Tier I/II/III values when holding `[Shift]`.

## Defensive Security & Validation Considerations

### Input Validation
- All armor slot inspections validate non-null `ItemStack`, non-empty stack (`!stack.isEmpty()`), and presence of `DataComponents.TRIM` before dereferencing `Holder<TrimPattern>` or `Holder<TrimMaterial>`.
- Unknown or modded trim pattern/material `Identifier`s that are not registered in `PatternBonusCatalog` or `MaterialBonusCatalog` are safely ignored (`Optional.empty()`) without throwing exceptions.
- All `ModConfigSpec` numeric multipliers use strict bounded ranges (`defineInRange`, e.g., `0.0` to `10.0`) to prevent arithmetic overflow, negative health/damage corruption, or NaN propagation.
- Chain damage (`bolt`) and thorns reflection (`tide`) guard against recursive self-triggering loops by checking damage source flags / recursion guards.

### Secret Isolation
- No secrets, API keys, credentials, or external network calls exist anywhere in the mod.

### Authorization & Authentication Impact
- N/A — Gameplay calculations (damage, healing, attributes, XP, durability) are executed strictly on the authoritative logical server (`!level.isClientSide()` / `ServerLevel`) so clients cannot spoof buffs or attribute modifiers.

### Other Security Risks
- **Server TPS / DoS Prevention:** Tick listeners (`PlayerTickEvent.Post`) are strictly throttled via modulo game time (`level.getGameTime() % 20 == 0` for passive effects; `% 5 == 0` with small bounded AABB `<= 5.0` blocks for `host` magnetism) and never allocate unbounded streams or cache `Entity`/`Level` references in static maps.

## Testing Strategy & Shift-Left Plan

### Happy Path Tests
| Test ID | Scenario | Test Type | Expected Result |
|---------|----------|-----------|-----------------|
| TP-01 | 2, 3, and 4 matching Pattern pieces (`minecraft:silence`) resolve to `TIER_1`, `TIER_2`, `TIER_3` | Unit (`SetBonusCalculatorTest`) | Returns expected `TrimTier` for each piece count |
| TP-02 | Hybrid combination: 4 `silence` pattern pieces with 2 `gold` + 2 `quartz` materials | Unit (`SetBonusCalculatorTest`) | Resolves `silence=TIER_3`, `gold=TIER_1`, `quartz=TIER_1` |
| TP-03 | Two 2-piece pattern sets (`2 dune` + `2 tide`) and two 2-piece material sets (`2 emerald` + `2 amethyst`) | Unit (`SetBonusCalculatorTest`) | Resolves `TIER_1` for all four active sets simultaneously |
| TP-04 | All 11 `MaterialBonusCatalog` entries return valid non-zero scaling values for Tiers I, II, and III | Unit (`MaterialBonusCatalogTest`) | Strictly increasing positive magnitude across Tiers I < II < III |
| TP-05 | All 18 `PatternBonusCatalog` entries return valid scaling values and combat formulas for Tiers I, II, and III | Unit (`PatternBonusCatalogTest`) | Strictly increasing progression across Tiers I < II < III |

### Edge Case & Failure Mode Tests
| Test ID | Scenario | Test Type | Expected Result |
|---------|----------|-----------|-----------------|
| TE-01 | 0 pieces or 1 piece equipped of any Pattern/Material | Unit (`SetBonusCalculatorTest`) | Returns `TrimTier.NONE` (no active set bonus) |
| TE-02 | More than 4 entries or null/empty list passed to `SetBonusCalculator` | Unit (`SetBonusCalculatorTest`) | Handles null/empty safely and clamps/validates up to 4 armor slots |
| TE-03 | Unknown modded trim pattern/material `Identifier` (`modded:custom_trim`) | Unit (`MaterialBonusCatalogTest` / `PatternBonusCatalogTest`) | Safely returns `Optional.empty()` without throwing `NullPointerException` |
| TE-04 | Out-of-bounds tier or negative piece count passed to `TrimTier.fromPieceCount` | Unit (`SetBonusCalculatorTest`) | Safely returns `TrimTier.NONE` for `< 2` and `TrimTier.TIER_3` for `>= 4` |

### Test Commands
- **Run domain unit tests (Maven):** `mvn clean test -f pom-test.xml`
- **Run full Gradle build & tests:** `.\gradlew.bat test`

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Recursive damage loop when two players with `tide` (Thorns reflection) hit each other | Med | High | Use a thread-local reentrancy guard (`IS_PROCESSING_TRIM_DAMAGE`) and ignore `thorns` damage sources in `tide` and `bolt` handlers |
| Attribute modifiers stacking infinitely across equipment swaps or dimension changes | Med | High | Always remove deterministic `Identifier` modifiers (`removeModifier(id)`) before applying updated Tier modifiers in `MaterialAttributeApplier` |
| Dedicated server crash from client-only tooltip/particle classes | Low | High | Isolate `ItemTooltipEvent` in `com.trimfx.client` with `@EventBusSubscriber(value = Dist.CLIENT)` and spawn particles via `ServerLevel#sendParticles` |

## Specification Changes & Delta Stubs

### `specs/armor-trim-effects.md`
- **Target Canonical Spec:** `New Specification` (`openspec/specs/armor-trim-effects.md`)
- **Summary of Updates:** Defines the canonical living specification for TrimFX's independent Pattern & Material set bonus mechanics, the 11 Material attribute mappings, the 18 Pattern thematic abilities, configuration contract, and dynamic tooltip behavior.

## References

- `.agents/rules/AGENTS.md` — Mandatory Minecraft 26.3 & NeoForge 26.3.0.95 Stack
- `.agents/skills/minecraft-neoforge-tech-spec/SKILL.md` — Minecraft 26.3 API conventions
- `.agents/skills/minecraft-clean-code/SKILL.md` — Clean Code & Side-Safety rules
- `.agents/skills/minecraft-design-patterns/SKILL.md` — Strategy & Observer patterns
