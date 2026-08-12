package com.hnigrowth.intent;

/** Buying-intent bands derived from the intent score (0-100). */
public enum IntentBand {
    COLD, LOW, MEDIUM, HIGH, HOT;

    public static IntentBand fromScore(int score) {
        if (score >= 80) return HOT;
        if (score >= 60) return HIGH;
        if (score >= 40) return MEDIUM;
        if (score >= 20) return LOW;
        return COLD;
    }
}
