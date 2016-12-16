package pw;

import pw.stego.coders.Decoder;
import pw.stego.coders.Encoder;
import pw.stego.coders.WrongTaskException;
import pw.stego.task.DecodeTask;
import pw.stego.task.EncodeTask;
import pw.stego.util.Patterns;

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

    public static File encode(byte[] key, byte[] message, String pattern, File container) {
        try {
            encoder.encode(new EncodeTask(
                    container,
                    message,
                    key,
                    pattern
            ));
        } catch (WrongTaskException ignored) { /*never happens*/ }

        return container;
    }

    public static File encode(File key, File message, File pattern, File container) throws IOException {
        try {
            encoder.encode(new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    new String(Files.readAllBytes(pattern.toPath()))
            ));
        } catch (WrongTaskException e) { /*never happens*/ }

        return container;
    }

    public static File encode(byte[] key, byte[] message, Patterns.Type type, File container) {
        try {
            encoder.encode(new EncodeTask(
                    container,
                    message,
                    key,
                    type
            ));
        } catch (WrongTaskException e) { /*never happens*/ }

        return container;
    }

    public static File encode(File key, File message, Patterns.Type type, File container) throws IOException {
        try {
            encoder.encode(new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    type
            ));
        } catch (WrongTaskException e) { /*never happens*/ }

        return container;
    }

    public static byte[] decode(byte[] key, File container) {
        try {
            return decoder.decode(new DecodeTask(container, key));
        } catch (WrongTaskException e) { /*never happens*/ }

        return new byte[0];
    }

    public static byte[] decode(File key, File container) throws IOException {
        try {
            return decoder.decode(new DecodeTask(container, Files.readAllBytes(key.toPath())));
        } catch (WrongTaskException e) { /*never happens*/ }

        return new byte[0];
    }

    public static boolean tryKey(byte[] key, File container) throws IOException {
        return decoder.checkKey(key, ImageIO.read(container));
    }
}
