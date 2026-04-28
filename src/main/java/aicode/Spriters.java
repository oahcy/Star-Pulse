package aicode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public abstract class Spriters {
    private static final double HIT_FLASH_DURATION = 0.12;

    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected double vx;
    protected double vy;
    protected int health;
    protected boolean removed;
    protected Color color;
    protected double hitFlashTimer;

    protected Spriters(double x, double y, double width, double height, int health, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = health;
        this.color = color;
    }

    public abstract void update(GameMap map, double deltaTime);

    public abstract void draw(Graphics2D graphics);

    public boolean removed() {
        return removed || health <= 0;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean collides(Spriters other) {
        return bounds().intersects(other.bounds());
    }

    public Rectangle2D bounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public double centerX() {
        return x + width / 2.0;
    }

    public double centerY() {
        return y + height / 2.0;
    }

    public void damage(int amount) {
        health -= amount;
        hitFlashTimer = HIT_FLASH_DURATION;
        if (health <= 0) {
            removed = true;
        }
    }

    protected void updateEffects(double deltaTime) {
        hitFlashTimer = Math.max(0, hitFlashTimer - deltaTime);
    }

    protected Color currentColor() {
        if (hitFlashTimer > 0) {
            return Color.WHITE;
        }
        return color;
    }

    public int getHealth() {
        return health;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
