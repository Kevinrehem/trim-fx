package com.trimfx.domain;

import java.util.Locale;

public record ArmorSlotTrim(String patternId, String materialId) {

    public static final String DEFAULT_NAMESPACE = "minecraft:";
    public static final String BOP_ROSE_QUARTZ_ID = "biomesoplenty:rose_quartz";
    private static final String ROSE_QUARTZ_PATH = "rose_quartz";

    public ArmorSlotTrim {
        patternId = normalizeId(patternId);
        materialId = normalizeId(materialId);
    }

    public static String normalizeId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return "";
        }
        String trimmed = rawId.trim().toLowerCase(Locale.ROOT);
        String path = pathOnly(trimmed);
        if (ROSE_QUARTZ_PATH.equals(path)) {
            return BOP_ROSE_QUARTZ_ID;
        }
        if (!trimmed.contains(":")) {
            return DEFAULT_NAMESPACE + trimmed;
        }
        return trimmed;
    }

    public static String pathOnly(String normalizedId) {
        if (normalizedId == null || normalizedId.isBlank()) {
            return "";
        }
        int colonIndex = normalizedId.indexOf(':');
        return colonIndex >= 0 ? normalizedId.substring(colonIndex + 1) : normalizedId;
    }

    public boolean hasPattern() {
        return !patternId.isEmpty();
    }

    public boolean hasMaterial() {
        return !materialId.isEmpty();
    }
}
