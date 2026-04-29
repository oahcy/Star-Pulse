package aicode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Game extends JPanel implements KeyListener, MouseListener, MouseMotionListener, Runnable {
    private static final int WIDTH = 960;
    private static final int HEIGHT = 640;
    private static final long TARGET_FRAME_NS = 1_000_000_000L / 60; // 约 16.67ms

    private GameMap map = new GameMap(WIDTH, HEIGHT, true);
    private long lastUpdateTime = System.nanoTime();
    private long lastFpsUpdateTime = System.nanoTime();
    private int frameCount = 0;
    private double fps = 0;
    private final RoundRectangle2D.Double primaryButton = new RoundRectangle2D.Double(WIDTH / 2.0 - 120, HEIGHT / 2.0 + 90, 240, 48, 18, 18);
    private final RoundRectangle2D.Double secondaryButton = new RoundRectangle2D.Double(WIDTH / 2.0 - 120, HEIGHT / 2.0 + 150, 240, 48, 18, 18);
    private boolean hoverPrimary;
    private boolean hoverSecondary;
    private volatile boolean running = true;

    private enum GameState {
        START_SCREEN,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private GameState state = GameState.START_SCREEN;

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        Thread gameThread = new Thread(this, "GameThread");
        gameThread.setPriority(Thread.MAX_PRIORITY);
        gameThread.start();
    }

    @Override
    public void run() {
        while (running) {
            long frameStart = System.nanoTime();
            
            // 更新逻辑
            long now = System.nanoTime();
            double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
            lastUpdateTime = now;

            frameCount++;
            long fpsDelta = now - lastFpsUpdateTime;
            if (fpsDelta >= 1_000_000_000) {
                fps = frameCount * 1_000_000_000.0 / fpsDelta;
                frameCount = 0;
                lastFpsUpdateTime = now;
            }

            if (state == GameState.PLAYING) {
                map.update(Math.min(deltaTime, 0.05));
                if (map.isGameOver()) {
                    map.getPlayer().clearMovement();
                    AudioAssets.play("gameover.wav", TonePlayer::playGameOver);
                    state = GameState.GAME_OVER;
                }
            }

            repaint();
            
            // 动态计算休眠时间
            long frameEnd = System.nanoTime();
            long frameDuration = frameEnd - frameStart;
            long sleepTime = TARGET_FRAME_NS - frameDuration;
            
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Star Pulse");
            Game game = new Game();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        map.draw(g2d);
        if (state == GameState.PLAYING) {
            g2d.setColor(Color.WHITE);
            g2d.drawString(String.format("FPS: %.1f", fps), WIDTH - 70, 24);
        }
        if (state == GameState.START_SCREEN) {
            drawStartScreen(g2d);
        } else if (state == GameState.PAUSED) {
            drawPauseOverlay(g2d);
        } else if (state == GameState.GAME_OVER) {
            drawGameOverButtons(g2d);
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // No-op.
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_P || event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            togglePause();
            return;
        }

        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            if (state == GameState.START_SCREEN) {
                startGame();
                return;
            }
            if (state == GameState.PAUSED) {
                state = GameState.PLAYING;
                lastUpdateTime = System.nanoTime();
                return;
            }
            if (state == GameState.GAME_OVER) {
                restartGame();
                return;
            }
        }

        if (state != GameState.PLAYING) {
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            map.usePulseSkill();
            return;
        }
        updateMovement(event, true);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (state != GameState.PLAYING) {
            return;
        }
        updateMovement(event, false);
    }

    private void startGame() {
        restartGame();
    }

    private void restartGame() {
        map = new GameMap(WIDTH, HEIGHT);
        lastUpdateTime = System.nanoTime();
        state = GameState.PLAYING;
    }

    private void updateMovement(KeyEvent event, boolean pressed) {
        Player player = map.getPlayer();
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> player.setMoveUp(pressed);
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> player.setMoveDown(pressed);
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> player.setMoveLeft(pressed);
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> player.setMoveRight(pressed);
            default -> {
            }
        }
    }

    private void drawStartScreen(Graphics2D graphics) {
        graphics.setColor(new Color(0, 0, 0, 170));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);

        Font oldFont = graphics.getFont();
        graphics.setColor(new Color(236, 240, 241));
        graphics.setFont(oldFont.deriveFont(Font.BOLD, 36f));
        graphics.drawString("Star Pulse", WIDTH / 2 - 100, HEIGHT / 2 - 70);

        graphics.setFont(oldFont.deriveFont(Font.PLAIN, 18f));
        graphics.drawString("WASD / Arrow Keys Move", WIDTH / 2 - 105, HEIGHT / 2 - 15);
        graphics.drawString("Auto fire when enemies are nearby", WIDTH / 2 - 132, HEIGHT / 2 + 15);
        graphics.drawString("Space: pulse skill clears bullets and damages nearby enemies", WIDTH / 2 - 240, HEIGHT / 2 + 45);
        graphics.drawString("Press P to pause during battle", WIDTH / 2 - 130, HEIGHT / 2 + 75);
        drawButton(graphics, primaryButton, hoverPrimary, "Start Game");
        drawButton(graphics, secondaryButton, hoverSecondary, "Exit");
        graphics.drawString("Press Enter to Start", WIDTH / 2 - 92, HEIGHT / 2 + 230);
        graphics.setFont(oldFont);
    }

    private void drawPauseOverlay(Graphics2D graphics) {
        graphics.setColor(new Color(0, 0, 0, 150));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        Font oldFont = graphics.getFont();
        graphics.setColor(Color.WHITE);
        graphics.setFont(oldFont.deriveFont(Font.BOLD, 34f));
        graphics.drawString("Paused", WIDTH / 2 - 56, HEIGHT / 2 - 10);
        graphics.setFont(oldFont.deriveFont(Font.PLAIN, 18f));
        graphics.drawString("Press P, Esc, or Enter to resume", WIDTH / 2 - 140, HEIGHT / 2 + 28);
        graphics.setFont(oldFont);
    }

    private void drawGameOverButtons(Graphics2D graphics) {
        drawButton(graphics, primaryButton, hoverPrimary, "Restart");
        drawButton(graphics, secondaryButton, hoverSecondary, "Back To Menu");
    }

    private void drawButton(Graphics2D graphics, RoundRectangle2D button, boolean hovered, String label) {
        graphics.setColor(hovered ? new Color(52, 152, 219) : new Color(41, 128, 185, 215));
        graphics.fill(button);
        graphics.setColor(new Color(236, 240, 241));
        graphics.draw(button);
        Font oldFont = graphics.getFont();
        graphics.setFont(oldFont.deriveFont(Font.BOLD, 20f));
        int textX = (int) (button.getX() + button.getWidth() / 2 - graphics.getFontMetrics().stringWidth(label) / 2.0);
        int textY = (int) (button.getY() + 31);
        graphics.drawString(label, textX, textY);
        graphics.setFont(oldFont);
    }

    private void togglePause() {
        if (state == GameState.PLAYING) {
            map.getPlayer().clearMovement();
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            lastUpdateTime = System.nanoTime();
        }
    }

    private void handleMouseClick(int x, int y) {
        if (state == GameState.START_SCREEN) {
            if (primaryButton.contains(x, y)) {
                startGame();
            } else if (secondaryButton.contains(x, y)) {
                System.exit(0);
            }
        } else if (state == GameState.GAME_OVER) {
            if (primaryButton.contains(x, y)) {
                restartGame();
            } else if (secondaryButton.contains(x, y)) {
                state = GameState.START_SCREEN;
                map = new GameMap(WIDTH, HEIGHT, true);
                lastUpdateTime = System.nanoTime();
            }
        }
    }

    private void updateHoverState(int x, int y) {
        hoverPrimary = (state == GameState.START_SCREEN || state == GameState.GAME_OVER) && primaryButton.contains(x, y);
        hoverSecondary = (state == GameState.START_SCREEN || state == GameState.GAME_OVER) && secondaryButton.contains(x, y);
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        handleMouseClick(event.getX(), event.getY());
        requestFocusInWindow();
    }

    @Override
    public void mousePressed(MouseEvent event) {
        // No-op.
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        // No-op.
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        updateHoverState(event.getX(), event.getY());
    }

    @Override
    public void mouseExited(MouseEvent event) {
        hoverPrimary = false;
        hoverSecondary = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        updateHoverState(event.getX(), event.getY());
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        updateHoverState(event.getX(), event.getY());
    }
}
