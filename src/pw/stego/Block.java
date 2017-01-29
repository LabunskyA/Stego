package pw.stego;

import pw.stego.util.FString;

import java.nio.charset.StandardCharsets;

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
            case JUMP:
            case JUMP_MARKED:
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

    private static Type getType(char type) {
        switch (type) {
            case 'i':
                return Type.INV;
            case 't':
                return Type.TRANS;
            default:
                return Type.NONE;
        }
    }

    public static Block[] toBlocks(int count, String pattern, String[] parts, String section) {
        Block[] result = new Block[count];

        for (int i = 0, bId = 0, pId = 0; i < pattern.length();) {
            if (pattern.charAt(i) == '<') {
                result[bId++] = new Block(getType(pattern.charAt((i += 3) - 2)));
                continue;
            }

            if (parts[pId].length() == 0) {
                pId++;
                continue;
            }

            int length = Integer.parseInt(parts[pId]);
            for (byte b : FString.cutTo(section, length).getBytes(StandardCharsets.ISO_8859_1))
                for (int shift = 0; shift < 7; shift += 2)
                    result[bId++] = new Block((byte) ((b >> shift) & 3));

            section = FString.cutFrom(section, length);
            i += parts[pId++].length();
        }

        return result;
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
