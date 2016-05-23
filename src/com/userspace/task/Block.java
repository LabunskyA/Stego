package com.userspace.task;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Block {
    public enum ControlBlock {EOF, URL, INV, TRANS, NONE}

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

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
