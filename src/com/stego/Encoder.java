package com.stego;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
class Encoder {
    int hideData(BufferedImage to, byte[] data, int from) {
        return from + encode(to, data, from) + 1;
    }

    private int encode(BufferedImage to, byte[] data, int from){
        int length = to.getWidth();

        int i;
        for (i = 0; i < data.length; i++)
            for (int j = from + 4 * i, shift = 0; j - from - 4 * i < 4; j++, shift += 2)
                to.setRGB(j % length, j / length, toEncoded(to.getRGB(j % length, j / length), data[i], shift));

        return 4 * i;
    }

    private int toEncoded(int a, byte data, int shift) {
        return (a & 0xfffefefe) | (((data >> shift) & 1) << 8) | (data >> ++shift & 1);
    }
}
