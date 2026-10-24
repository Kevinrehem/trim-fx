---
id: "armor-trim-effects"
title: "Armor Trim Set & Material Bonuses — Living Specification"
version: "1.0.0"
status: "draft"
owners:
  - "@jaca"
scope:
  paths:
    - "src/main/java/com/trimfx/**"
    - "src/main/resources/**"
    - "src/test/java/com/trimfx/**"
resources: []
---

# Armor Trim Set & Material Bonuses — Living Specification

> This is the authoritative source of truth for this module's current functional state.
> Update this file only after merging a completed change from `openspec/changes/`.

---

## Context & Objectives

**TrimFX** (`trimfx`) brings functional progression and build crafting to Armor Trims in Minecraft `26.3` (NeoForge `26.3.0.95`). Every trimmed armor piece worn in the 4 humanoid armor slots (`HEAD`, `CHEST`, `LEGS`, `FEET`) contributes independently to a **Pattern Set Count** (`TrimPattern`) and a **Material Set Count** (`TrimMaterial`). Wearing 2, 3, or 4 pieces with the same Pattern or Material unlocks **Tier I (2 pieces)**, **Tier II (3 pieces)**, or **Tier III (4 pieces)** respectively.

## Functional Requirements

- **[RF-01] Independent Set Evaluation:** Pattern piece counts and Material piece counts MUST be evaluated independently across the 4 armor slots (`2 pieces = TIER_1`, `3 pieces = TIER_2`, `4 pieces = TIER_3`, `< 2 pieces = NONE`).
- **[RF-02] 12 Material Bonuses (11 Vanilla + Biomes O' Plenty Rose Quartz):** All 11 Vanilla Trim Materials (`iron`, `copper`, `gold`, `diamond`, `netherite`, `redstone`, `lapis`, `emerald`, `quartz`, `amethyst`, `resin`) and Biomes O' Plenty's `biomesoplenty:rose_quartz` (`MAX_ABSORPTION` `+2/+4/+6` & Melee Lifesteal `8%/15%/25%` with overheal absorption) MUST grant their designated `AttributeModifier`s and effects scaled by active Tier (`TIER_1`, `TIER_2`, `TIER_3`) and cleanly removed when unequipped.
- **[RF-03] 18 Pattern Thematic Set Bonuses:** All 18 Vanilla Trim Patterns (`sentry`, `vex`, `wild`, `dune`, `coast`, `tide`, `flow`, `bolt`, `ward`, `silence`, `wayfinder`, `raiser`, `shaper`, `host`, `snout`, `rib`, `eye`, `spire`) MUST grant their designated thematic passive/conditional effects and combat event triggers scaled by active Tier (`TIER_1`, `TIER_2`, `TIER_3`).
- **[RF-04] Server Configuration (`ModConfigSpec`):** A common config (`trimfx-common.toml`) MUST allow enabling/disabling individual Pattern and Material effects, adjusting global scaling multipliers, and toggling audiovisual proc feedback.
- **[RF-05] Dynamic Tooltips & Localization:** Trimmed armor pieces and Smithing Templates MUST display localized (`pt_br` and `en_us`) tooltips indicating the Pattern and Material effects, active piece count `[X/4]`, and full Tier I/II/III values when holding `[Shift]`.

## Non-Functional Requirements

- **[RNF-01] Tick Performance & Side Safety:** Periodic player tick evaluations MUST be throttled (`% 20` ticks for passives, `% 5` ticks for `host`/`spire`) with zero static `Entity`/`Level` retention and strict logical server guards (`!level.isClientSide()`).
- **[RNF-02] Reentrancy Safety:** Combat damage handlers (`tide` reflection, `bolt` chain shock) MUST prevent recursive damage loops via reentrancy guards.
- **[RNF-03] Minecraft 26.3 API Compliance:** Must exclusively use `net.minecraft.resources.Identifier`, `DataComponents.TRIM`, and NeoForge `26.3.0.95` event signatures.

## Out of Scope

- Non-vanilla custom Trim patterns or materials.
- Persistent on-screen HUD overlays outside item tooltips.

## Revision History

| Version | Date | Author | Summary |
|---------|------|--------|---------|
| 1.0.0 | 2026-10-23 | @jaca | Initial specification for `bootstrap-trimfx-mod` |
