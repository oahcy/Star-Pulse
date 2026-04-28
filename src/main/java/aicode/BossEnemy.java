package aicode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class BossEnemy extends Enemy {
    private static final BufferedImage SPRITE = SpriteAssets.get("boss.png");
    private double bulletCooldown = 1.8;

    public BossEnemy(double x, double y) {
        super(x, y, 52, 52, 140, 65.0, 16, 0.65, 60, new Color(155, 89, 182));
    }

    @Override
    public void update(GameMap map, double deltaTime) {
        super.update(map, deltaTime);
        bulletCooldown = Math.max(0, bulletCooldown - deltaTime);
        if (!removed() && bulletCooldown <= 0 && !map.getPlayer().removed()) {
            map.spawnEnemyBullet(EnemyBullet.fromEnemy(this, map.getPlayer()));
            bulletCooldown = 1.45;
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (SPRITE != null) {
            graphics.drawImage(SPRITE, (int) x, (int) y, (int) width, (int) height, null);
            return;
        }
        graphics.setColor(currentColor());
        graphics.fill(new Ellipse2D.Double(x, y, width, height));
        graphics.setColor(new Color(241, 196, 15));
        graphics.draw(new Rectangle2D.Double(x + 10, y + 10, width - 20, height - 20));
        graphics.draw(new Rectangle2D.Double(x + 16, y + 16, width - 32, height - 32));
    }

    @Override
    public boolean isBoss() {
        return true;
    }
}
