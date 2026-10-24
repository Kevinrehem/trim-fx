package com.trimfx.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class PatternBonusCatalog {

    public static final String SENTRY = "minecraft:sentry";
    public static final String VEX = "minecraft:vex";
    public static final String WILD = "minecraft:wild";
    public static final String DUNE = "minecraft:dune";
    public static final String COAST = "minecraft:coast";
    public static final String TIDE = "minecraft:tide";
    public static final String FLOW = "minecraft:flow";
    public static final String BOLT = "minecraft:bolt";

    public static final String WARD = "minecraft:ward";
    public static final String SILENCE = "minecraft:silence";
    public static final String WAYFINDER = "minecraft:wayfinder";
    public static final String RAISER = "minecraft:raiser";
    public static final String SHAPER = "minecraft:shaper";
    public static final String HOST = "minecraft:host";

    public static final String SNOUT = "minecraft:snout";
    public static final String RIB = "minecraft:rib";
    public static final String EYE = "minecraft:eye";
    public static final String SPIRE = "minecraft:spire";

    public static final int MAX_DARKNESS_LIGHT_LEVEL = 7;
    public static final float DUNE_LOW_HEALTH_RATIO = 0.40F;
    public static final int DUNE_COOLDOWN_TICKS = 600; // 30 seconds
    public static final int DUNE_ABSORPTION_DURATION_TICKS = 200; // 10 seconds
    public static final float RIB_KILL_HEAL_AMOUNT = 2.0F;
    public static final double SNOUT_TIER3_DOUBLE_BARTER_CHANCE = 0.25D;
    public static final double WAYFINDER_STEP_HEIGHT_BONUS = 0.5D;

    public record PatternBonusSpec(
            String patternId,
            String translationKeySuffix,
            MaterialBonusCatalog.TierValues primaryValues,
            MaterialBonusCatalog.TierValues secondaryValues
    ) {
        public double getPrimaryValue(TrimTier tier, double multiplier) {
            return primaryValues.forTier(tier) * MaterialBonusCatalog.clampMultiplier(multiplier);
        }

        public double getSecondaryValue(TrimTier tier, double multiplier) {
            return secondaryValues.forTier(tier) * MaterialBonusCatalog.clampMultiplier(multiplier);
        }
    }

    private static final Map<String, PatternBonusSpec> CATALOG;

    static {
        Map<String, PatternBonusSpec> map = new LinkedHashMap<>();

        // Group 1: Overworld, Ocean & Trial Chambers
        register(map, SENTRY, "sentry", new MaterialBonusCatalog.TierValues(0.10D, 0.20D, 0.35D), new MaterialBonusCatalog.TierValues(0.0D, 0.0D, 100.0D));
        register(map, VEX, "vex", new MaterialBonusCatalog.TierValues(0.08D, 0.15D, 0.25D), new MaterialBonusCatalog.TierValues(0.0D, 0.0D, 0.0D));
        register(map, WILD, "wild", new MaterialBonusCatalog.TierValues(0.50D, 0.75D, 1.00D), new MaterialBonusCatalog.TierValues(60.0D, 100.0D, 160.0D));
        register(map, DUNE, "dune", new MaterialBonusCatalog.TierValues(0.15D, 0.25D, 0.40D), new MaterialBonusCatalog.TierValues(1.0D, 2.0D, 3.0D));
        register(map, COAST, "coast", new MaterialBonusCatalog.TierValues(0.25D, 0.50D, 1.00D), new MaterialBonusCatalog.TierValues(0.0D, 0.0D, 1.0D));
        register(map, TIDE, "tide", new MaterialBonusCatalog.TierValues(0.15D, 0.25D, 0.40D), new MaterialBonusCatalog.TierValues(0.0D, 0.0D, 1.0D));
        register(map, FLOW, "flow", new MaterialBonusCatalog.TierValues(0.40D, 0.70D, 1.00D), new MaterialBonusCatalog.TierValues(0.0D, 1.0D, 2.0D));
        register(map, BOLT, "bolt", new MaterialBonusCatalog.TierValues(0.10D, 0.18D, 0.30D), new MaterialBonusCatalog.TierValues(2.0D, 4.0D, 6.0D));

        // Group 2: Deep Dark & Trail Ruins
        register(map, WARD, "ward", new MaterialBonusCatalog.TierValues(0.10D, 0.18D, 0.28D), new MaterialBonusCatalog.TierValues(0.0D, 0.0D, 1.0D));
        register(map, SILENCE, "silence", new MaterialBonusCatalog.TierValues(0.25D, 0.50D, 0.80D), new MaterialBonusCatalog.TierValues(0.20D, 0.35D, 0.50D));
        register(map, WAYFINDER, "wayfinder", new MaterialBonusCatalog.TierValues(0.25D, 0.50D, 0.75D), new MaterialBonusCatalog.TierValues(0.5D, 0.5D, 0.5D));
        register(map, RAISER, "raiser", new MaterialBonusCatalog.TierValues(0.15D, 0.30D, 0.50D), new MaterialBonusCatalog.TierValues(1.0D, 2.0D, 3.0D));
        register(map, SHAPER, "shaper", new MaterialBonusCatalog.TierValues(0.15D, 0.30D, 0.45D), new MaterialBonusCatalog.TierValues(0.0D, 0.0D, 0.0D));
        register(map, HOST, "host", new MaterialBonusCatalog.TierValues(0.20D, 0.40D, 0.70D), new MaterialBonusCatalog.TierValues(2.0D, 3.5D, 5.0D));

        // Group 3: Nether & End
        register(map, SNOUT, "snout", new MaterialBonusCatalog.TierValues(0.12D, 0.22D, 0.35D), new MaterialBonusCatalog.TierValues(0.0D, 0.0D, SNOUT_TIER3_DOUBLE_BARTER_CHANCE));
        register(map, RIB, "rib", new MaterialBonusCatalog.TierValues(0.50D, 0.80D, 1.00D), new MaterialBonusCatalog.TierValues(60.0D, 100.0D, 120.0D));
        register(map, EYE, "eye", new MaterialBonusCatalog.TierValues(0.25D, 0.50D, 0.75D), new MaterialBonusCatalog.TierValues(0.10D, 0.20D, 0.35D));
        register(map, SPIRE, "spire", new MaterialBonusCatalog.TierValues(0.12D, 0.20D, 0.30D), new MaterialBonusCatalog.TierValues(30.0D, 40.0D, 50.0D));

        CATALOG = Collections.unmodifiableMap(map);
    }

    private PatternBonusCatalog() {
    }

    private static void register(
            Map<String, PatternBonusSpec> map,
            String id,
            String suffix,
            MaterialBonusCatalog.TierValues primary,
            MaterialBonusCatalog.TierValues secondary
    ) {
        map.put(id, new PatternBonusSpec(id, suffix, primary, secondary));
    }

    public static Optional<PatternBonusSpec> find(String patternId) {
        String normalized = ArmorSlotTrim.normalizeId(patternId);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(CATALOG.get(normalized));
    }

    public static Map<String, PatternBonusSpec> all() {
        return CATALOG;
    }

    public static float applyDamageReduction(float incomingDamage, TrimTier tier, String patternId, double multiplier) {
        if (incomingDamage <= 0.0F || tier == null || !tier.isActive()) {
            return Math.max(0.0F, incomingDamage);
        }
        Optional<PatternBonusSpec> spec = find(patternId);
        if (spec.isEmpty()) {
            return incomingDamage;
        }
        double reductionRatio = Math.min(1.0D, Math.max(0.0D, spec.get().getPrimaryValue(tier, multiplier)));
        return (float) (incomingDamage * (1.0D - reductionRatio));
    }

    public static float applyDamageAmplification(float baseDamage, TrimTier tier, String patternId, double multiplier) {
        if (baseDamage <= 0.0F || tier == null || !tier.isActive()) {
            return Math.max(0.0F, baseDamage);
        }
        Optional<PatternBonusSpec> spec = find(patternId);
        if (spec.isEmpty()) {
            return baseDamage;
        }
        double bonusRatio = Math.max(0.0D, spec.get().getPrimaryValue(tier, multiplier));
        return (float) (baseDamage * (1.0D + bonusRatio));
    }
}
