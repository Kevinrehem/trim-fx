package com.trimfx.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TrimFxConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_PATTERN_BONUSES;
    public static final ModConfigSpec.BooleanValue ENABLE_MATERIAL_BONUSES;
    public static final ModConfigSpec.BooleanValue ENABLE_PROC_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_TEMPLATE_TOOLTIPS;
    public static final ModConfigSpec.DoubleValue PATTERN_EFFECT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MATERIAL_ATTRIBUTE_MULTIPLIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        ENABLE_PATTERN_BONUSES = builder
                .comment("Enable set bonuses for the 18 Armor Trim Patterns (2, 3, and 4 pieces).")
                .define("enablePatternBonuses", true);

        ENABLE_MATERIAL_BONUSES = builder
                .comment("Enable passive attribute bonuses for the 11 Armor Trim Materials (2, 3, and 4 pieces).")
                .define("enableMaterialBonuses", true);

        ENABLE_PROC_FEEDBACK = builder
                .comment("Play subtle Vanilla particles and sounds when combat Trim effects trigger.")
                .define("enableProcFeedback", true);

        ENABLE_TEMPLATE_TOOLTIPS = builder
                .comment("Display Trim set bonus descriptions on Smithing Template items.")
                .define("enableTemplateTooltips", true);
        builder.pop();

        builder.push("scaling");
        PATTERN_EFFECT_MULTIPLIER = builder
                .comment("Global multiplier applied to numeric Trim Pattern effect magnitudes (0.1 to 5.0).")
                .defineInRange("patternEffectMultiplier", 1.0D, 0.1D, 5.0D);

        MATERIAL_ATTRIBUTE_MULTIPLIER = builder
                .comment("Global multiplier applied to numeric Trim Material attribute bonuses (0.1 to 5.0).")
                .defineInRange("materialAttributeMultiplier", 1.0D, 0.1D, 5.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private TrimFxConfig() {
    }

    public static boolean arePatternBonusesEnabled() {
        return !SPEC.isLoaded() || ENABLE_PATTERN_BONUSES.get();
    }

    public static boolean areMaterialBonusesEnabled() {
        return !SPEC.isLoaded() || ENABLE_MATERIAL_BONUSES.get();
    }

    public static boolean isProcFeedbackEnabled() {
        return !SPEC.isLoaded() || ENABLE_PROC_FEEDBACK.get();
    }

    public static boolean areTemplateTooltipsEnabled() {
        return !SPEC.isLoaded() || ENABLE_TEMPLATE_TOOLTIPS.get();
    }

    public static double getPatternMultiplier() {
        return SPEC.isLoaded() ? PATTERN_EFFECT_MULTIPLIER.get() : 1.0D;
    }

    public static double getMaterialMultiplier() {
        return SPEC.isLoaded() ? MATERIAL_ATTRIBUTE_MULTIPLIER.get() : 1.0D;
    }
}
