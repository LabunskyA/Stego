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

        for (int j = from, temp = 0, part = 0;; temp = 0) {
            boolean flag = true;

            for (int shift = 0; shift < 7 && flag; j += delta, shift += 2) {
                switch (toBlock(part = toDecoded(image.getRGB(j % length, j / length)))) {
                    case INV:
                        delta *= -1;
                        flag = false;
                        break;
                    case TRANS:
                        if (transposed)
                            delta /= length;
                        else {
                            j += delta;
                            delta *= length;
                            j -= delta;
                        }
                        transposed = !transposed;
                    case URL:
                    case EOF:
                        flag = false;
                        break;
                    case NONE:
                        temp = temp | ((part & 3) << shift);
                }

                System.out.print(part);
            }

            if (flag)
                result[size++] = (byte) temp;
            else switch (toBlock(part)) {
                case URL:
                    int tempSize;
                    if ((tempSize = extractPoint(image, j, result, size)) != -1)
                        size = tempSize;
                    else {
                        j += delta * 4;
                        break;
                    }
                case EOF:
                    return size;
            }
        }
    }

    private int extractPoint(BufferedImage image, int j, byte[] result, int size) {
        int length = image.getWidth(), part;
        int p[] = new int[2];

        System.out.println();
        System.out.println("Extracting from x = " + j % length + " y = " + j / length);

        for (int shift = 0; shift < 31; j += delta, shift += 2) {
            part = toDecoded(image.getRGB(j % length, j / length));
            p[shift / 16] = p[shift / 16] | ((part & 3) << (shift % 16));

            System.out.print(part);
        }

        if ((p[1] & 0x8000) != 0)
            if (!processMark(image, j))
                return -1;
        p[1] &= 0x7fff;

        System.out.println();

        return decode(image, result, p[1] * length + p[0], size);
    }

    private boolean processMark(BufferedImage image, int j) {
        int length = image.getWidth(), part, mark = 0;
        for (int shift = 0; shift < 7; j += delta, shift += 2) {
            part = toDecoded(image.getRGB(j % length, j / length));
            mark = mark | (part << shift);
        }

        if (mark == 0)
            return false;

        mark--;
        for (int shift = 6; shift > -1; j -= delta, shift -= 2) {
            int x = j % length, y = j / length;
            image.setRGB(x, y, toEncoded(image.getRGB(x, y), (byte) (mark >> shift & 3)));
        }

        return true;
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
