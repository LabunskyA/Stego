package pw.stego.coders;

import pw.stego.Block;

import java.awt.image.BufferedImage;

/**
 * Class for encoding message to container
 */
public class Encoder extends Coder{
    public int hideData(BufferedImage to, Block[] data, int from) {
        System.out.println("Encoding from x = " + from % to.getWidth() + " y = " + from / to.getWidth());
        return encode(to, data, from);
    }

    private int encode(BufferedImage to, Block[] data, int from) {
        int length = to.getWidth();

        int i, j;
        for (i = 0, j = from; i < data.length; i++) {
            to.setRGB(j % length, j / length, toEncoded(to.getRGB(j % length, j / length), data[i].value));
            System.out.print(toDecoded(to.getRGB(j % length, j / length)));

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

        System.out.println();

        return j;
    }
}
