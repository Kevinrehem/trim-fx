package com.trimfx.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class MaterialBonusCatalog {

    public static final String IRON = "minecraft:iron";
    public static final String COPPER = "minecraft:copper";
    public static final String GOLD = "minecraft:gold";
    public static final String DIAMOND = "minecraft:diamond";
    public static final String NETHERITE = "minecraft:netherite";
    public static final String REDSTONE = "minecraft:redstone";
    public static final String LAPIS = "minecraft:lapis";
    public static final String EMERALD = "minecraft:emerald";
    public static final String QUARTZ = "minecraft:quartz";
    public static final String AMETHYST = "minecraft:amethyst";
    public static final String RESIN = "minecraft:resin";
    public static final String ROSE_QUARTZ = ArmorSlotTrim.BOP_ROSE_QUARTZ_ID;

    public record TierValues(double tier1, double tier2, double tier3) {
        public double forTier(TrimTier tier) {
            if (tier == null) {
                return 0.0D;
            }
            return switch (tier) {
                case TIER_1 -> tier1;
                case TIER_2 -> tier2;
                case TIER_3 -> tier3;
                case NONE -> 0.0D;
            };
        }
    }

    public record MaterialBonusSpec(
            String materialId,
            String translationKeySuffix,
            TierValues primaryValues,
            TierValues secondaryValues,
            boolean percentageDisplay
    ) {
        public double getPrimaryBonus(TrimTier tier, double multiplier) {
            return primaryValues.forTier(tier) * clampMultiplier(multiplier);
        }

        public double getSecondaryBonus(TrimTier tier, double multiplier) {
            return secondaryValues.forTier(tier) * clampMultiplier(multiplier);
        }
    }

    private static final Map<String, MaterialBonusSpec> CATALOG;

    static {
        Map<String, MaterialBonusSpec> map = new LinkedHashMap<>();
        register(map, IRON, "iron", new TierValues(1.0D, 2.0D, 3.0D), new TierValues(0.0D, 0.0D, 0.0D), false);
        register(map, COPPER, "copper", new TierValues(0.04D, 0.08D, 0.12D), new TierValues(0.0D, 0.0D, 0.0D), true);
        register(map, GOLD, "gold", new TierValues(0.05D, 0.10D, 0.15D), new TierValues(0.0D, 0.0D, 0.0D), true);
        register(map, DIAMOND, "diamond", new TierValues(1.0D, 2.0D, 3.5D), new TierValues(0.0D, 0.0D, 0.0D), false);
        register(map, NETHERITE, "netherite", new TierValues(0.10D, 0.20D, 0.35D), new TierValues(-0.15D, -0.30D, -0.50D), true);
        register(map, REDSTONE, "redstone", new TierValues(0.08D, 0.16D, 0.25D), new TierValues(0.0D, 0.0D, 0.0D), true);
        register(map, LAPIS, "lapis", new TierValues(1.0D, 2.0D, 3.0D), new TierValues(0.25D, 0.50D, 0.80D), false);
        register(map, EMERALD, "emerald", new TierValues(1.0D, 2.0D, 3.0D), new TierValues(0.0D, 0.0D, 0.0D), false);
        register(map, QUARTZ, "quartz", new TierValues(1.0D, 2.0D, 3.0D), new TierValues(0.0D, 0.0D, 0.0D), false);
        register(map, AMETHYST, "amethyst", new TierValues(0.5D, 1.0D, 1.5D), new TierValues(0.5D, 1.0D, 1.5D), false);
        register(map, RESIN, "resin", new TierValues(2.0D, 4.0D, 6.0D), new TierValues(0.0D, 0.0D, 0.0D), false);
        register(map, ROSE_QUARTZ, "rose_quartz", new TierValues(2.0D, 4.0D, 6.0D), new TierValues(0.08D, 0.15D, 0.25D), false);
        CATALOG = Collections.unmodifiableMap(map);
    }

    private MaterialBonusCatalog() {
    }

    private static void register(
            Map<String, MaterialBonusSpec> map,
            String id,
            String suffix,
            TierValues primary,
            TierValues secondary,
            boolean percentage
    ) {
        map.put(id, new MaterialBonusSpec(id, suffix, primary, secondary, percentage));
    }

    public static Optional<MaterialBonusSpec> find(String materialId) {
        String normalized = ArmorSlotTrim.normalizeId(materialId);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(CATALOG.get(normalized));
    }

    public static Map<String, MaterialBonusSpec> all() {
        return CATALOG;
    }

    public static double clampMultiplier(double multiplier) {
        if (Double.isNaN(multiplier) || Double.isInfinite(multiplier)) {
            return 1.0D;
        }
        return Math.max(0.1D, Math.min(5.0D, multiplier));
    }
}
