package pw.stego;

/**
 * Block class to represent data in a comfortable way before encoding
 */
public class Block {
    /**Types of blocks*/
    public enum Type {EOF, JUMP, JUMP_MARKED, INV, TRANS, NONE}

    public final Type type;
    public final byte value;

    private Block(byte value) {
        this.value = value;
        type = Type.NONE;
    }

    public Block(Type type) {
        this.type = type;
        switch (type) {
            case EOF:
                value = 4;
                break;

            case INV:
                value = 5;
                break;

            case TRANS:
                value = 6;
                break;

            case JUMP:
            case JUMP_MARKED:
                value = 7;
                break;

            //should not happen
            default:
            case NONE:
                value = 0;
        }
    }

    public static Block[] toBlocks(byte[] data) {
        Block[] result = new Block[data.length * 4];

        for (int i = 0, id = 0; i < data.length; i++)
            for (int shift = 0; shift < 7; shift += 2)
                result[id++] = new Block((byte) ((data[i] >> shift) & 3));

        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
