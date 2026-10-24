package com.trimfx.domain;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternBonusCatalogTest {

    @Test
    void tp05_allEighteenVanillaPatternsExistAndScaleStrictlyAcrossTiers() {
        assertEquals(18, PatternBonusCatalog.all().size());

        for (PatternBonusCatalog.PatternBonusSpec spec : PatternBonusCatalog.all().values()) {
            double t1 = spec.getPrimaryValue(TrimTier.TIER_1, 1.0D);
            double t2 = spec.getPrimaryValue(TrimTier.TIER_2, 1.0D);
            double t3 = spec.getPrimaryValue(TrimTier.TIER_3, 1.0D);

            assertTrue(t1 > 0.0D, "Tier 1 primary value must be > 0 for " + spec.patternId());
            assertTrue(t2 > t1, "Tier 2 must exceed Tier 1 for " + spec.patternId());
            assertTrue(t3 > t2, "Tier 3 must exceed Tier 2 for " + spec.patternId());
            assertEquals(0.0D, spec.getPrimaryValue(TrimTier.NONE, 1.0D));
        }
    }

    @Test
    void damageReductionAndAmplificationFormulasComputeAccurately() {
        // Flow Tier 3 = 100% fall damage reduction -> 0.0 damage
        assertEquals(0.0F, PatternBonusCatalog.applyDamageReduction(20.0F, TrimTier.TIER_3, "minecraft:flow", 1.0D), 1e-4F);
        // Dune Tier 2 = 25% fire/explosion reduction -> 15.0 damage from 20.0
        assertEquals(15.0F, PatternBonusCatalog.applyDamageReduction(20.0F, TrimTier.TIER_2, "minecraft:dune", 1.0D), 1e-4F);
        // Sentry Tier 3 = +35% ranged damage -> 13.5 damage from 10.0
        assertEquals(13.5F, PatternBonusCatalog.applyDamageAmplification(10.0F, TrimTier.TIER_3, "minecraft:sentry", 1.0D), 1e-4F);
    }

    @Test
    void te03_unknownPatternIdsAndInvalidDamageInputsReturnSafeDefaults() {
        assertEquals(Optional.empty(), PatternBonusCatalog.find(null));
        assertEquals(Optional.empty(), PatternBonusCatalog.find("modded:dragon_trim"));
        assertEquals(10.0F, PatternBonusCatalog.applyDamageReduction(10.0F, TrimTier.NONE, "minecraft:flow", 1.0D));
        assertEquals(0.0F, PatternBonusCatalog.applyDamageReduction(-5.0F, TrimTier.TIER_3, "minecraft:flow", 1.0D));
        assertEquals(10.0F, PatternBonusCatalog.applyDamageAmplification(10.0F, TrimTier.TIER_2, "modded:unknown", 1.0D));
    }
}
