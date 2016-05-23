package com.stego.coders;

import java.awt.image.BufferedImage;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Encoder extends Coder{
    public int hideData(BufferedImage to, byte[] data, int from) {
        return from + encode(to, data, from) + 1;
    }

    private int encode(BufferedImage to, byte[] data, int from) {
        int length = to.getWidth();

        int i, j;
        for (i = 0, j = from; i < data.length; i++)
            for (int shift = 0; shift < 7; j += delta, shift += 2) {
                to.setRGB(j % length, j / length, toEncoded(to.getRGB(j % length, j / length), data[i], shift));
                switch (toBlock(toDecoded(to.getRGB(j % length, j / length)))) {
                    case INV:
                        delta *= -1;
                        break;
                    case TRANS:
                        j += delta;

                        if (transposed)
                            delta /= length;
                        else delta *= length;
                }

                System.out.print(toDecoded(to.getRGB(j % length, j / length)));
            }

        System.out.println();

        return i * 4;
    }
}
