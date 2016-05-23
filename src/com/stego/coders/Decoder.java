package com.stego.coders;

import com.userspace.task.Block;

import java.awt.image.BufferedImage;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Decoder extends Coder {
    public byte[] decode(BufferedImage image, Block[] key) {
        int length = image.getWidth();

        byte[] result = new byte[image.getHeight() * length];

        int from = find(image, key) - key.length;
        int size = decode(image, result, from, 0);

        System.out.println();

        System.arraycopy(result, 0, (result = new byte[size]), 0, size);
        return result;
    }

    private int decode(BufferedImage image, byte[] result, int from, int size) {
        int length = image.getWidth();
        System.out.println("Extracting from x = " + from % length + " y = " + from / length);

        for (int i = 0, j = from, temp = 0, part = 0;; i++, temp = 0) {
            boolean flag = true;

            for (int shift = 0; shift < 7 && flag; j += delta, shift += 2)
                switch (toBlock(part)) {
                    case URL:
                    case EOF:
                        flag = false;
                        break;
                    case INV:
                        delta *= -1;
                        break;
                    case TRANS:
                        j += delta;
                        if (transposed)
                            delta /= length;
                        else delta *= length;
                        break;
                    default:
                        part = toDecoded(image.getRGB(j % length, j / length));
                        temp = temp | ((part & 3) << shift);

                        System.out.print(part);
                }

            if (flag)
                result[size++] = (byte) temp;
            else switch (toBlock(part)) {
                case URL:
                    int p[] = new int[2];

                    System.out.println();
                    System.out.println("Extracting from x = " + (j -= delta) % length + " y = " + j / length);

                    for (int shift = 0; shift < 63; j += delta, shift += 2) {
                        part = toDecoded(image.getRGB(j % length, j / length));
                        p[shift / 32] = p[shift / 32] | ((part & 3) << (shift % 32));

                        System.out.print(part);
                    }

                    System.out.println();

                    size = decode(image, result, p[1] * length + p[0], size);
                case EOF:
                    return size;
            }
        }
    }

    private int find(BufferedImage where, Block[] what) {
        int length = where.getWidth();

        for (int i = 0; i < where.getHeight() * length; i++)
            for (int j = i; j < i + what.length; j++)
                if ((toDecoded(where.getRGB(j % length, j / length)) & 3) != ((what[j - i].value)))
                    break;
                else if (j == i + what.length - 1)
                    return what.length + i;

        return -1;
    }
}
