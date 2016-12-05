package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.DecodeTask;

import java.awt.image.BufferedImage;

/**
 * Class for decoding message from container
 */
public class Decoder extends Coder {
    public byte[] decode(DecodeTask task) {
        BufferedImage image = task.getImage();
        Block[] key = task.getKey();

        int length = image.getWidth();
        byte[] result = new byte[image.getHeight() * length / 4];

        int from = find(image, key);
        if (from == -1)
            return null;
        int size = decode(image, result, from, 0);

        System.arraycopy(result, 0, (result = new byte[size]), 0, size);
        return result;
    }

    private int decode(BufferedImage image, byte[] result, int from, int size) {
        int length = image.getWidth();

        for (int j = from, temp = 0, part = 0;; temp = 0) {
            boolean control = false;

            for (int shift = 0; shift < 7 && !control; j += delta, shift += 2)
                switch (toBlock(part = toDecoded(image.getRGB(j % length, j / length)))) {
                    case INV:
                        delta *= -1;
                        control = true;
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

                    case JUMP:
                    case EOF:
                        control = true;
                        break;

                    case NONE:
                        temp = temp | ((part & 3) << shift);
                }

            if (control) switch (toBlock(part)) {
                case JUMP:
                    int tSize = extractPoint(image, j, result, size);
                    if (tSize == -1) {
                        j += delta * 4;
                        break;
                    } else size = tSize;

                case EOF:
                    return size;
            } else result[size++] = (byte) temp;
        }
    }

    private int extractPoint(BufferedImage image, int j, byte[] result, int size) {
        int length = image.getWidth(), part;
        int p[] = new int[2];

        for (int shift = 0; shift < 31; j += delta, shift += 2) {
            part = toDecoded(image.getRGB(j % length, j / length));
            p[shift / 16] = p[shift / 16] | ((part & 3) << (shift % 16));
        }

        if ((p[1] & 0x8000) != 0)
            if (!processMark(image, j))
                return -1;
        p[1] &= 0x7fff;

        return decode(image, result, p[1] * length + p[0], size);
    }

    private boolean processMark(BufferedImage image, int p) {
        int length = image.getWidth(), part, mark = 0;

        for (int shift = 0; shift < 7; p += delta, shift += 2) {
            part = toDecoded(image.getRGB(p % length, p / length));
            mark = mark | (part << shift);
        }

        if (mark == 0)
            return false;
        mark--;

        for (int shift = 8; shift > 1; p -= delta, shift -= 2) {
            int x = p % length, y = p / length;
            image.setRGB(x, y, toEncoded(image.getRGB(x, y), (byte) (mark >> shift & 3)));
        }

        return true;
    }

    private int find(BufferedImage where, Block[] key) {
        int length = where.getWidth();

        for (int i = 0; i < where.getHeight() * length; i++)
            for (int j = i; j < i + key.length; j++)
                if ((toDecoded(where.getRGB(j % length, j / length)) & 3) != ((key[j - i].value)))
                    break;
                else if (j == i + key.length - 1)
                    return key.length + i;

        return -1;
    }

    public boolean checkKey(byte[] key, BufferedImage container) {
        return find(container, Block.toBlocks(key)) != -1;
    }
}
