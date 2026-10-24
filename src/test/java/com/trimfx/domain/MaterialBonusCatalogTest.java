package com.trimfx.domain;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialBonusCatalogTest {

    @Test
    void tp04_allTwelveMaterialsExistAndScaleStrictlyAcrossTiers() {
        assertEquals(12, MaterialBonusCatalog.all().size());

        for (MaterialBonusCatalog.MaterialBonusSpec spec : MaterialBonusCatalog.all().values()) {
            double t1 = Math.abs(spec.getPrimaryBonus(TrimTier.TIER_1, 1.0D));
            double t2 = Math.abs(spec.getPrimaryBonus(TrimTier.TIER_2, 1.0D));
            double t3 = Math.abs(spec.getPrimaryBonus(TrimTier.TIER_3, 1.0D));

            assertTrue(t1 > 0.0D, "Tier 1 primary value must be > 0 for " + spec.materialId());
            assertTrue(t2 > t1, "Tier 2 must exceed Tier 1 for " + spec.materialId());
            assertTrue(t3 > t2, "Tier 3 must exceed Tier 2 for " + spec.materialId());
            assertEquals(0.0D, spec.getPrimaryBonus(TrimTier.NONE, 1.0D));
        }

        // Verify Lapis has both Oxygen and Water Movement Efficiency
        MaterialBonusCatalog.MaterialBonusSpec lapis = MaterialBonusCatalog.find("lapis").orElseThrow();
        assertEquals(3.0D, lapis.getPrimaryBonus(TrimTier.TIER_3, 1.0D));
        assertEquals(0.80D, lapis.getSecondaryBonus(TrimTier.TIER_3, 1.0D), 1e-6);

        // Verify Biomes O' Plenty Rose Quartz resolves from both biomesoplenty:rose_quartz and rose_quartz
        MaterialBonusCatalog.MaterialBonusSpec roseQuartz = MaterialBonusCatalog.find("biomesoplenty:rose_quartz").orElseThrow();
        assertEquals(roseQuartz, MaterialBonusCatalog.find("rose_quartz").orElseThrow());
        assertEquals(6.0D, roseQuartz.getPrimaryBonus(TrimTier.TIER_3, 1.0D), 1e-6);
        assertEquals(0.25D, roseQuartz.getSecondaryBonus(TrimTier.TIER_3, 1.0D), 1e-6);
    }

    @Test
    void te03_unknownMaterialIdsAndExtremeMultipliersAreHandledSafely() {
        assertEquals(Optional.empty(), MaterialBonusCatalog.find(null));
        assertEquals(Optional.empty(), MaterialBonusCatalog.find(""));
        assertEquals(Optional.empty(), MaterialBonusCatalog.find("modded:vibranium"));

        assertEquals(0.1D, MaterialBonusCatalog.clampMultiplier(-10.0D));
        assertEquals(5.0D, MaterialBonusCatalog.clampMultiplier(999.0D));
        assertEquals(1.0D, MaterialBonusCatalog.clampMultiplier(Double.NaN));
    }
}
