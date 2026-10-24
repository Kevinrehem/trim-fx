package com.trimfx.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SetBonusCalculator {

    private SetBonusCalculator() {
    }

    public static TrimSetSummary calculate(Collection<ArmorSlotTrim> equippedTrims) {
        if (equippedTrims == null || equippedTrims.isEmpty()) {
            return TrimSetSummary.EMPTY;
        }

        Map<String, Integer> patternCounts = new LinkedHashMap<>();
        Map<String, Integer> materialCounts = new LinkedHashMap<>();

        int processedSlots = 0;
        for (ArmorSlotTrim slotTrim : equippedTrims) {
            if (slotTrim == null) {
                continue;
            }
            if (processedSlots >= TrimTier.MAX_ARMOR_SLOTS) {
                break;
            }
            processedSlots++;
            incrementIfPresent(patternCounts, slotTrim.patternId());
            incrementIfPresent(materialCounts, slotTrim.materialId());
        }

        Map<String, TrimTier> activePatterns = resolveActiveTiers(patternCounts);
        Map<String, TrimTier> activeMaterials = resolveActiveTiers(materialCounts);

        return new TrimSetSummary(patternCounts, materialCounts, activePatterns, activeMaterials);
    }

    private static void incrementIfPresent(Map<String, Integer> counter, String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        int current = counter.getOrDefault(key, 0);
        counter.put(key, Math.min(TrimTier.MAX_ARMOR_SLOTS, current + 1));
    }

    private static Map<String, TrimTier> resolveActiveTiers(Map<String, Integer> counts) {
        Map<String, TrimTier> activeTiers = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            TrimTier tier = TrimTier.fromPieceCount(entry.getValue());
            if (tier.isActive()) {
                activeTiers.put(entry.getKey(), tier);
            }
        }
        return activeTiers;
    }
}
