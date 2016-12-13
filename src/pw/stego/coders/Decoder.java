package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.DecodeTask;
import pw.stego.task.Task;

import java.awt.image.BufferedImage;

/**
 * Class for decoding message from container
 */
public class Decoder extends Coder {
    /**
     * @param decodeTask to process
     * @return all hidden data in byte array. Null in case if wrong key specified
     * @throws WrongTaskException on wrong task argument type
     */
    public byte[] decode(Task decodeTask) throws WrongTaskException {
        if (!(decodeTask instanceof DecodeTask))
            throw new WrongTaskException();

        DecodeTask task = (DecodeTask) decodeTask;

        BufferedImage image = task.getImage();
        Block[] key = task.getKey();

        int length = image.getWidth();
        byte[] result = new byte[image.getHeight() * length / 4];

        int from = find(image, key);
        if (from == -1)
            return null;
        int size = decode(image, result, from);

        System.arraycopy(result, 0, (result = new byte[size]), 0, size);
        return result;
    }

    /**
     * @param image as container
     * @param result array to write result to
     * @param from this point function fill work
     * @return count of extracted bytes
     */
    private int decode(BufferedImage image, byte[] result, int from) {
        int size = 0;
        int length = image.getWidth();

        for (int cursor = from, temp = 0, part = 0;; temp = 0) {
            boolean control = false;

            for (int shift = 0; shift < 7 && !control; cursor += delta, shift += 2)
                switch (toBlock(part = toDecoded(image.getRGB(cursor % length, cursor / length)))) {
                    case INV:
                        delta *= -1;
                        control = true;
                        break;

                    case TRANS:
                        if (transposed)
                            delta /= length;
                        else {
                            cursor += delta;
                            delta *= length;
                            cursor -= delta;
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
                    int p[] = new int[2];

                    for (int shift = 0; shift < 31; cursor += delta, shift += 2) {
                        part = toDecoded(image.getRGB(cursor % length, cursor / length));
                        p[shift / 16] = p[shift / 16] | ((part & 3) << (shift % 16));
                    }

                    if ((p[1] & 0x8000) != 0)
                        if (!processMark(image, cursor)) {
                            cursor += delta * 4;
                            break;
                        }

                    cursor = (p[1] & 0x7fff) * length + p[0];
                    break;

                case EOF:
                    return size;
            } else result[size++] = (byte) temp;
        }
    }

    /**
     * @param image as container
     * @param p point from
     * @return false if mark is 0, true if not
     */
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

    /**
     * @param image as container
     * @param key to search
     * @return index of first point after key
     */
    private int find(BufferedImage image, Block[] key) {
        int length = image.getWidth();

        for (int i = 0; i < image.getHeight() * length; i++)
            for (int j = i; j < i + key.length; j++)
                if ((toDecoded(image.getRGB(j % length, j / length)) & 3) != ((key[j - i].value)))
                    break;
                else if (j == i + key.length - 1)
                    return key.length + i;

        return -1;
    }

    /**
     * @param key as byte array
     * @param container to check in
     * @return true if container contains key
     */
    public boolean checkKey(byte[] key, BufferedImage container) {
        return find(container, Block.toBlocks(key)) != -1;
    }
}
