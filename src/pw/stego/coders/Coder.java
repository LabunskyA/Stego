package pw.stego.coders;

import pw.stego.Block;
import pw.stego.util.StegoImage;


/**
 * Base class for encoding and decoding
 */
class Coder {
    private boolean transposed = false;
    private boolean inverted = false;

    private int cursor = 0;
    private int delta = 1;

    private final int mW;

    private final int mZ;

    static int toDecoded(int a) {
        return  (((a & ~0xfeffff) >> 16) & 1) << 2 |
                (((a & ~0xfeff  ) >> 8)  & 1) << 1 |
                   a & ~0xfe             & 1;
    }

    static int toEncoded(int a, byte data) {
        return (a & 0xfffefefe) | ((data & 4) << 14) |
                                  ((data & 2) << 7) |
                                   (data & 1);
    }

    static Block.Type toBlock(int a) {
        switch (a) {
            case 4: return Block.Type.EOF;
            case 5: return Block.Type.INV;
            case 6: return Block.Type.TRANS;
            case 7: return Block.Type.JUMP;
        }

        return Block.Type.NONE;
    }

    Coder(int mW, int mZ) {
        this.mW = mW;
        this.mZ = mZ;
    }

    void reset() {
        transposed = false;
        inverted = false;
        delta = 1;
    }

    private static int modInc(int i, int delta, int module) {
        return (module + i + delta) % module;
    }

    private static int modDec(int i, int delta, int module) {
        return (module + i - delta) % module;
    }

    void stepFwd() {
        int nextPos = modInc(cursor, delta, mZ);
        if (transposed) {
            int y = cursor / mW;
            int yn = nextPos / mW;

            if (!inverted && yn < y)
                nextPos = modInc(nextPos, 1, mZ);
            else if (inverted && yn > y)
                nextPos = modInc(nextPos, -1, mZ);
        }

        cursor = nextPos;
    }

    void stepBwd() {
        int nextPos = modDec(cursor, delta, mZ);
        if (transposed) {
            int y = cursor / mW;
            int yn = nextPos / mW;

            if (!inverted && yn > y)
                nextPos = modDec(nextPos, 1, mZ);
            else if (inverted && yn < y)
                nextPos = modDec(nextPos, -1, mZ);
        }

        cursor = nextPos;
    }

    void jumpTo(int destination) {
        cursor = destination;
    }

    int getDataOnCursor(StegoImage image) {
        return toDecoded(image.getRGB(getCursorX(), getCursorY()));
    }

    void setDataOnCursor(StegoImage image, byte value) {
        image.setRGB(
                getCursorX(), getCursorY(),
                toEncoded(
                        image.getRGB(getCursorX(), getCursorY()),
                        value
                )
        );
    }

    int getCursorX() {
        return cursor % mW;
    }

    int getCursorY() {
        return cursor / mW;
    }

    int getCursor() {
        return cursor;
    }

    void transpose() {
        if (!transposed)
            delta *= mW;
        else delta /= mW;

        transposed = !transposed;
    }

    void inverse() {
        delta = -delta;
        inverted = !inverted;
    }
}
