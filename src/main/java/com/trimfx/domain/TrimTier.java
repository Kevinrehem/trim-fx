package com.trimfx.domain;

public enum TrimTier {
    NONE(0, 0, ""),
    TIER_1(1, 2, "I"),
    TIER_2(2, 3, "II"),
    TIER_3(3, 4, "III");

    public static final int MIN_PIECES_FOR_TIER_1 = 2;
    public static final int MIN_PIECES_FOR_TIER_2 = 3;
    public static final int MIN_PIECES_FOR_TIER_3 = 4;
    public static final int MAX_ARMOR_SLOTS = 4;

    private final int level;
    private final int requiredPieces;
    private final String romanNumeral;

    TrimTier(int level, int requiredPieces, String romanNumeral) {
        this.level = level;
        this.requiredPieces = requiredPieces;
        this.romanNumeral = romanNumeral;
    }

    public int level() {
        return level;
    }

    public int requiredPieces() {
        return requiredPieces;
    }

    public String romanNumeral() {
        return romanNumeral;
    }

    public boolean isActive() {
        return this != NONE;
    }

    public boolean isAtLeast(TrimTier other) {
        if (other == null) {
            return false;
        }
        return this.level >= other.level;
    }

    public static TrimTier fromPieceCount(int pieceCount) {
        if (pieceCount >= MIN_PIECES_FOR_TIER_3) {
            return TIER_3;
        }
        if (pieceCount == MIN_PIECES_FOR_TIER_2) {
            return TIER_2;
        }
        if (pieceCount == MIN_PIECES_FOR_TIER_1) {
            return TIER_1;
        }
        return NONE;
    }
}
