package pw;

import pw.stego.TaskHandler;
import pw.stego.coders.Decoder;
import pw.stego.coders.KeyNotFoundException;
import pw.stego.coders.WrongTaskException;
import pw.stego.task.DecodeTask;
import pw.stego.task.EncodeTask;
import pw.stego.util.Patterns;
import pw.stego.util.StegoImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Basic static functions, highest abstraction level
 * Created by lina on 21.09.16.
 */
@SuppressWarnings("unused")
public class Stego {
    public static StegoImage encode(byte[] key, byte[] message,
                                    String pattern, StegoImage container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    message,
                    key,
                    pattern
            );

            encodeTask(task);
        } catch (WrongTaskException | KeyNotFoundException ignored) { /*never happens*/ }

        return container;
    }

    public static StegoImage encode(File key, File message,
                                    File pattern, StegoImage container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    new String(Files.readAllBytes(pattern.toPath()))
            );

            encodeTask(task);
        } catch (WrongTaskException | KeyNotFoundException e) { /*never happens*/ }

        return container;
    }

    public static StegoImage encode(byte[] key, byte[] message, Patterns.Type type,
                                    StegoImage container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    message,
                    key,
                    type
            );

            encodeTask(task);
        } catch (WrongTaskException | KeyNotFoundException e) { /*never happens*/ }

        return container;
    }

    public static StegoImage encode(File key, File message,
                                    Patterns.Type type, StegoImage container) throws IOException {
        try {
            EncodeTask task = new EncodeTask(
                    container,
                    Files.readAllBytes(message.toPath()),
                    Files.readAllBytes(key.toPath()),
                    type
            );

            encodeTask(task);
        } catch (WrongTaskException | KeyNotFoundException e) { /*never happens*/ }

        return container;
    }

    public static byte[] decode(byte[] key, StegoImage container) throws IOException, KeyNotFoundException {
        try {
            DecodeTask task = new DecodeTask(container, key);
            Decoder decoder = new Decoder(task.shift(), task.mZ());

            return decoder.decode(task);
        } catch (WrongTaskException e) { /*never happens*/ }

        return new byte[0];
    }

    public static byte[] decode(File key, StegoImage container) throws IOException, KeyNotFoundException {
        try {
            DecodeTask task = new DecodeTask(container, Files.readAllBytes(key.toPath()));
            Decoder decoder = new Decoder(task.shift(), task.mZ());

            return decoder.decode(task);
        } catch (WrongTaskException e) { /*never happens*/ }

        return new byte[0];
    }

    public static boolean tryKey(byte[] key, StegoImage container) throws IOException {
        DecodeTask task = new DecodeTask(container, key);
        Decoder decoder = new Decoder(task.shift(), task.mZ());

        return decoder.checkKey(key, container);
    }

    private static void encodeTask(EncodeTask task) throws WrongTaskException, IOException, KeyNotFoundException {
        TaskHandler handler = new TaskHandler(task);

        handler.process();
        handler.finish();
    }
}
