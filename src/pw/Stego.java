package pw;

import pw.stego.Patterns;
import pw.stego.coders.Decoder;
import pw.stego.coders.Encoder;
import pw.stego.task.DecodeTask;
import pw.stego.task.EncodeTask;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Basic static functions, highest abstraction level
 * Created by lina on 21.09.16.
 */
public class Stego {
    private static final Encoder encoder = new Encoder();
    private static final Decoder decoder = new Decoder();

    public static void encode(byte[] key, byte[] message, String pattern, File container) {
        encoder.encode(new EncodeTask(
                container,
                message,
                key,
                pattern
        ));
    }

    public static void encode(File key, File message, File pattern, File container) {
        try {
            encoder.encode(new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    new String(Files.readAllBytes(pattern.toPath()))
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void encode(byte[] key, byte[] message, Patterns.Type type, File container) {
        encoder.encode(new EncodeTask(
                container,
                message,
                key,
                type
        ));
    }

    public static void encode(File key, File message, Patterns.Type type, File container) {
        try {
            encoder.encode(new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    type
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] decode(byte[] key, File container) {
        return decoder.decode(new DecodeTask(container, key));
    }

    public static byte[] decode(File key, File container) throws IOException {
        return decoder.decode(new DecodeTask(container, Files.readAllBytes(key.toPath())));
    }

    public static boolean tryKey(byte[] key, File container) {
        try {
            return decoder.checkKey(key, ImageIO.read(container));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
