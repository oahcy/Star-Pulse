package aicode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMap {
    private static final int GRID_SIZE = 40;
    private static final int BASE_ENEMIES = 4;
    private static final int ENEMIES_PER_WAVE = 2;
    private static final int BOSS_WAVE_INTERVAL = 3;
    private static final double PICKUP_DROP_RATE = 0.25;
    private static final double SKILL_RADIUS = 130.0;
    private static final int SKILL_DAMAGE = 24;
    private static final BufferedImage BACKGROUND_TILE = SpriteAssets.get("background_tile.png");

    private final int width;
    private final int height;
    private final Random random = new Random();
    private final Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private boolean suppressNextWaveSound;
    private int wave = 1;
    private int score = 0;
    private boolean waveBannerVisible = true;
    private double waveBannerTimer = 2.0;

    public GameMap(int width, int height) {
        this(width, height, false);
    }

    public GameMap(int width, int height, boolean silentStart) {
        this.width = width;
        this.height = height;
        this.player = new Player(width / 2.0 - 15, height / 2.0 - 15);
        this.suppressNextWaveSound = silentStart;
        startWave(wave);
    }

    public void update(double deltaTime) {
        if (!player.removed()) {
            player.update(this, deltaTime);
        }

        for (Enemy enemy : enemies) {
            if (!enemy.removed()) {
                enemy.update(this, deltaTime);
            }
        }

        for (Bullet bullet : bullets) {
            if (!bullet.removed()) {
                bullet.update(this, deltaTime);
            }
        }

        for (EnemyBullet bullet : enemyBullets) {
            if (!bullet.removed()) {
                bullet.update(this, deltaTime);
            }
        }

        for (Pickup pickup : pickups) {
            if (!pickup.removed()) {
                pickup.update(this, deltaTime);
            }
        }

        enemies.removeIf(Spriters::removed);
        bullets.removeIf(Spriters::removed);
        enemyBullets.removeIf(Spriters::removed);
        pickups.removeIf(Spriters::removed);

        if (waveBannerVisible) {
            waveBannerTimer = Math.max(0, waveBannerTimer - deltaTime);
            if (waveBannerTimer == 0) {
                waveBannerVisible = false;
            }
        }

        if (!player.removed() && enemies.isEmpty()) {
            wave++;
            startWave(wave);
        }
    }

    public void draw(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (BACKGROUND_TILE != null) {
            graphics.setPaint(new TexturePaint(BACKGROUND_TILE, new Rectangle2D.Double(0, 0, BACKGROUND_TILE.getWidth(), BACKGROUND_TILE.getHeight())));
        } else {
            graphics.setPaint(new GradientPaint(0, 0, new Color(17, 24, 39), 0, height, new Color(28, 40, 51)));
        }
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(new Color(44, 62, 80));
        for (int x = 0; x <= width; x += GRID_SIZE) {
            graphics.drawLine(x, 0, x, height);
        }
        for (int y = 0; y <= height; y += GRID_SIZE) {
            graphics.drawLine(0, y, width, y);
        }

        drawBackgroundDecor(graphics);

        player.draw(graphics);
        for (Enemy enemy : enemies) {
            enemy.draw(graphics);
        }
        for (Bullet bullet : bullets) {
            bullet.draw(graphics);
        }
        for (EnemyBullet enemyBullet : enemyBullets) {
            enemyBullet.draw(graphics);
        }
        for (Pickup pickup : pickups) {
            pickup.draw(graphics);
        }

        drawHud(graphics);
    }

    private void drawHud(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.drawString("HP: " + Math.max(0, player.getHealth()), 16, 24);
        graphics.drawString("Wave: " + wave, 16, 44);
        graphics.drawString("Score: " + score, 16, 64);
        graphics.drawString("Enemies: " + enemies.size(), 16, 84);
        graphics.drawString("Skill: " + skillText(), 16, 104);
        if (containsBoss()) {
            graphics.drawString("Boss Incoming!", 16, 124);
        }

        int barX = 70;
        int barY = 12;
        int barWidth = 160;
        int barHeight = 12;
        graphics.setColor(new Color(127, 140, 141));
        graphics.drawRect(barX, barY, barWidth, barHeight);
        graphics.setStroke(new BasicStroke(2f));
        graphics.setColor(new Color(46, 204, 113));
        int currentWidth = (int) (Math.max(0, player.getHealth()) / 100.0 * (barWidth - 1));
        graphics.fillRect(barX + 1, barY + 1, currentWidth, barHeight - 1);

        if (waveBannerVisible && !player.removed()) {
            Font oldFont = graphics.getFont();
            graphics.setFont(oldFont.deriveFont(Font.BOLD, 28f));
            graphics.setColor(new Color(236, 240, 241));
            String title = isBossWave(wave) ? "Boss Wave " + wave : "Wave " + wave;
            graphics.drawString(title, width / 2 - 80, 80);
            graphics.setFont(oldFont);
        }

        if (player.removed()) {
            graphics.setColor(new Color(236, 240, 241));
            graphics.drawString("Game Over", width / 2 - 34, height / 2 - 10);
            graphics.drawString("Final Score: " + score, width / 2 - 42, height / 2 + 12);
            graphics.drawString("Press Enter to restart", width / 2 - 62, height / 2 + 34);
        }
    }

    private void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            enemies.add(new Enemy(randomSpawnX(), randomSpawnY()));
        }
    }

    private void startWave(int waveNumber) {
        int normalEnemies = BASE_ENEMIES + (waveNumber - 1) * ENEMIES_PER_WAVE;
        if (isBossWave(waveNumber)) {
            normalEnemies = Math.max(2, normalEnemies / 2);
            enemies.add(new BossEnemy(randomSpawnX(), randomSpawnY()));
            if (!suppressNextWaveSound) {
                triggerBossSound();
            }
        } else {
            if (!suppressNextWaveSound) {
                triggerWaveSound();
            }
        }
        suppressNextWaveSound = false;
        spawnEnemies(normalEnemies);
        waveBannerVisible = true;
        waveBannerTimer = 2.0;
    }

    private boolean isBossWave(int waveNumber) {
        return waveNumber > 0 && waveNumber % BOSS_WAVE_INTERVAL == 0;
    }

    private boolean containsBoss() {
        for (Enemy enemy : enemies) {
            if (enemy.isBoss()) {
                return true;
            }
        }
        return false;
    }

    private void drawBackgroundDecor(Graphics2D graphics) {
        graphics.setColor(new Color(255, 255, 255, 16));
        for (int i = 0; i < 18; i++) {
            int starX = (int) ((i * 53L + wave * 29L) % width);
            int starY = (int) ((i * 97L + wave * 17L) % height);
            graphics.fillOval(starX, starY, 3, 3);
        }
    }

    private double randomSpawnX() {
        while (true) {
            double candidate = random.nextDouble() * (width - 30);
            if (Math.abs(candidate - player.getX()) > 120) {
                return candidate;
            }
        }
    }

    private double randomSpawnY() {
        while (true) {
            double candidate = random.nextDouble() * (height - 30);
            if (Math.abs(candidate - player.getY()) > 120) {
                return candidate;
            }
        }
    }

    public Enemy findNearestEnemyInRange(Player player, double range) {
        Enemy nearest = null;
        double nearestDistance = range;
        for (Enemy enemy : enemies) {
            double distance = Math.hypot(enemy.centerX() - player.centerX(), enemy.centerY() - player.centerY());
            if (distance <= nearestDistance) {
                nearest = enemy;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public void spawnBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void spawnEnemyBullet(EnemyBullet bullet) {
        enemyBullets.add(bullet);
    }

    public void onEnemyDefeated(Enemy enemy) {
        score += enemy.getScoreValue();
        if (random.nextDouble() < PICKUP_DROP_RATE) {
            pickups.add(new Pickup(enemy.getX(), enemy.getY()));
        }
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWave() {
        return wave;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return player.removed();
    }

    public void playShootSound() {
        AudioAssets.play("shoot.wav", TonePlayer::playShoot);
    }

    public void playHitSound() {
        AudioAssets.play("hit.wav", TonePlayer::playHit);
    }

    public void playPickupSound() {
        AudioAssets.play("pickup.wav", TonePlayer::playPickup);
    }

    public void playSkillSound() {
        AudioAssets.play("skill.wav", TonePlayer::playSkill);
    }

    public void triggerWaveSound() {
        AudioAssets.play("wave.wav", TonePlayer::playWaveStart);
    }

    public void triggerBossSound() {
        AudioAssets.play("boss.wav", TonePlayer::playBossSpawn);
    }

    public void usePulseSkill() {
        if (player.removed() || !player.canUseSkill()) {
            return;
        }

        player.triggerSkillCooldown();
        playSkillSound();
        for (Enemy enemy : enemies) {
            if (!enemy.removed() && distance(player.centerX(), player.centerY(), enemy.centerX(), enemy.centerY()) <= SKILL_RADIUS) {
                boolean defeated = enemy.getHealth() <= SKILL_DAMAGE;
                enemy.damage(SKILL_DAMAGE);
                if (defeated) {
                    onEnemyDefeated(enemy);
                }
            }
        }
        enemyBullets.clear();
    }

    private String skillText() {
        return player.canUseSkill() ? "Ready (Space)" : String.format("%.1fs", player.getSkillCooldown());
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}
