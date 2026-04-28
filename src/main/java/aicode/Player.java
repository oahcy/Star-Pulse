package aicode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Player extends Spriters {
    private static final int MAX_HEALTH = 100;
    private static final double SPEED = 220.0;
    private static final double SHOOT_RANGE = 210.0;
    private static final double SHOOT_INTERVAL = 0.35;
    private static final BufferedImage SPRITE = SpriteAssets.get("player.png");

    private double shotCooldown;
    private double skillCooldown;
    private boolean moveUp;
    private boolean moveDown;
    private boolean moveLeft;
    private boolean moveRight;
    private double facingAngle = 0.0;

    public Player(double x, double y) {
        super(x, y, 30, 30, 100, new Color(52, 152, 219));
    }

    @Override
    public void update(GameMap map, double deltaTime) {
        updateEffects(deltaTime);
        double dx = 0;
        double dy = 0;
        if (moveUp) {
            dy -= 1;
        }
        if (moveDown) {
            dy += 1;
        }
        if (moveLeft) {
            dx -= 1;
        }
        if (moveRight) {
            dx += 1;
        }

        double length = Math.hypot(dx, dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
            facingAngle = Math.atan2(dy, dx) + Math.PI / 2.0;
        }

        x += dx * SPEED * deltaTime;
        y += dy * SPEED * deltaTime;

        x = clamp(x, 0, map.getWidth() - width);
        y = clamp(y, 0, map.getHeight() - height);

        shotCooldown = Math.max(0, shotCooldown - deltaTime);
        skillCooldown = Math.max(0, skillCooldown - deltaTime);
        Enemy nearestEnemy = map.findNearestEnemyInRange(this, SHOOT_RANGE);
        if (nearestEnemy != null && shotCooldown <= 0) {
            map.spawnBullet(Bullet.fromPlayer(this, nearestEnemy));
            map.playShootSound();
            shotCooldown = SHOOT_INTERVAL;
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        AffineTransform oldTransform = graphics.getTransform();
        graphics.rotate(facingAngle, centerX(), centerY());
        if (SPRITE != null) {
            graphics.drawImage(SPRITE, (int) x, (int) y, (int) width, (int) height, null);
            graphics.setTransform(oldTransform);
            return;
        }
        graphics.setColor(currentColor());
        graphics.fill(new Ellipse2D.Double(x, y, width, height));
        graphics.setColor(new Color(174, 214, 241));
        graphics.fill(new Ellipse2D.Double(x + 8, y + 6, width - 16, height - 16));
        graphics.setColor(new Color(21, 67, 96, 160));
        graphics.drawLine((int) centerX(), (int) y + 6, (int) centerX(), (int) (y + height - 6));
        graphics.drawLine((int) x + 6, (int) centerY(), (int) (x + width - 6), (int) centerY());
        graphics.setTransform(oldTransform);
    }

    public void setMoveUp(boolean moveUp) {
        this.moveUp = moveUp;
    }

    public void setMoveDown(boolean moveDown) {
        this.moveDown = moveDown;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public void clearMovement() {
        moveUp = false;
        moveDown = false;
        moveLeft = false;
        moveRight = false;
    }

    public boolean canUseSkill() {
        return skillCooldown <= 0;
    }

    public void triggerSkillCooldown() {
        skillCooldown = 6.0;
    }

    public double getSkillCooldown() {
        return skillCooldown;
    }

    public void heal(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
    }

    public double getFacingAngle() {
        return facingAngle;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
