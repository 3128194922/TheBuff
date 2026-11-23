package com.thebuff.api;

import java.util.Arrays;
import java.util.Locale;

public enum BuffCategory {
    BENEFICIAL,
    HARMFUL,
    NEUTRAL;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BuffCategory byName(String name) {
        if (name == null || name.isBlank()) {
            return NEUTRAL;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(category -> category.id().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown buff category: " + name));
    }
}
