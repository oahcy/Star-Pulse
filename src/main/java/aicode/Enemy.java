package aicode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Enemy extends Spriters {
    private static final BufferedImage SPRITE = SpriteAssets.get("enemy.png");
    private final double speed;
    private final double contactDamageInterval;
    private final int contactDamage;
    private final int scoreValue;

    private double damageCooldown;

    public Enemy(double x, double y) {
        this(x, y, 26, 26, 35, 80.0, 8, 0.8, 10, new Color(231, 76, 60));
    }

    protected Enemy(
            double x,
            double y,
            double width,
            double height,
            int health,
            double speed,
            int contactDamage,
            double contactDamageInterval,
            int scoreValue,
            Color color) {
        super(x, y, width, height, health, color);
        this.speed = speed;
        this.contactDamage = contactDamage;
        this.contactDamageInterval = contactDamageInterval;
        this.scoreValue = scoreValue;
    }

    @Override
    public void update(GameMap map, double deltaTime) {
        updateEffects(deltaTime);
        Player player = map.getPlayer();
        double dx = player.centerX() - centerX();
        double dy = player.centerY() - centerY();
        double distance = Math.hypot(dx, dy);
        if (distance > 0.001) {
            dx /= distance;
            dy /= distance;
            x += dx * speed * deltaTime;
            y += dy * speed * deltaTime;
        }

        damageCooldown = Math.max(0, damageCooldown - deltaTime);
        if (collides(player) && damageCooldown <= 0) {
            player.damage(contactDamage);
            map.playHitSound();
            damageCooldown = contactDamageInterval;
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (SPRITE != null && !isBoss()) {
            graphics.drawImage(SPRITE, (int) x, (int) y, (int) width, (int) height, null);
            return;
        }
        graphics.setColor(currentColor());
        graphics.fill(new Rectangle2D.Double(x, y, width, height));
        graphics.setColor(new Color(80, 0, 0, 120));
        graphics.draw(new Rectangle2D.Double(x + 4, y + 4, width - 8, height - 8));
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public boolean isBoss() {
        return false;
    }
}
