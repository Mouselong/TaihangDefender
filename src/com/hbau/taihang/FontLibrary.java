package com.hbau.taihang;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class FontLibrary {
    // Cache available font family names for quick lookup
    private static final Set<String> AVAILABLE_FAMILIES = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
    ));

    // If a bundled font is provided in resources/fonts/, load it and prefer it for CJK rendering.
    private static final String[] BUNDLED_FONT_CANDIDATES = new String[] {
            "/resources/fonts/NotoSansCJKsc-Regular.otf",
            "/resources/fonts/NotoSansSC-Regular.ttf",
            "/resources/fonts/NotoSansCJKsc-Regular.ttf",
            "/resources/fonts/MicrosoftYaHei.ttf",
            "/resources/fonts/MicrosoftYaHeiUI.ttf"
    };
    private static String embeddedFamily = null;
    private static boolean cjkFamilyDetected = false;

    static {
        // Attempt to load bundled font from resources if present. This makes the app portable when the
        // font file is packaged inside the jar under resources/fonts/.
        for (String path : BUNDLED_FONT_CANDIDATES) {
            try (java.io.InputStream is = FontLibrary.class.getResourceAsStream(path)) {
                if (is == null) continue;
                Font f = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(f);
                embeddedFamily = f.getFamily();
                // add to available families set
                AVAILABLE_FAMILIES.add(embeddedFamily);
                cjkFamilyDetected = true;
                break;
            } catch (Throwable ex) {
                // ignore and try next
            }
        }

        // Fallback probe: if any known CJK family exists, mark detected for diagnostics.
        if (!cjkFamilyDetected) {
            String[] probeFamilies = {"Microsoft YaHei UI", "Microsoft YaHei", "SimSun", "Noto Sans CJK SC", "Dialog"};
            for (String fam : probeFamilies) {
                if (AVAILABLE_FAMILIES.contains(fam)) {
                    cjkFamilyDetected = true;
                    break;
                }
            }
        }
    }

    private FontLibrary() {}

    public static Font titleFont(float size) {
        String family = embeddedFamily != null ? embeddedFamily : resolveFamily(
                "Microsoft YaHei UI", "Microsoft YaHei", "Noto Sans CJK SC", "SimSun", "Segoe UI", "Dialog", "SansSerif");
        return new Font(family, Font.BOLD, Math.round(size));
    }

    public static Font bodyFont(float size) {
        String family = embeddedFamily != null ? embeddedFamily : resolveFamily(
                "Microsoft YaHei UI", "Microsoft YaHei", "Noto Sans CJK SC", "SimSun", "Segoe UI", "Dialog", "SansSerif");
        return new Font(family, Font.PLAIN, Math.round(size));
    }

    public static boolean isCjkFontAvailable() {
        return cjkFamilyDetected;
    }

    private static String resolveFamily(String... candidates) {
        for (String family : candidates) {
            if (AVAILABLE_FAMILIES.contains(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }
}
