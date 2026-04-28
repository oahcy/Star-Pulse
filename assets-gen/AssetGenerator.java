import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AssetGenerator {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected resource root path");
        }

        File root = new File(args[0]);
        File images = new File(root, "images");
        File audio = new File(root, "audio");
        images.mkdirs();
        audio.mkdirs();

        writeImage(new File(images, "player.png"), 48, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(82, 182, 255), 48, 48, new Color(25, 88, 181)));
            Path2D ship = new Path2D.Double();
            ship.moveTo(24, 4);
            ship.lineTo(40, 38);
            ship.lineTo(24, 32);
            ship.lineTo(8, 38);
            ship.closePath();
            g.fill(ship);
            g.setColor(new Color(210, 244, 255));
            g.fill(new Ellipse2D.Double(17, 14, 14, 14));
            g.setColor(new Color(255, 200, 60, 180));
            g.fill(new Ellipse2D.Double(19, 31, 10, 12));
        });

        writeImage(new File(images, "enemy.png"), 40, g -> {
            g.setColor(new Color(186, 48, 48));
            g.fill(new RoundRectangle2D.Double(6, 8, 28, 24, 10, 10));
            g.setColor(new Color(90, 18, 18));
            g.fill(new Ellipse2D.Double(4, 14, 10, 10));
            g.fill(new Ellipse2D.Double(26, 14, 10, 10));
            g.setColor(new Color(255, 228, 135));
            g.fill(new Ellipse2D.Double(14, 14, 12, 8));
        });

        writeImage(new File(images, "boss.png"), 72, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(178, 110, 235), 72, 72, new Color(93, 36, 138)));
            g.fill(new Ellipse2D.Double(8, 8, 56, 56));
            g.setStroke(new BasicStroke(4f));
            g.setColor(new Color(255, 224, 102));
            g.draw(new Ellipse2D.Double(15, 15, 42, 42));
            g.setColor(new Color(255, 246, 196));
            g.fill(new Ellipse2D.Double(25, 25, 22, 22));
        });

        writeImage(new File(images, "bullet.png"), 16, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(255, 248, 134), 16, 16, new Color(255, 166, 0)));
            g.fill(new Ellipse2D.Double(2, 2, 12, 12));
            g.setColor(new Color(255, 255, 220, 200));
            g.fill(new Ellipse2D.Double(5, 5, 6, 6));
        });

        writeImage(new File(images, "enemy_bullet.png"), 18, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(255, 171, 171), 18, 18, new Color(214, 48, 49)));
            g.fill(new Ellipse2D.Double(2, 2, 14, 14));
            g.setColor(new Color(255, 230, 230, 180));
            g.fill(new Ellipse2D.Double(6, 6, 6, 6));
        });

        writeImage(new File(images, "pickup.png"), 28, g -> {
            g.setColor(new Color(192, 57, 43));
            Path2D heart = new Path2D.Double();
            heart.moveTo(14, 23);
            heart.curveTo(3, 15, 4, 6, 10, 6);
            heart.curveTo(13, 6, 14, 9, 14, 9);
            heart.curveTo(14, 9, 15, 6, 18, 6);
            heart.curveTo(24, 6, 25, 15, 14, 23);
            heart.closePath();
            g.fill(heart);
            g.setColor(new Color(255, 255, 255, 120));
            g.draw(new Ellipse2D.Double(3, 3, 22, 22));
        });

        writeImage(new File(images, "background_tile.png"), 96, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(21, 32, 43), 96, 96, new Color(31, 45, 61)));
            g.fillRect(0, 0, 96, 96);
            g.setColor(new Color(255, 255, 255, 18));
            for (int i = 0; i < 10; i++) {
                int x = 6 + i * 8;
                int y = 8 + (i * 11) % 80;
                g.fill(new Ellipse2D.Double(x, y, 3, 3));
            }
            g.setColor(new Color(77, 109, 143, 60));
            g.drawRect(0, 0, 95, 95);
        });

        writeWav(new File(audio, "shoot.wav"), toneBytes(880, 70, 0.16));
        writeWav(new File(audio, "hit.wav"), toneBytes(220, 90, 0.22));
        writeWav(new File(audio, "wave.wav"), toneBytes(620, 120, 0.18));
        writeWav(new File(audio, "boss.wav"), concat(toneBytes(300, 120, 0.22), toneBytes(200, 160, 0.22)));
        writeWav(new File(audio, "gameover.wav"), concat(toneBytes(340, 140, 0.18), toneBytes(220, 220, 0.18)));
        writeWav(new File(audio, "pickup.wav"), concat(toneBytes(660, 70, 0.14), toneBytes(990, 80, 0.14)));
        writeWav(new File(audio, "skill.wav"), concat(toneBytes(520, 60, 0.15), toneBytes(780, 80, 0.15)));
    }

    private static void writeImage(File file, int size, Painter painter) throws Exception {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        painter.paint(g);
        g.dispose();
        ImageIO.write(image, "png", file);
    }

    private static byte[] toneBytes(double frequency, int millis, double volume) {
        int sampleRate = 22050;
        byte[] data = new byte[(int) (sampleRate * millis / 1000.0)];
        for (int i = 0; i < data.length; i++) {
            double angle = 2.0 * Math.PI * i * frequency / sampleRate;
            data[i] = (byte) (Math.sin(angle) * 127.0 * volume);
        }
        return data;
    }

    private static byte[] concat(byte[] first, byte[] second) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(first);
        output.write(new byte[1200]);
        output.write(second);
        return output.toByteArray();
    }

    private static void writeWav(File file, byte[] data) throws Exception {
        AudioFormat format = new AudioFormat(22050f, 8, 1, true, false);
        try (AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(data), format, data.length)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
        }
    }

    @FunctionalInterface
    private interface Painter {
        void paint(Graphics2D graphics) throws Exception;
    }
}
