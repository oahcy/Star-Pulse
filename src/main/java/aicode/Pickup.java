package aicode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Pickup extends Spriters {
    private static final BufferedImage SPRITE = SpriteAssets.get("pickup.png");
    private double lifeTime = 9.0;

    public Pickup(double x, double y) {
        super(x, y, 22, 22, 1, new Color(231, 76, 60));
    }

    @Override
    public void update(GameMap map, double deltaTime) {
        updateEffects(deltaTime);
        lifeTime -= deltaTime;
        if (lifeTime <= 0) {
            removed = true;
            return;
        }

        if (!map.getPlayer().removed() && collides(map.getPlayer())) {
            map.getPlayer().heal(20);
            map.playPickupSound();
            removed = true;
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (SPRITE != null) {
            graphics.drawImage(SPRITE, (int) x, (int) y, (int) width, (int) height, null);
            return;
        }
        graphics.setColor(currentColor());
        graphics.fillOval((int) x, (int) y, (int) width, (int) height);
    }
}
