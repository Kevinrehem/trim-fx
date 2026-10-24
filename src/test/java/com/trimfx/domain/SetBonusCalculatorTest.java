package com.trimfx.domain;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetBonusCalculatorTest {

    @Test
    void tp01_twoThreeAndFourMatchingPiecesResolveToTiersOneTwoThree() {
        TrimSetSummary twoPieces = SetBonusCalculator.calculate(List.of(
                new ArmorSlotTrim("minecraft:silence", "minecraft:amethyst"),
                new ArmorSlotTrim("minecraft:silence", "minecraft:amethyst")
        ));
        assertEquals(TrimTier.TIER_1, twoPieces.getPatternTier("minecraft:silence"));
        assertEquals(TrimTier.TIER_1, twoPieces.getMaterialTier("minecraft:amethyst"));
        assertEquals(2, twoPieces.getPatternCount("silence"));
        assertEquals(2, twoPieces.getMaterialCount("amethyst"));

        TrimSetSummary threePieces = SetBonusCalculator.calculate(List.of(
                new ArmorSlotTrim("silence", "amethyst"),
                new ArmorSlotTrim("silence", "amethyst"),
                new ArmorSlotTrim("silence", "amethyst")
        ));
        assertEquals(TrimTier.TIER_2, threePieces.getPatternTier("minecraft:silence"));
        assertEquals(TrimTier.TIER_2, threePieces.getMaterialTier("minecraft:amethyst"));

        TrimSetSummary fourPieces = SetBonusCalculator.calculate(List.of(
                new ArmorSlotTrim("silence", "amethyst"),
                new ArmorSlotTrim("silence", "amethyst"),
                new ArmorSlotTrim("silence", "amethyst"),
                new ArmorSlotTrim("silence", "amethyst")
        ));
        assertEquals(TrimTier.TIER_3, fourPieces.getPatternTier("minecraft:silence"));
        assertEquals(TrimTier.TIER_3, fourPieces.getMaterialTier("minecraft:amethyst"));
    }

    @Test
    void tp02_hybridArmorSetEvaluatesPatternsAndMaterialsIndependently() {
        TrimSetSummary summary = SetBonusCalculator.calculate(List.of(
                new ArmorSlotTrim("minecraft:silence", "minecraft:gold"),
                new ArmorSlotTrim("minecraft:silence", "minecraft:gold"),
                new ArmorSlotTrim("minecraft:silence", "minecraft:quartz"),
                new ArmorSlotTrim("minecraft:silence", "minecraft:quartz")
        ));

        assertEquals(TrimTier.TIER_3, summary.getPatternTier("minecraft:silence"));
        assertEquals(TrimTier.TIER_1, summary.getMaterialTier("minecraft:gold"));
        assertEquals(TrimTier.TIER_1, summary.getMaterialTier("minecraft:quartz"));
        assertEquals(1, summary.activePatterns().size());
        assertEquals(2, summary.activeMaterials().size());
    }

    @Test
    void tp03_dualTwoPiecePatternAndMaterialSetsActivateSimultaneously() {
        TrimSetSummary summary = SetBonusCalculator.calculate(List.of(
                new ArmorSlotTrim("dune", "emerald"),
                new ArmorSlotTrim("dune", "emerald"),
                new ArmorSlotTrim("tide", "amethyst"),
                new ArmorSlotTrim("tide", "amethyst")
        ));

        assertEquals(TrimTier.TIER_1, summary.getPatternTier("minecraft:dune"));
        assertEquals(TrimTier.TIER_1, summary.getPatternTier("minecraft:tide"));
        assertEquals(TrimTier.TIER_1, summary.getMaterialTier("minecraft:emerald"));
        assertEquals(TrimTier.TIER_1, summary.getMaterialTier("minecraft:amethyst"));
    }

    @Test
    void te01_zeroOrSinglePieceDoesNotActivateSetBonus() {
        TrimSetSummary emptySummary = SetBonusCalculator.calculate(List.of());
        assertTrue(emptySummary.activePatterns().isEmpty());
        assertTrue(emptySummary.activeMaterials().isEmpty());

        TrimSetSummary singlePiece = SetBonusCalculator.calculate(List.of(
                new ArmorSlotTrim("minecraft:dune", "minecraft:diamond"),
                new ArmorSlotTrim("minecraft:sentry", "minecraft:iron"),
                new ArmorSlotTrim("minecraft:vex", "minecraft:copper"),
                new ArmorSlotTrim("minecraft:wild", "minecraft:lapis")
        ));
        assertTrue(singlePiece.activePatterns().isEmpty());
        assertTrue(singlePiece.activeMaterials().isEmpty());
        assertEquals(1, singlePiece.getPatternCount("minecraft:dune"));
        assertEquals(TrimTier.NONE, singlePiece.getPatternTier("minecraft:dune"));
    }

    @Test
    void te02_nullEmptyAndOversizedInputsAreHandledSafely() {
        assertEquals(TrimSetSummary.EMPTY, SetBonusCalculator.calculate(null));

        List<ArmorSlotTrim> listWithNullsAndExtra = new ArrayList<>();
        listWithNullsAndExtra.add(null);
        listWithNullsAndExtra.add(new ArmorSlotTrim("rib", "netherite"));
        listWithNullsAndExtra.add(new ArmorSlotTrim("rib", "netherite"));
        listWithNullsAndExtra.add(new ArmorSlotTrim("rib", "netherite"));
        listWithNullsAndExtra.add(new ArmorSlotTrim("rib", "netherite"));
        listWithNullsAndExtra.add(new ArmorSlotTrim("rib", "netherite")); // 5th non-null slot should be ignored

        TrimSetSummary summary = SetBonusCalculator.calculate(listWithNullsAndExtra);
        assertEquals(4, summary.getPatternCount("minecraft:rib"));
        assertEquals(TrimTier.TIER_3, summary.getPatternTier("minecraft:rib"));
    }

    @Test
    void te04_trimTierFromPieceCountClampsOutOfRangeValues() {
        assertEquals(TrimTier.NONE, TrimTier.fromPieceCount(-5));
        assertEquals(TrimTier.NONE, TrimTier.fromPieceCount(0));
        assertEquals(TrimTier.NONE, TrimTier.fromPieceCount(1));
        assertEquals(TrimTier.TIER_1, TrimTier.fromPieceCount(2));
        assertEquals(TrimTier.TIER_2, TrimTier.fromPieceCount(3));
        assertEquals(TrimTier.TIER_3, TrimTier.fromPieceCount(4));
        assertEquals(TrimTier.TIER_3, TrimTier.fromPieceCount(99));
        assertFalse(TrimTier.NONE.isActive());
        assertTrue(TrimTier.TIER_1.isActive());
        assertTrue(TrimTier.TIER_3.isAtLeast(TrimTier.TIER_2));
    }
}
