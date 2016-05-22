package com.stego;

import java.awt.image.BufferedImage;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
class Decoder {
    private enum ControlBlock {EOF, URL, INV, TRANS, NONE}

    private int delta = 1;
    private boolean transposed = false;

    byte[] decode(BufferedImage image, byte[] key) {
        int length = image.getWidth();

        byte[] result = new byte[image.getHeight() * length];

        int from = find(image, key) - key.length * 4;
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

            for (int shift = 0; j - from < 4 + 4 * i && flag; j++, shift += 2)
                switch (toBlock(part)) {
                    case URL:
                    case EOF:
                        flag = false;
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
                    System.out.println("Extracting from x = " + --j % length + " y = " + j / length);

                    for (int k = j, shift = 0; k < j + 32; k++, shift = (shift + 2) % 8) {
                        part = toDecoded(image.getRGB(k % length, k / length));
                        p[(k - j) / 16] = p[(k - j) / 16] | ((part & 3) << shift);

                        System.out.print(part);
                    }

                    System.out.println();

                    size = decode(image, result, p[1] * length + p[0], size);
                default:
                    return size;
            }
        }
    }

    private ControlBlock toBlock(int a) {
        switch (a) {
            case 4: return ControlBlock.EOF;
            case 7: return ControlBlock.URL;
            case 5: return ControlBlock.INV;
            case 6: return ControlBlock.TRANS;
        }

        return ControlBlock.NONE;
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
