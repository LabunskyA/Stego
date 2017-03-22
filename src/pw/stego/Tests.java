package pw.stego;

import org.junit.Assert;
import org.junit.Test;
import pw.Stego;
import pw.stego.coders.WrongTaskException;
import pw.stego.task.DecodeTask;
import pw.stego.task.EncodeTask;
import pw.stego.util.Patterns;

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
    public void correctCDecode() throws WrongTaskException, IOException {
        for (int i = 0; i < 2; i++)
            Files.write(
                    Paths.get("/home/lina/steg/test-" + i + ".dec"),
                    Stego.decode("\2\3\4\5\6".getBytes(), new File("/home/lina/steg/test-" + i + ".png"))
            );
    }

    @Test
    public void correctEncodeTask() throws IOException, WrongTaskException {
        Assert.assertTrue(
                testTaskHandler(
                        new TaskHandler(
                                new EncodeTask(
                                    new File("tests/garfield.png"),
                                    Files.readAllBytes(Paths.get("tests/encode/message.txt")),
                                    Files.readAllBytes(Paths.get("tests/encode/key.txt")),
                                    new String(
                                            Files.readAllBytes(Paths.get("tests/encode/pattern.txt")),
                                            StandardCharsets.ISO_8859_1
                                    )
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
    public void correctDecodeTask() throws IOException, WrongTaskException {
        Assert.assertTrue(
                testTaskHandler(new TaskHandler(new DecodeTask(
                        new File("tests/garfield.png"),
                        Files.readAllBytes(Paths.get("tests/decode/key.txt"))))
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

    private boolean testTaskHandler(TaskHandler handler) throws WrongTaskException {
        return handler.process() != null && handler.writeResult();
    }

    @Test
    public void sizeTest() throws IOException, WrongTaskException {
        Random r = new Random();

        byte[] doc = new byte[18 * 1000 * 1024];
        r.nextBytes(doc);

        testTaskHandler(new TaskHandler(new EncodeTask(
                new File("tests/BIG.png"),
                doc,
                Files.readAllBytes(Paths.get("tests/encode/key.txt")),
                Patterns.Type.SIMPLE
        )));

        testTaskHandler(new TaskHandler(new DecodeTask(
                    new File("tests/BIG.png"),
                    Files.readAllBytes(Paths.get("tests/decode/key.txt"))
        )));

        Assert.assertTrue(Arrays.equals(doc, Files.readAllBytes(Paths.get("tests/BIG.dec"))));
    }
}
