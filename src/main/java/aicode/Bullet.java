package aicode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Bullet extends Spriters {
    private static final double SPEED = 380.0;
    private static final int DAMAGE = 12;
    private static final BufferedImage SPRITE = SpriteAssets.get("bullet.png");

    private Bullet(double x, double y, double vx, double vy) {
        super(x, y, 8, 8, 1, new Color(241, 196, 15));
        this.vx = vx;
        this.vy = vy;
    }

    public static Bullet fromPlayer(Player player, Enemy target) {
        double dx = target.centerX() - player.centerX();
        double dy = target.centerY() - player.centerY();
        double distance = Math.max(0.001, Math.hypot(dx, dy));
        double velocityX = dx / distance * SPEED;
        double velocityY = dy / distance * SPEED;
        return new Bullet(player.centerX() - 4, player.centerY() - 4, velocityX, velocityY);
    }

    @Override
    public void update(GameMap map, double deltaTime) {
        updateEffects(deltaTime);
        x += vx * deltaTime;
        y += vy * deltaTime;

        if (x < 0 || y < 0 || x > map.getWidth() || y > map.getHeight()) {
            removed = true;
            return;
        }

        for (Enemy enemy : map.getEnemies()) {
            if (!enemy.removed() && collides(enemy)) {
                boolean defeated = enemy.getHealth() <= DAMAGE;
                enemy.damage(DAMAGE);
                if (defeated) {
                    map.onEnemyDefeated(enemy);
                }
                map.playHitSound();
                removed = true;
                return;
            }
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (SPRITE != null) {
            graphics.drawImage(SPRITE, (int) x - 2, (int) y - 2, (int) width + 4, (int) height + 4, null);
            return;
        }
        graphics.setColor(currentColor());
        graphics.fill(new Ellipse2D.Double(x, y, width, height));
        graphics.setColor(new Color(255, 245, 157, 120));
        graphics.fill(new Ellipse2D.Double(x - 2, y - 2, width + 4, height + 4));
    }
}
