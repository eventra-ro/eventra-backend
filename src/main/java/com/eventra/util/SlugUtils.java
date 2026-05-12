package com.eventra.util;

import java.text.Normalizer;

public final class SlugUtils {

    private SlugUtils() {}

    public static String generate(String businessName, String countyCode) {
        String normalized = Normalizer
                .normalize(businessName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-");

        return normalized + "-" + countyCode.toLowerCase();
    }

    public static String makeUnique(String baseSlug, int attempt) {
        if (attempt == 0) return baseSlug;
        return baseSlug + "-" + attempt;
    }
}