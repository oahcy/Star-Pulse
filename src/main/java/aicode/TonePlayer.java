package aicode;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public final class TonePlayer {
    private static final float SAMPLE_RATE = 22050f;

    private TonePlayer() {
    }

    public static void playShoot() {
        playAsync(880, 50, 0.18);
    }

    public static void playHit() {
        playAsync(220, 80, 0.22);
    }

    public static void playWaveStart() {
        playAsync(660, 120, 0.20);
    }

    public static void playBossSpawn() {
        new Thread(() -> {
            playTone(300, 100, 0.22);
            playTone(220, 140, 0.24);
        }, "boss-tone").start();
    }

    public static void playGameOver() {
        new Thread(() -> {
            playTone(330, 130, 0.22);
            playTone(247, 180, 0.20);
        }, "gameover-tone").start();
    }

    public static void playPickup() {
        new Thread(() -> {
            playTone(660, 70, 0.14);
            playTone(990, 80, 0.14);
        }, "pickup-tone").start();
    }

    public static void playSkill() {
        new Thread(() -> {
            playTone(520, 60, 0.15);
            playTone(780, 80, 0.15);
        }, "skill-tone").start();
    }

    private static void playAsync(double frequency, int millis, double volume) {
        new Thread(() -> playTone(frequency, millis, volume), "tone-player").start();
    }

    private static void playTone(double frequency, int millis, double volume) {
        byte[] buffer = new byte[(int) (SAMPLE_RATE * millis / 1000.0)];
        for (int i = 0; i < buffer.length; i++) {
            double angle = 2.0 * Math.PI * i * frequency / SAMPLE_RATE;
            buffer[i] = (byte) (Math.sin(angle) * 127.0 * volume);
        }

        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        SourceDataLine line = null;
        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, buffer.length);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        } catch (LineUnavailableException ignored) {
            // Audio is optional; the game still works without it.
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }
}
