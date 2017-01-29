package pw;

import pw.stego.Block;
import pw.stego.coders.Decoder;
import pw.stego.coders.Encoder;
import pw.stego.coders.WrongTaskException;
import pw.stego.task.DecodeTask;
import pw.stego.task.EncodeTask;
import pw.stego.util.Patterns;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

    public static File encode(byte[] key, byte[] message, String pattern, File container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    message,
                    key,
                    pattern
            );

            encodeTask(task);
        } catch (WrongTaskException ignored) { /*never happens*/ }

        return container;
    }

    public static File encode(File key, File message, File pattern, File container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    new String(Files.readAllBytes(pattern.toPath()))
            );

            encodeTask(task);
        } catch (WrongTaskException e) { /*never happens*/ }

        return container;
    }

    public static File encode(byte[] key, byte[] message, Patterns.Type type, File container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    message,
                    key,
                    type
            );

            encodeTask(task);
        } catch (WrongTaskException e) { /*never happens*/ }

        return container;
    }

    public static File encode(File key, File message, Patterns.Type type, File container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    type
            );

            encodeTask(task);
        } catch (WrongTaskException e) { /*never happens*/ }

        return container;
    }

    public static byte[] decode(byte[] key, File container) throws IOException {
        try {
            DecodeTask task = new DecodeTask(container, key);

            byte[] decoded = decoder.decode(task);
            task.finish();

            return decoded;
        } catch (WrongTaskException e) { /*never happens*/ }

        return new byte[0];
    }

    public static byte[] decode(File key, File container) throws IOException {
        try {
            DecodeTask task = new DecodeTask(container, Files.readAllBytes(key.toPath()));

            byte[] decoded = decoder.decode(task);
            task.finish();

            return decoded;
        } catch (WrongTaskException e) { /*never happens*/ }

        return new byte[0];
    }

    public static boolean tryKey(byte[] key, File container) throws IOException {
        return decoder.checkKey(key, ImageIO.read(container));
    }

    private static void removeBefore(BufferedImage image, Block[] toRemove, int before) {
        int length = image.getWidth();

        int firstKey;
        while ((firstKey = decoder.find(image, toRemove)) < before) {
            int x = (firstKey - 1) % length;
            int y = (firstKey - 1) / length;

            image.setRGB(x, y, image.getRGB(x, y) ^ 1);
        }
    }

    private static void encodeTask(EncodeTask task) throws WrongTaskException, IOException {
        encoder.encode(task);
        removeBefore(task.getImage(), task.getKey(), task.getStart());
        task.finish();
    }
}
