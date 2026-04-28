package aicode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

public final class SpriteAssets {
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    private SpriteAssets() {
    }

    public static BufferedImage get(String name) {
        return CACHE.computeIfAbsent(name, SpriteAssets::load);
    }

    private static BufferedImage load(String name) {
        try (InputStream input = SpriteAssets.class.getResourceAsStream("/images/" + name)) {
            if (input == null) {
                return null;
            }
            return ImageIO.read(input);
        } catch (IOException ignored) {
            return null;
        }
    }
}
