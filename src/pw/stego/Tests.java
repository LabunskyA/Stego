package pw.stego;

import org.junit.Assert;
import org.junit.Test;
import pw.Stego;
import pw.stego.coders.KeyNotFoundException;
import pw.stego.coders.WrongTaskException;
import pw.stego.task.DecodeTask;
import pw.stego.task.EncodeTask;
import pw.stego.util.Patterns;
import pw.stego.util.StegoImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * Global tests class
 * Created by lina on 14.09.16.
 */
public class Tests {
    @Test
    public void distributedPatternTest() throws IOException, KeyNotFoundException {
        Random r = new Random();
        StegoImage container = new StegoImage(ImageIO.read(new File("tests/garfield.png")));

        for (int i = 0; i < 1000; i++) {
            byte[] key = new byte[56];
            r.nextBytes(key);

            byte[] data = new byte[1 + r.nextInt(1000)];
            r.nextBytes(data);

            container = Stego.encode(key, data, Patterns.Type.EVENTUALY_DISTRIBUTED, container);
            byte[] result = Stego.decode(key, container);

            Assert.assertTrue(Arrays.equals(data, result));
        }
    }

    @Test
    public void correctEncodeTask() throws IOException, WrongTaskException, KeyNotFoundException {
        testTaskHandler(
                new TaskHandler(
                        new EncodeTask(
                                new StegoImage(ImageIO.read(new File("tests/garfield.png"))),
                                Files.readAllBytes(Paths.get("tests/encode/message.txt")),
                                Files.readAllBytes(Paths.get("tests/encode/key.txt")),
                                new String(
                                        Files.readAllBytes(Paths.get("tests/encode/pattern.txt")),
                                        StandardCharsets.ISO_8859_1
                                )
                        )
                )
        );
    }

    @Test
    public void tooLargeEncodeTask() {
        //todo create this shit i dont rly care how do it
    }

    @Test
    public void correctDecodeTask() throws IOException, WrongTaskException, KeyNotFoundException {
        testTaskHandler(
                new TaskHandler(new DecodeTask(
                    new StegoImage(ImageIO.read(new File("tests/garfield.png"))),
                    Files.readAllBytes(Paths.get("tests/decode/key.txt")))
                )
        );

        System.out.println(new String(Files.readAllBytes(Paths.get("tests/garfield.dec"))));
    }

    @Test
    public void testLSBNoise() throws IOException {
        BufferedImage testImage = ImageIO.read(new File("tests/garfield.png"));

        int zeros = 0;
        for (int i = 0; i < testImage.getWidth(); i++)
            for (int j = 0; j < testImage.getHeight(); j++)
                if ((((testImage.getRGB(i, j) & ~0xfeffff) >> 16) & 1) == 0)
                    zeros++;

        System.out.println("Zero red bits: " + zeros);
        System.out.println("All bits: " + testImage.getHeight() * testImage.getWidth());
        System.out.println("Percentage: " + zeros * 1.0 / (testImage.getWidth() * testImage.getHeight()));
    }

    private void testTaskHandler(TaskHandler handler) throws WrongTaskException, KeyNotFoundException, IOException {
        handler.process();
        handler.finish();
    }
}
