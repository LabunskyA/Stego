package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.EncodeTask;

import java.awt.image.BufferedImage;

/**
 * Class for encoding message to container
 */
public class Encoder extends Coder {
    private int hideData(BufferedImage to, Block[] data, int from) {
//        System.out.println("Encoding from x = " + from % to.getWidth() + " y = " + from / to.getWidth());

        int length = to.getWidth();

        int i, j;
        for (i = 0, j = from; i < data.length; i++) {
            to.setRGB(j % length, j / length, toEncoded(to.getRGB(j % length, j / length), data[i].value));
//            System.out.print(toDecoded(to.getRGB(j % length, j / length)));

            switch (data[i].type) {
                case TRANS:
                    if (transposed) {
                        delta /= length;
                        j += delta;
                    } else {
                        j += delta;
                        delta *= length;
                    }
                    transposed = !transposed;
                    break;

                case INV:
                    delta *= -1;
                default:
                    j += delta;
            }
        }

//        System.out.println();

        return j;
    }

    public boolean encode(EncodeTask task) {
        BufferedImage image = task.getImage();

        int length = image.getWidth();
        int i = 0;

        try {
            while (task.nextDataPart()) {
                i = hideData(image, task.getData(), task.getFrom().y * length + task.getFrom().x);

                if (task.getMeta() != null) {
                    image.setRGB(i % length, i / length, (image.getRGB(i % length, i / length) & 0xfffffefe) |
                            0x10101);

                    hideData(image, task.getMeta(), i + delta);
                }
            }

//            System.out.println("Encoding from x = " + i % length + " y = " + i / length);
            image.setRGB(i % length, i / length, (image.getRGB(i % length, i / length) & 0xfffffefe) |
                    0x10000);
//            System.out.println(4);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
