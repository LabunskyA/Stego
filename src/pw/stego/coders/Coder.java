package pw.stego.coders;

import pw.stego.Block;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
class Coder {
    public int delta = 1;
    boolean transposed = false;

    int toDecoded(int a) {
        return (((a & ~0xfeffff) >> 16) & 1) << 2 | (((a & ~0xfeff) >> 8) & 1) << 1 | a & ~0xfe & 1;
    }

    int toEncoded(int a, byte data) {
        return (a & 0xfffefefe) | ((data & 4) << 14) | ((data & 2) << 7) | (data & 1);
    }

    Block.ControlBlock toBlock(int a) {
        switch (a) {
            case 4: return Block.ControlBlock.EOF;
            case 5: return Block.ControlBlock.INV;
            case 6: return Block.ControlBlock.TRANS;
            case 7: return Block.ControlBlock.URL;
        }

        return Block.ControlBlock.NONE;
    }
}
