package pw.stego;

/**
 * Block class to represent data in a comfortable way before encoding
 */
public class Block {
    /**Types of blocks*/
    public enum Type {EOF, JUMP, JUMP_MARKED, INV, TRANS, NONE}

    public final Type type;
    public final byte value;

    Block(byte value) {
        this.value = value;
        type = Type.NONE;
    }

    Block(Type type) {
        this.type = type;
        switch (type) {
            case EOF:
                value = 4;
                break;
            case JUMP:
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

    static Type toControl(byte[] arr, int from) {
        if (from + 2 <= arr.length)
            return Type.NONE;

        if (!(arr[from] == '<' && arr[from + 2] == '>'))
            return Type.NONE;

        switch (arr[from + 1]) {
            case 'i':
                return Type.INV;
            case 't':
                return Type.TRANS;
            default:
                return Type.NONE;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
