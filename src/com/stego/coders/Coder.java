package com.stego.coders;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
class Coder {
    @SuppressWarnings("WeakerAccess")
    enum ControlBlock {EOF, URL, INV, TRANS, NONE}

    int delta = 1;
    boolean transposed = false;

    int toDecoded(int a) {
        return ((a & ~0xfffeffff) >> 14) | (((a & ~0xfeff) >> 8) & 1) | ((a  & ~0xfe & 1) << 1);
    }

    int toEncoded(int a, byte data, int shift) {
        return (a & 0xfffefefe) | (((data >> shift) & 1) << 8) | (data >> ++shift & 1);
    }

    ControlBlock toBlock(int a) {
        switch (a) {
            case 4: return Decoder.ControlBlock.EOF;
            case 7: return Decoder.ControlBlock.URL;
            case 5: return Decoder.ControlBlock.INV;
            case 6: return Decoder.ControlBlock.TRANS;
        }

        return Decoder.ControlBlock.NONE;
    }
}
