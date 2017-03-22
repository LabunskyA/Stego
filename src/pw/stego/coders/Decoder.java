package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.DecodeTask;
import pw.stego.task.Task;

import java.awt.image.BufferedImage;

/**
 * Class for decoding message from container
 */
public class Decoder extends Coder {
    public Decoder(int shift, int mZ) {
        super(shift, mZ);
    }

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
            return new byte[0];

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
        final int length = image.getWidth();
        final int totalPixels = image.getWidth() * image.getHeight();

        jumpTo(from);
        for (int shift = 0, part, size = 0;;) {
            Block.Type type = Block.Type.NONE;

            while (type != Block.Type.JUMP && type != Block.Type.EOF) {
                switch (type = toBlock(part = getDataOnCursor(image))) {
                    case INV:
                        inverse();
                        break;

                    case TRANS:
                        transpose();
                        break;

                    case NONE:
                        result[size] |= (part << (shift * 2));

                        shift++;
                        if (shift > 3) {
                            size++;
                            shift = 0;
                        }
                }

                stepFwd();
            }

            switch (type) {
                case JUMP:
                    int p = extractPoint(image, totalPixels) & 0x7fffffff;
                    jumpTo(p);
                    break;

                case EOF:
                    return size;
            }
        }
    }

    private int extractPoint(BufferedImage image, int totalPixels) {
        int p = 0;

        for (int shift = 0, part; shift < 31; stepFwd(), shift += 2) {
            part = getDataOnCursor(image);
            p |= ((part & 3) << shift);
        }

//        if ((p[1] & 0x8000) != 0 && !processMark(image)) {
//            for (int i = 0; i < 4; i++)
//                stepFwd(totalPixels);
//            p[0] = -1;
//        }

        return p;
    }

    /**
     * @param image as container
     * @return false if mark is 0, true if not
     */
    private boolean processMark(BufferedImage image) {
        int part, mark = 0;

        for (int shift = 0; shift < 7; stepFwd(), shift += 2) {
            part = getDataOnCursor(image);
            mark = mark | (part << shift);
        }

        if (mark == 0)
            return false;
        mark--;

        for (int shift = 8; shift > 1; stepBwd(), shift -= 2) {
            int x = getCursorX(), y = getCursorY();
            image.setRGB(x, y, toEncoded(image.getRGB(x, y), (byte) (mark >> shift & 3)));
        }

        return true;
    }

    /**
     * @param image as container
     * @param key to search
     * @return index of first point after key
     */
    public int find(BufferedImage image, Block[] key) {
        final int length = image.getWidth(), totalPixels = image.getHeight() * length;

        for (int from = 0; from < totalPixels; from++, reset(), jumpTo(from)) {
            for (int shift = 0, id = 0, part;;) {
                boolean next = false;

                Block.Type type = Block.Type.NONE;

                int count = 0;
                while (type != Block.Type.JUMP && type != Block.Type.EOF && !next) try {
                    if (count > 5000) {
                        next = true;
                        count = 0;
                        break;
                    }

                    switch (type = toBlock(part = getDataOnCursor(image))) {
                        case INV:
                            count++;
                            inverse();
                            break;

                        case TRANS:
                            count++;
                            transpose();
                            break;

                        case NONE:
                            if (key[id++].value != part) {
                                next = true;
                                break;
                            } else if (id == key.length) {
                                stepFwd();
                                return getCursor();
                            }

                            shift += 2;
                            if (shift > 7) {
                                shift = 0;
                            }
                    }

                    stepFwd();
                } catch (ArrayIndexOutOfBoundsException e) {
                    next = true;
                }

                switch (type) {
                    case JUMP:
                        int p = extractPoint(image, totalPixels);
                        if (p == -1)
                            break;

                        jumpTo(p & 0x7fffffff);
                        break;

                    case EOF:
                        next = true;
                }

                if (next) break;
            }
        }

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
