package com.trimfx.service;

import com.trimfx.TrimFxMod;
import com.trimfx.config.TrimFxConfig;
import com.trimfx.domain.MaterialBonusCatalog;
import com.trimfx.domain.PatternBonusCatalog;
import com.trimfx.domain.TrimSetSummary;
import com.trimfx.domain.TrimTier;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class MaterialAttributeApplier {

    private record ManagedModifier(Holder<Attribute> attribute, Identifier modifierId) {
    }

    private static final Identifier IRON_ARMOR_ID = id("material_iron_armor");
    private static final Identifier COPPER_SPEED_ID = id("material_copper_speed");
    private static final Identifier GOLD_ATK_SPEED_ID = id("material_gold_attack_speed");
    private static final Identifier DIAMOND_TOUGHNESS_ID = id("material_diamond_toughness");
    private static final Identifier NETHERITE_KB_ID = id("material_netherite_knockback_res");
    private static final Identifier NETHERITE_BURN_ID = id("material_netherite_burn_time");
    private static final Identifier REDSTONE_MINE_ID = id("material_redstone_break_speed");
    private static final Identifier LAPIS_OXYGEN_ID = id("material_lapis_oxygen");
    private static final Identifier LAPIS_WATER_MOVE_ID = id("material_lapis_water_move");
    private static final Identifier EMERALD_LUCK_ID = id("material_emerald_luck");
    private static final Identifier QUARTZ_DAMAGE_ID = id("material_quartz_damage");
    private static final Identifier AMETHYST_BLOCK_RANGE_ID = id("material_amethyst_block_range");
    private static final Identifier AMETHYST_ENTITY_RANGE_ID = id("material_amethyst_entity_range");
    private static final Identifier RESIN_HEALTH_ID = id("material_resin_max_health");
    private static final Identifier ROSE_QUARTZ_ABSORPTION_ID = id("material_rose_quartz_max_absorption");

    private static final Identifier PATTERN_SILENCE_SNEAK_ID = id("pattern_silence_sneak_speed");
    private static final Identifier PATTERN_WAYFINDER_STEP_ID = id("pattern_wayfinder_step_height");
    private static final Identifier PATTERN_COAST_SUBMERGED_ID = id("pattern_coast_submerged_mining");

    private static final List<ManagedModifier> ALL_MANAGED_MODIFIERS = List.of(
            new ManagedModifier(Attributes.ARMOR, IRON_ARMOR_ID),
            new ManagedModifier(Attributes.MOVEMENT_SPEED, COPPER_SPEED_ID),
            new ManagedModifier(Attributes.ATTACK_SPEED, GOLD_ATK_SPEED_ID),
            new ManagedModifier(Attributes.ARMOR_TOUGHNESS, DIAMOND_TOUGHNESS_ID),
            new ManagedModifier(Attributes.KNOCKBACK_RESISTANCE, NETHERITE_KB_ID),
            new ManagedModifier(Attributes.BURNING_TIME, NETHERITE_BURN_ID),
            new ManagedModifier(Attributes.BLOCK_BREAK_SPEED, REDSTONE_MINE_ID),
            new ManagedModifier(Attributes.OXYGEN_BONUS, LAPIS_OXYGEN_ID),
            new ManagedModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, LAPIS_WATER_MOVE_ID),
            new ManagedModifier(Attributes.LUCK, EMERALD_LUCK_ID),
            new ManagedModifier(Attributes.ATTACK_DAMAGE, QUARTZ_DAMAGE_ID),
            new ManagedModifier(Attributes.BLOCK_INTERACTION_RANGE, AMETHYST_BLOCK_RANGE_ID),
            new ManagedModifier(Attributes.ENTITY_INTERACTION_RANGE, AMETHYST_ENTITY_RANGE_ID),
            new ManagedModifier(Attributes.MAX_HEALTH, RESIN_HEALTH_ID),
            new ManagedModifier(Attributes.MAX_ABSORPTION, ROSE_QUARTZ_ABSORPTION_ID),
            new ManagedModifier(Attributes.SNEAKING_SPEED, PATTERN_SILENCE_SNEAK_ID),
            new ManagedModifier(Attributes.STEP_HEIGHT, PATTERN_WAYFINDER_STEP_ID),
            new ManagedModifier(Attributes.SUBMERGED_MINING_SPEED, PATTERN_COAST_SUBMERGED_ID)
    );

    private MaterialAttributeApplier() {
    }

    public static void refreshEntityAttributes(LivingEntity entity, TrimSetSummary summary) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        clearAllManagedModifiers(entity);

        if (summary == null) {
            return;
        }
        if (TrimFxConfig.areMaterialBonusesEnabled()) {
            applyMaterialModifiers(entity, summary);
        }
        if (TrimFxConfig.arePatternBonusesEnabled()) {
            applyPatternAttributeModifiers(entity, summary);
        }
        clampEntityHealthIfNeeded(entity);
    }

    private static void clearAllManagedModifiers(LivingEntity entity) {
        for (ManagedModifier managed : ALL_MANAGED_MODIFIERS) {
            AttributeInstance instance = entity.getAttribute(managed.attribute());
            if (instance != null) {
                instance.removeModifier(managed.modifierId());
            }
        }
    }

    private static void applyMaterialModifiers(LivingEntity entity, TrimSetSummary summary) {
        double mult = TrimFxConfig.getMaterialMultiplier();
        for (Map.Entry<String, TrimTier> entry : summary.activeMaterials().entrySet()) {
            String materialId = entry.getKey();
            TrimTier tier = entry.getValue();
            MaterialBonusCatalog.find(materialId).ifPresent(spec ->
                    applySingleMaterialSpec(entity, spec, tier, mult)
            );
        }
    }

    private static void applySingleMaterialSpec(
            LivingEntity entity,
            MaterialBonusCatalog.MaterialBonusSpec spec,
            TrimTier tier,
            double mult
    ) {
        double primary = spec.getPrimaryBonus(tier, mult);
        double secondary = spec.getSecondaryBonus(tier, mult);

        switch (spec.materialId()) {
            case MaterialBonusCatalog.IRON ->
                    addModifier(entity, Attributes.ARMOR, IRON_ARMOR_ID, primary, AttributeModifier.Operation.ADD_VALUE);
            case MaterialBonusCatalog.COPPER ->
                    addModifier(entity, Attributes.MOVEMENT_SPEED, COPPER_SPEED_ID, primary, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case MaterialBonusCatalog.GOLD ->
                    addModifier(entity, Attributes.ATTACK_SPEED, GOLD_ATK_SPEED_ID, primary, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case MaterialBonusCatalog.DIAMOND ->
                    addModifier(entity, Attributes.ARMOR_TOUGHNESS, DIAMOND_TOUGHNESS_ID, primary, AttributeModifier.Operation.ADD_VALUE);
            case MaterialBonusCatalog.NETHERITE -> {
                addModifier(entity, Attributes.KNOCKBACK_RESISTANCE, NETHERITE_KB_ID, primary, AttributeModifier.Operation.ADD_VALUE);
                addModifier(entity, Attributes.BURNING_TIME, NETHERITE_BURN_ID, secondary, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
            case MaterialBonusCatalog.REDSTONE ->
                    addModifier(entity, Attributes.BLOCK_BREAK_SPEED, REDSTONE_MINE_ID, primary, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case MaterialBonusCatalog.LAPIS -> {
                addModifier(entity, Attributes.OXYGEN_BONUS, LAPIS_OXYGEN_ID, primary, AttributeModifier.Operation.ADD_VALUE);
                addModifier(entity, Attributes.WATER_MOVEMENT_EFFICIENCY, LAPIS_WATER_MOVE_ID, secondary, AttributeModifier.Operation.ADD_VALUE);
            }
            case MaterialBonusCatalog.EMERALD ->
                    addModifier(entity, Attributes.LUCK, EMERALD_LUCK_ID, primary, AttributeModifier.Operation.ADD_VALUE);
            case MaterialBonusCatalog.QUARTZ ->
                    addModifier(entity, Attributes.ATTACK_DAMAGE, QUARTZ_DAMAGE_ID, primary, AttributeModifier.Operation.ADD_VALUE);
            case MaterialBonusCatalog.AMETHYST -> {
                addModifier(entity, Attributes.BLOCK_INTERACTION_RANGE, AMETHYST_BLOCK_RANGE_ID, primary, AttributeModifier.Operation.ADD_VALUE);
                addModifier(entity, Attributes.ENTITY_INTERACTION_RANGE, AMETHYST_ENTITY_RANGE_ID, secondary, AttributeModifier.Operation.ADD_VALUE);
            }
            case MaterialBonusCatalog.RESIN ->
                    addModifier(entity, Attributes.MAX_HEALTH, RESIN_HEALTH_ID, primary, AttributeModifier.Operation.ADD_VALUE);
            case MaterialBonusCatalog.ROSE_QUARTZ ->
                    addModifier(entity, Attributes.MAX_ABSORPTION, ROSE_QUARTZ_ABSORPTION_ID, primary, AttributeModifier.Operation.ADD_VALUE);
            default -> {
            }
        }
    }

    private static void applyPatternAttributeModifiers(LivingEntity entity, TrimSetSummary summary) {
        double mult = TrimFxConfig.getPatternMultiplier();

        TrimTier silenceTier = summary.getPatternTier(PatternBonusCatalog.SILENCE);
        if (silenceTier.isActive()) {
            PatternBonusCatalog.find(PatternBonusCatalog.SILENCE).ifPresent(spec ->
                    addModifier(entity, Attributes.SNEAKING_SPEED, PATTERN_SILENCE_SNEAK_ID,
                            spec.getPrimaryValue(silenceTier, mult), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
            );
        }

        TrimTier wayfinderTier = summary.getPatternTier(PatternBonusCatalog.WAYFINDER);
        if (wayfinderTier.isActive()) {
            addModifier(entity, Attributes.STEP_HEIGHT, PATTERN_WAYFINDER_STEP_ID,
                    PatternBonusCatalog.WAYFINDER_STEP_HEIGHT_BONUS, AttributeModifier.Operation.ADD_VALUE);
        }

        TrimTier coastTier = summary.getPatternTier(PatternBonusCatalog.COAST);
        if (coastTier.isActive()) {
            PatternBonusCatalog.find(PatternBonusCatalog.COAST).ifPresent(spec ->
                    addModifier(entity, Attributes.SUBMERGED_MINING_SPEED, PATTERN_COAST_SUBMERGED_ID,
                            spec.getPrimaryValue(coastTier, mult), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
            );
        }
    }

    private static void addModifier(
            LivingEntity entity,
            Holder<Attribute> attribute,
            Identifier id,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (amount == 0.0D) {
            return;
        }
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static void clampEntityHealthIfNeeded(LivingEntity entity) {
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
        if (entity.getAbsorptionAmount() > entity.getMaxAbsorption()) {
            entity.setAbsorptionAmount(entity.getMaxAbsorption());
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TrimFxMod.MOD_ID, path);
    }
}
