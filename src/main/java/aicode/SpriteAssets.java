package aicode;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

public final class SpriteAssets {
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Region> ATLAS_REGIONS = new ConcurrentHashMap<>();
    private static final Map<String, String> ALIASES = Map.of(
            "player", "player",
            "bullet", "bullet",
            "enemy", "mech1",
            "boss", "doubleturret",
            "enemy_bullet", "laserend",
            "pickup", "icon-steel",
            "background_tile", "grass");
    private static volatile BufferedImage atlasImage;
    private static volatile boolean atlasLoaded;

    private SpriteAssets() {
    }

    public static BufferedImage get(String name) {
        return CACHE.computeIfAbsent(name, SpriteAssets::load);
    }

    private static BufferedImage load(String name) {
        BufferedImage atlasSprite = loadFromAtlas(name);
        if (atlasSprite != null) {
            return atlasSprite;
        }

        try (InputStream input = SpriteAssets.class.getResourceAsStream("/images/" + name)) {
            if (input == null) {
                return null;
            }
            return ImageIO.read(input);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static BufferedImage loadFromAtlas(String name) {
        ensureAtlasLoaded();
        if (atlasImage == null || ATLAS_REGIONS.isEmpty()) {
            return null;
        }

        String key = normalizeName(name);
        Region region = ATLAS_REGIONS.get(key);
        if (region == null) {
            String alias = ALIASES.get(key);
            if (alias != null) {
                region = ATLAS_REGIONS.get(alias);
            }
        }
        if (region == null) {
            return null;
        }

        int safeX = Math.max(0, Math.min(region.x, atlasImage.getWidth() - 1));
        int safeY = Math.max(0, Math.min(region.y, atlasImage.getHeight() - 1));
        int safeW = Math.min(region.width, atlasImage.getWidth() - safeX);
        int safeH = Math.min(region.height, atlasImage.getHeight() - safeY);
        if (safeW <= 0 || safeH <= 0) {
            return null;
        }
        return atlasImage.getSubimage(safeX, safeY, safeW, safeH);
    }

    private static void ensureAtlasLoaded() {
        if (atlasLoaded) {
            return;
        }
        synchronized (SpriteAssets.class) {
            if (atlasLoaded) {
                return;
            }
            atlasImage = readAtlasImage();
            ATLAS_REGIONS.putAll(readAtlasRegions());
            atlasLoaded = true;
        }
    }

    private static BufferedImage readAtlasImage() {
        try (InputStream input = SpriteAssets.class.getResourceAsStream("/sprites/moment.png")) {
            if (input == null) {
                return null;
            }
            return ImageIO.read(input);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static Map<String, Region> readAtlasRegions() {
        Map<String, Region> regions = new HashMap<>();
        try (InputStream input = SpriteAssets.class.getResourceAsStream("/sprites/moment.atlas")) {
            if (input == null) {
                return regions;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                String currentName = null;
                int x = -1;
                int y = -1;
                int w = -1;
                int h = -1;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }

                    if (!line.startsWith(" ") && !line.contains(":")) {
                        if (currentName != null && x >= 0 && y >= 0 && w > 0 && h > 0) {
                            regions.put(currentName, new Region(x, y, w, h));
                        }
                        currentName = trimmed;
                        x = -1;
                        y = -1;
                        w = -1;
                        h = -1;
                        continue;
                    }

                    if (trimmed.startsWith("xy:")) {
                        int[] pair = parsePair(trimmed.substring(3));
                        x = pair[0];
                        y = pair[1];
                    } else if (trimmed.startsWith("size:")) {
                        int[] pair = parsePair(trimmed.substring(5));
                        w = pair[0];
                        h = pair[1];
                    }
                }

                if (currentName != null && x >= 0 && y >= 0 && w > 0 && h > 0) {
                    regions.put(currentName, new Region(x, y, w, h));
                }
            }
        } catch (IOException ignored) {
            // Atlas is optional.
        }
        return regions;
    }

    private static int[] parsePair(String value) {
        String[] parts = value.split(",");
        int first = Integer.parseInt(parts[0].trim());
        int second = Integer.parseInt(parts[1].trim());
        return new int[]{first, second};
    }

    private static String normalizeName(String name) {
        String normalized = name.toLowerCase();
        if (normalized.endsWith(".png")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        return normalized;
    }

    private record Region(int x, int y, int width, int height) {
    }
}
