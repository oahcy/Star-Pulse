package aicode;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public final class AudioAssets {
    private AudioAssets() {
    }

    public static void play(String name, Runnable fallback) {
        new Thread(() -> playInternal(name, fallback), "audio-" + name).start();
    }

    private static void playInternal(String name, Runnable fallback) {
        try (InputStream raw = AudioAssets.class.getResourceAsStream("/audio/" + name);
             BufferedInputStream input = raw == null ? null : new BufferedInputStream(raw)) {
            if (input == null) {
                if (fallback != null) {
                    fallback.run();
                }
                return;
            }

            try (AudioInputStream stream = AudioSystem.getAudioInputStream(input)) {
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.addLineListener(event -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            }
        } catch (Exception ignored) {
            if (fallback != null) {
                fallback.run();
            }
        }
    }
}
