package pw.stego.coders;

import pw.stego.Block;

/**
 * Base class for encoding and decoding
 */
class Coder {
    int delta = 1;
    boolean transposed = false;

    int toDecoded(int a) {
        return (((a & ~0xfeffff) >> 16) & 1) << 2 | (((a & ~0xfeff) >> 8) & 1) << 1 | a & ~0xfe & 1;
    }

    int toEncoded(int a, byte data) {
        return (a & 0xfffefefe) | ((data & 4) << 14) | ((data & 2) << 7) | (data & 1);
    }

    Block.Type toBlock(int a) {
        switch (a) {
            case 4: return Block.Type.EOF;
            case 5: return Block.Type.INV;
            case 6: return Block.Type.TRANS;
            case 7: return Block.Type.JUMP;
        }

        return Block.Type.NONE;
    }
}
