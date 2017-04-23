package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.DecodeTask;
import pw.stego.task.Task;
import pw.stego.util.StegoImage;


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
    public byte[] decode(Task decodeTask) throws WrongTaskException, KeyNotFoundException {
        if (!(decodeTask instanceof DecodeTask))
            throw new WrongTaskException();

        DecodeTask task = (DecodeTask) decodeTask;

        StegoImage image = task.getImage();
        Block[] key = task.getKey();

        int length = image.getWidth();
        byte[] result = new byte[image.getHeight() * length / 4];

        int from = find(image, key, -1);
        if (from == -1)
            throw new KeyNotFoundException("Key now found in provided container.");

        int size = decode(image, result, getCursor());
        System.arraycopy(result, 0, (result = new byte[size]), 0, size);

        return result;
    }

    /**
     * @param image as container
     * @param result array to write result to
     * @param from this point function fill work
     * @return count of extracted bytes
     */
    private int decode(StegoImage image, byte[] result, int from) {
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
                        result[size] |= (part << shift);

                        shift += 2;
                        if (shift > 7) {
                            size++;
                            shift = 0;
                        }
                }

                stepFwd();
            }

            switch (type) {
                case JUMP:
                    int p = extractPoint(image) & 0x7fffffff;
                    jumpTo(p);
                    break;

                case EOF:
                    return size;
            }
        }
    }

    private int extractPoint(StegoImage image) {
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
    private boolean processMark(StegoImage image) {
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
    public int find(StegoImage image, Block[] key, int start) {
        final int totalPixels = image.getHeight() * image.getWidth();

        jumpTo(0);
        for (int from = 0; from < totalPixels; from++, reset(), jumpTo(from)) {
            for (int shift = 0, id = 0, controls = 0, part;;) {
                boolean next = false;

                Block.Type type = Block.Type.NONE;
                while (type != Block.Type.JUMP && type != Block.Type.EOF && !next) {
                    if (controls > Math.pow(key.length, 2)) {
                        if (start == from)
                            System.out.println("sdf");
                        next = true;
                        break;
                    }

                    try {
                        part = getDataOnCursor(image);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        next = true;
                        break;
                    }

                    switch (type = toBlock(part)) {
                        case INV:
                            controls++;
                            inverse();
                            break;

                        case TRANS:
                            controls++;
                            transpose();
                            break;

                        case NONE:
                            if (key[id++].value != part) {
                                next = true;
                                break;
                            } else if (id == key.length) {
                                if (start == from)
                                    return -1;

                                stepFwd();
                                return getCursor();
                            }

                            shift += 2;
                            if (shift > 7) {
                                shift = 0;
                            }
                    }

                    stepFwd();
                }

                switch (type) {
                    case JUMP:
                        controls++;
                        int p = extractPoint(image);
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

        reset();
        return -1;
    }

    public void clearFalseKeys(StegoImage container, Block[] key, int start) {
        int firstKey;
        while ((firstKey = find(container, key, start)) != start && firstKey > -1) {
            jumpTo(firstKey);
            stepBwd();
            
            setDataOnCursor(container, (byte) (getDataOnCursor(container) ^ 1));
            reset();
        }
    }

    /**
     * @param key as byte array
     * @param container to check in
     * @return true if container contains key
     */
    public boolean checkKey(byte[] key, StegoImage container) {
        return find(container, Block.toBlocks(key), -1) != -1;
    }
}
