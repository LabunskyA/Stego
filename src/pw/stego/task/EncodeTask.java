package pw.stego.task;

import pw.stego.Block;
import pw.stego.util.FString;
import pw.stego.util.Patterns;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Task with encode constroctors and fields
 * Created by lina on 14.09.16.
 */
public class EncodeTask extends Task {
    private String input;
    private String pattern;

    private Block[] data;
    private Block[] meta;

    private Point from;

    /**
     * Constructor without defined container, but with defined container type
     * @param patternType pattern distribution type from Patterns.Type enum
     */
    public EncodeTask(File container, byte[] message, byte[] key, Patterns.Type patternType) {
        super(container, readBI(container));
        type = Type.ENCODE;

        input = new String(key, StandardCharsets.ISO_8859_1) + new String(message, StandardCharsets.ISO_8859_1);
        pattern = Patterns.createPattern(
                patternType,
                input.length(),
                new Point(getImage().getWidth(), getImage().getHeight())
        );
    }

    /**
     * Constructor with pre-defined container
     * @param pattern in String
     */
    public EncodeTask(File container, byte[] message, byte[] key, String pattern) {
        super(container, readBI(container));
        type = Type.ENCODE;

        this.input = new String(key, StandardCharsets.ISO_8859_1) + new String(message, StandardCharsets.ISO_8859_1);
        this.pattern = pattern;
    }

    private int countBlocks(String[] parts) {
        int blocks = parts.length - 1;
        for (String part : parts)
            if (part.length() > 0)
                blocks += Integer.parseInt(part) * 4;
        return blocks;
    }

    /**
     * Process next section into byte array if any
     * @return true if there is data left, false if none
     */
    public Boolean nextDataPart() {
        if (!pattern.contains("<j"))
            return false;

        pattern = FString.cutFrom(pattern, "<j");
        String patternPart = FString.getBetween(pattern, ">", "<j");

        String[] parts = patternPart.split("<(?:i|t)>");
        int blocks = countBlocks(parts);

        String section = FString.cutTo(input, (blocks - (parts.length - 1)) / 4);
        input = FString.cutFrom(input, (blocks - (parts.length - 1)) / 4);

        data = Block.toBlocks(blocks, patternPart, parts, section);

        String[] coords = FString.getBetween(pattern, ":", ">").split(",");
        from = new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));

        switch (Block.getSectionEnd(pattern.indexOf("<j:"), pattern.indexOf("<jm:"))) {
            case JUMP:
                coords = FString.getBetween(pattern, "<j:", ">").split(",");
                int[] point = new int[]{
                        Integer.parseInt(coords[0]), Integer.parseInt(coords[1])
                };

                meta = Block.toBlocks(new byte[] {
                        (byte) (point[0] & 0xff), (byte) ((point[0] & 0xff00) >> 8),
                        (byte) (point[1] & 0xff), (byte) ((point[1] & 0x7f00) >> 8)
                });
                break;

            case JUMP_MARKED:
                coords = FString.getBetween(pattern, "<jm:", ">").split(",");
                int[] markedPoint = new int[]{
                        Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])
                };

                meta = Block.toBlocks(new byte[] {
                        (byte) (markedPoint[0] & 0xff), (byte) ((markedPoint[0] & 0xff00) >> 8),
                        (byte) (markedPoint[1] & 0xff), (byte) ((markedPoint[1] & 0xff00 | 0x8000) >> 8),
                        (byte) (markedPoint[2]  & 0xff)
                });
                break;

            default:
                meta = null;
        }

        return true;
    }

    public Block[] getData() {
        return data;
    }

    public Block[] getMeta() {
        return meta;
    }

    public Point fromPoint() {
        return from;
    }
}
