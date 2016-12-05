package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.EncodeTask;

import java.awt.image.BufferedImage;

/**
 * Class for encoding message to container
 */
public class Encoder extends Coder {
    private int encode(BufferedImage to, int from, Block[] data) {
        int length = to.getWidth();

        int i, j;
        for (i = 0, j = from; i < data.length; i++) {
            to.setRGB(
                    j % length, j / length,
                    toEncoded(
                            to.getRGB(j % length, j / length),
                            data[i].value
                    )
            );

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

        return j;
    }

    public boolean encode(EncodeTask task) {
        BufferedImage image = task.getImage();

        int length = image.getWidth();
        int i = 0;

        try {
            while (task.nextDataPart()) {
                i = encode(image, task.fromPoint().y * length + task.fromPoint().x, task.getData());

                if (task.getMeta() != null) {
                    image.setRGB(
                            i % length, i / length,
                            (image.getRGB(i % length, i / length) & 0xfffffefe) | 0x10101
                    );

                    encode(image, i + delta, task.getMeta());
                }
            }

            image.setRGB(
                    i % length, i / length,
                    (image.getRGB(i % length, i / length) & 0xfffffefe) | 0x10000
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
