package pw.stego;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Block class to represent data in a comfortable way before encoding
 */
public class Block {
    /**Types of blocks*/
    public enum Type {EOF, JUMP, JUMP_MARKED, INV, TRANS, NONE}

    public final Type type;
    public final byte value;

    private static final Map<String, Integer> inputsIdx = new LinkedHashMap<>();

    private Block(byte value) {
        this.value = value;
        type = Type.NONE;
    }

    private Block(Type type) {
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

    private static Type getType(byte[] arr, int from) {
        if (from + 2 >= arr.length)
            return Type.NONE;

        if (arr[from] != '<' || arr[from + 2] != '>')
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

    /**
     * @param str String to represent as blocks array
     * @param strInput ISO-8859-1 String representation message, where's str from
     * @return Blocks array from specified fullBody
     */
    public static Block[] toBlocks(String str, String strInput) {
        String[] section = str.split("<(?:i|t)>");

        int dataLen = section.length - 1;
        for (String partLength : section)
            if (partLength.length() > 0)
                dataLen += Integer.parseInt(partLength) * 4;

        return toBlocks(mergeWithInput(section, str, strInput), dataLen);
    }

    /**
     * @param data Data to represent as blocks array
     * @return Blocks array
     */
    static private Block[] toBlocks(byte[] data, int length) {
        Block[] result = new Block[length];

        for (int i = 0, j = 0; i < data.length; i++)
            for (int shift = 0; shift < 7; j++, shift += 2)
                if (Block.getType(data, i) == Type.NONE)
                    result[j] = new Block((byte) ((data[i] >> shift) & 3));
                else {
                    result[j] = new Block(Block.getType(data, i));

                    i += 2; j++;
                    break;
                }

        return result;
    }

    public static Block[] toBlocks(byte[] data) {
        return toBlocks(data, data.length * 4);
    }

    /**
     * @param section String array with data lengths between control blocks
     * @param sectStr Full section string
     * @return Pattern filled with data of in byte ISO-8859-1 representation
     */
    static private byte[] mergeWithInput(String[] section, String sectStr, String source) {
        if (!inputsIdx.containsKey(source))
            inputsIdx.put(source, 0);

        int fullIdx = 0;
        int inputIdx = inputsIdx.get(source);

        StringBuilder result = new StringBuilder();
        for (String part : section) {
            if (part.length() > 0) {
                int length = Integer.parseInt(part);

                fullIdx += part.length();
                result.append(source.substring(inputIdx, inputIdx += length));
            }

            try {
                result.append(sectStr.substring(fullIdx, fullIdx += 3));
            } catch (StringIndexOutOfBoundsException ignored) {}
        }

        inputsIdx.put(source, inputIdx);
        return result.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Extracts type of control block in the end of the section
     * @param p Index of jump block
     * @param pm Index of marked jump block
     * @return Next nearest control block type
     */
    public static Type getSectionEnd(int p, int pm) {
        if (p == pm)
            return Type.EOF;

        if (p == -1)
            return Type.JUMP_MARKED;
        if (pm == -1)
            return Type.JUMP;

        if (p < pm)
            return Type.JUMP;

        return Type.JUMP_MARKED;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
