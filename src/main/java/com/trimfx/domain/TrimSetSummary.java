package com.trimfx.domain;

import java.util.Map;

public record TrimSetSummary(
        Map<String, Integer> patternCounts,
        Map<String, Integer> materialCounts,
        Map<String, TrimTier> activePatterns,
        Map<String, TrimTier> activeMaterials
) {

    public static final TrimSetSummary EMPTY = new TrimSetSummary(
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of()
    );

    public TrimSetSummary {
        patternCounts = patternCounts == null ? Map.of() : Map.copyOf(patternCounts);
        materialCounts = materialCounts == null ? Map.of() : Map.copyOf(materialCounts);
        activePatterns = activePatterns == null ? Map.of() : Map.copyOf(activePatterns);
        activeMaterials = activeMaterials == null ? Map.of() : Map.copyOf(activeMaterials);
    }

    public int getPatternCount(String patternId) {
        String normalized = ArmorSlotTrim.normalizeId(patternId);
        return patternCounts.getOrDefault(normalized, 0);
    }

    public int getMaterialCount(String materialId) {
        String normalized = ArmorSlotTrim.normalizeId(materialId);
        return materialCounts.getOrDefault(normalized, 0);
    }

    public TrimTier getPatternTier(String patternId) {
        String normalized = ArmorSlotTrim.normalizeId(patternId);
        return activePatterns.getOrDefault(normalized, TrimTier.NONE);
    }

    public TrimTier getMaterialTier(String materialId) {
        String normalized = ArmorSlotTrim.normalizeId(materialId);
        return activeMaterials.getOrDefault(normalized, TrimTier.NONE);
    }
}
