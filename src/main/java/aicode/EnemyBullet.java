package aicode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class EnemyBullet extends Spriters {
    private static final double SPEED = 210.0;
    private static final BufferedImage SPRITE = SpriteAssets.get("enemy_bullet.png");

    public EnemyBullet(double x, double y, double vx, double vy) {
        super(x, y, 14, 14, 1, new Color(214, 48, 49));
        this.vx = vx;
        this.vy = vy;
    }

    public static EnemyBullet fromEnemy(Enemy enemy, Player player) {
        double dx = player.centerX() - enemy.centerX();
        double dy = player.centerY() - enemy.centerY();
        double distance = Math.max(0.001, Math.hypot(dx, dy));
        return new EnemyBullet(
                enemy.centerX() - 7,
                enemy.centerY() - 7,
                dx / distance * SPEED,
                dy / distance * SPEED);
    }

    @Override
    public void update(GameMap map, double deltaTime) {
        updateEffects(deltaTime);
        x += vx * deltaTime;
        y += vy * deltaTime;

        if (x < -width || y < -height || x > map.getWidth() || y > map.getHeight()) {
            removed = true;
            return;
        }

        if (!map.getPlayer().removed() && collides(map.getPlayer())) {
            map.getPlayer().damage(10);
            map.playHitSound();
            removed = true;
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        double angle = Math.atan2(vy, vx) + Math.PI / 2.0;
        AffineTransform oldTransform = graphics.getTransform();
        graphics.rotate(angle, centerX(), centerY());
        if (SPRITE != null) {
            graphics.drawImage(SPRITE, (int) x, (int) y, (int) width, (int) height, null);
            graphics.setTransform(oldTransform);
            return;
        }
        graphics.setColor(currentColor());
        graphics.fillOval((int) x, (int) y, (int) width, (int) height);
        graphics.setTransform(oldTransform);
    }
}
