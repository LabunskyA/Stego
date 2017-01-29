package pw.stego.task;

import pw.stego.Block;
import pw.stego.util.FString;
import pw.stego.util.Patterns;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Task with encode constroctors and fields
 * Created by lina on 14.09.16.
 */
public class EncodeTask extends Task {
    private final int start;
    private Block[] key;
    private Block[] data;

    private int dataId = 0;
    private int controlId = 0;

    private final String pattern;
    private final String[] controls;
    private final String[] counts;

    private final int[] jump = new int[2];

    /**
     * Constructor without defined container, but with defined container type
     * @param patternType pattern distribution type cursor Patterns.Type enum
     */
    public EncodeTask(File container, byte[] message, byte[] key, Patterns.Type patternType) {
        super(container, readBI(container));
        type = Type.ENCODE;

        this.key = Block.toBlocks(key);
        this.data = Block.toBlocks((new String(key) + new String(message)).getBytes(StandardCharsets.ISO_8859_1));

        pattern = Patterns.createPattern(
                patternType,
                data.length,
                new Point(getImage().getWidth(), getImage().getHeight())
        );
        this.controls = pattern.split(">(([0-9].*?<)|<)");
        this.counts =  pattern.split("<(i|t|j).*?>");

        start = Integer.parseInt(pattern.substring(3, pattern.indexOf(","))) +
                Integer.parseInt(pattern.substring(
                        pattern.indexOf(",") + 1,
                        pattern.indexOf(">")
                )) * getImage().getWidth() + this.key.length;
    }

    /**
     * Constructor with pre-defined container
     * @param pattern in String
     */
    public EncodeTask(File container, byte[] message, byte[] key, String pattern) {
        super(container, readBI(container));
        type = Type.ENCODE;

        this.key = Block.toBlocks(key);
        this.data = Block.toBlocks((new String(key) + new String(message)).getBytes(StandardCharsets.ISO_8859_1));

        this.pattern = pattern;
        this.controls = pattern.split(">(([0-9].*?<)|<)");
        this.counts =  pattern.split("<(i|t|j).*?>");

        start = Integer.parseInt(pattern.substring(3, pattern.indexOf(","))) +
                Integer.parseInt(pattern.substring(
                        pattern.indexOf(",") + 1,
                        pattern.indexOf(">")
                )) * getImage().getWidth() + this.key.length;
    }

    public Block[] nextDataPart() {
        if (controlId >= controls.length)
            return new Block[0];

        /*From where do we begin?*/ {
            if (controls[controlId].contains(">"))
                controls[controlId] = FString.cutTo(controls[controlId], ">");

            String[] coords = FString.cutFrom(controls[controlId], ":").split(",");

            jump[0] = Integer.parseInt(coords[0]);
            jump[1] = Integer.parseInt(coords[1]);
        }

        int from = ++controlId;
        while (controlId < controls.length && controls[controlId].charAt(0) != 'j')
            controlId++;
        int to = controlId;

        List<Block> merged = new ArrayList<>(100);
        for (int i = from; i < to && i < counts.length; i++) {
            if (counts[i].length() > 0)
                for (int j = 0, count = Integer.parseInt(counts[i]); j < count; j++)
                    merged.add(data[dataId++]);
            
            switch (controls[i].charAt(0)) {
                case 'i':
                    merged.add(new Block(Block.Type.INV));
                    break;

                case 't':
                    merged.add(new Block(Block.Type.TRANS));
                    break;
            }
        }

        if (to < counts.length && counts[to].length() > 0)
            for (int j = 0, count = Integer.parseInt(counts[to]); j < count; j++)
                merged.add(data[dataId++]);

        if (to < controls.length) {
            if (controls[controlId].contains(">"))
                controls[controlId] = FString.cutTo(controls[controlId], ">");

            String[] coords = FString.cutFrom(controls[controlId], ":").split(",");
            int[] jump = new int[coords.length];

            for (int i = 0; i < coords.length; i++)
                jump[i] = Integer.parseInt(coords[i]);

            merged.add(new Block(Block.Type.JUMP));
            if (jump.length == 2)
                Collections.addAll(merged, Block.toBlocks(new byte[]{
                        (byte) (jump[0] & 0xff), (byte) ((jump[0] & 0xff00) >> 8),
                        (byte) (jump[1] & 0xff), (byte) ((jump[1] & 0x7f00) >> 8)
                }));
            else
                Collections.addAll(merged, Block.toBlocks(new byte[]{
                        (byte) (jump[0] & 0xff), (byte) ((jump[0] & 0xff00)          >> 8),
                        (byte) (jump[1] & 0xff), (byte) ((jump[1] & 0xff00 | 0x8000) >> 8),
                        (byte) (jump[2] & 0xff)
            }));
        } else merged.add(new Block(Block.Type.EOF));

        return fromList(merged);
    }

    private Block[] fromList(List<Block> blocks) {
        Block[] result = new Block[blocks.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = blocks.get(i);
        return result;
    }

    public int[] getNextJump() {
        return jump;
    }

    public int getStart() {
        return start;
    }

    public Block[] getKey() {
        return key;
    }
}
