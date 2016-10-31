package pw.stego.task;

import pw.stego.Block;
import pw.stego.Patterns;
import pw.util.FString;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Task with encode constroctors and fields
 * Created by lina on 14.09.16.
 */
public class EncodeTask extends Task {
    private final String input;
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

        System.out.println(this.pattern);
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

    /**
     * Process next section into byte array if any
     * @return true if there is data left, false if none
     */
    public Boolean nextDataPart() {
        if (!pattern.contains("<p"))
            return false;

        pattern = FString.cutFrom(pattern, "<p");
        data = Block.toBlocks(FString.getBetween(pattern, ">", "<p"), input);

        String[] temp = FString.getBetween(pattern, ":", ">").split(",");
        int[] point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};
        from = new Point(point[0], point[1]);

        switch (Block.getSectionEnd(pattern.indexOf("<p:"), pattern.indexOf("<pm:"))) {
            case JUMP:
                temp = FString.getBetween(pattern, "<p:", ">").split(",");
                point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};

                meta = Block.toBlocks(new byte[] {
                        (byte) (point[0] & 0xff), (byte) ((point[0] & 0xff00) >> 8),
                        (byte) (point[1] & 0xff), (byte) ((point[1] & 0x7f00) >> 8)
                });
                break;
            case JUMP_MARKED:
                temp = FString.getBetween(pattern, "<pm:", ">").split(",");
                point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2])};

                meta = Block.toBlocks(new byte[] {
                        (byte) (point[0] & 0xff), (byte) ((point[0] & 0xff00) >> 8),
                        (byte) (point[1] & 0xff), (byte) ((point[1] & 0xff00 | 0x8000) >> 8),
                        (byte) (point[2]  & 0xff)
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

    public Point getFrom() {
        return from;
    }
}
