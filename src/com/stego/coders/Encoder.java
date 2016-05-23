package com.stego.coders;

import com.userspace.task.Block;

import java.awt.image.BufferedImage;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Encoder extends Coder{
    public int hideData(BufferedImage to, Block[] data, int from) {
        return from + encode(to, data, from) + 1;
    }

    private int encode(BufferedImage to, Block[] data, int from) {
        int length = to.getWidth();

        int i, j;
        for (i = 0, j = from; i < data.length; j += delta, i++) {
            to.setRGB(j % length, j / length, toEncoded(to.getRGB(j % length, j / length), data[i].value));
            switch (data[i].type) {
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

        return i;
    }
}
