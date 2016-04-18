package com.stego;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
class Decoder {
    byte[] decode(BufferedImage image, byte[] key){
        int length = image.getWidth();

        byte[] result = new byte[image.getHeight() * length];

        int from = find(image, key);
        System.out.println("Extracting from x = " + (from - key.length * 4) % length +
                                        " y = " + (from - key.length * 4) / length);

        int size = 0;
        for (int i = 0, j, k, shift, temp = 0, part = 0;; i++, temp = 0) {
            for (j = from + 4 * i, shift = 0; j - from - 4 * i < 4 && part != 4 && part != 7; j++, shift += 2) {
                part = toDecoded(image.getRGB(j % length, j / length));
                temp = temp | ((part & 3) << shift);
            }

            if (part != 4 && part != 7)
                result[size] = (byte) temp;
            else if (part == 7) {
                int p[] = new int[2];
                for (k = j + 1, shift = 0; k < j + 33; k++, shift = (shift + 2) % 8) {
                    part = toDecoded(image.getRGB(k % length, k / length));
                    p[(k - j - 1) / 16] = p[(k - j - 1) / 16] | ((part & 3) << shift);
                }

                System.out.println(Arrays.toString(p));

                from = p[1] * length + p[0];
                i = -1;
            } else break;

            size++;
        }
        System.out.println();

        System.arraycopy(result, 0, (result = new byte[size]), 0, size);
        return result;
    }

    private int toDecoded(int a) {
        return ((a & ~0xfffeffff) >> 14) | (((a & ~0xfeff) >> 8) & 1) | ((a  & ~0xfe & 1) << 1);
    }

    private int find(BufferedImage where, byte[] what) {
        int length = where.getWidth();

        for (int i = 0; i < where.getHeight() * length; i++)
            for (int j = i, shift = 0; j < i + what.length * 4; j++, shift = (shift + 2) % 8)
                if ((toDecoded(where.getRGB(j % length, j / length)) & 3) != ((what[(j - i) / 4] & (3 << shift)) >> shift))
                    break;
                else if (j == i + what.length - 1)
                    return what.length * 4 + i;


        return -1;
    }
}
