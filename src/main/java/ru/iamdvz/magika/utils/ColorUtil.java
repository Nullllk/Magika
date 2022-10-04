package ru.iamdvz.magika.utils;

import org.bukkit.Color;

public class ColorUtil {
    public static Color hexToRGBColor(String hex) {
        if (hex == null || hex.length() < 6) return null;

        try {
            final var r = Integer.parseInt(hex.substring(0, 2), 16);
            final var g = Integer.parseInt(hex.substring(2, 4), 16);
            final var b = Integer.parseInt(hex.substring(4, 6), 16);

            return Color.fromRGB(r, g, b);
        } catch (Exception ignored) {
            return null;
        }
    }
}