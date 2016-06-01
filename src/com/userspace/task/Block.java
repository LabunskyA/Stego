package com.userspace.task;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Block {
    public enum ControlBlock {EOF, URL, URL_M, INV, TRANS, NONE}

    public final ControlBlock type;
    public final byte value;

    Block(byte value) {
        this.value = value;
        type = ControlBlock.NONE;
    }

    Block(ControlBlock type) {
        this.type = type;
        switch (type) {
            case EOF:
                value = 4;
                break;
            case URL:
                value = 7;
                break;
            case INV:
                value = 5;
                break;
            case TRANS:
                value = 6;
                break;
            //should not happen
            default:
            case NONE:
                value = 0;
        }
    }

    static ControlBlock toControl(byte[] arr, int from) {
        if (!(arr[from] == '<' && arr[from + 2] == '>'))
            return ControlBlock.NONE;

        switch (arr[from + 1]) {
            case 'i':
                return ControlBlock.INV;
            case 't':
                return ControlBlock.TRANS;
            default:
                return ControlBlock.NONE;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
