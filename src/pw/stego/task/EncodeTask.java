package pw.stego.task;

import pw.stego.Block;
import pw.stego.util.FString;
import pw.stego.util.Patterns;
import pw.stego.util.StegoImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Task with encode constroctors and fields
 * Created by lina on 14.09.16.
 */
public class EncodeTask extends Task {
    private final int start;
    private Block[] data;

    private int dataId = 0;
    private int controlId = 0;
    private int jump;

    private final String pattern;
    private final String[] controls;
    private final String[] counts;

    /**
     * Constructor without defined container, but with defined container type
     * @param patternType pattern distribution type cursor Patterns.Type enum
     */
    public EncodeTask(StegoImage container, byte[] message, byte[] key, Patterns.Type patternType) {
        super(Type.ENCODE, container, Block.toBlocks(key));

        this.data = new Block[this.key.length + message.length * 4];

        Block[] data = Block.toBlocks(message);
        System.arraycopy(this.key, 0, this.data, 0, this.key.length);
        System.arraycopy(data, 0, this.data, this.key.length, data.length);

        pattern = Patterns.createPattern(
                patternType,
                this.data.length,
                getImage().getWidth() * getImage().getHeight(),
                getImage().getWidth()
        );

        this.controls = pattern.split(">(([\\d].*?<)|<)");
        this.counts =  pattern.split("<([itj]).*?>");

        start = Integer.parseInt(pattern.substring(3, pattern.indexOf(">")));
    }

    /**
     * Constructor with pre-defined container
     * @param pattern in String
     */
    public EncodeTask(StegoImage container, byte[] message, byte[] key, String pattern) {
        super(Type.ENCODE, container, Block.toBlocks(key));

        this.data = new Block[this.key.length + message.length * 4];
        Block[] data = Block.toBlocks(message);

        System.arraycopy(this.key, 0, this.data, 0, this.key.length);
        System.arraycopy(data, 0, this.data, this.key.length, data.length);

        this.pattern = pattern;
        this.controls = pattern.split(">(([\\d].*?<)|<)");
        this.counts =  pattern.split("<([itj]).*?>");

        start = Integer.parseInt(pattern.substring(3, pattern.indexOf(">")));
    }

    public Block[] nextDataPart() {
        if (controlId >= controls.length)
            return new Block[0];

        /*From where do we begin?*/ {
            if (controls[controlId].contains(">"))
                controls[controlId] = FString.cutTo(controls[controlId], ">");

            String[] jump = FString.cutFrom(controls[controlId], ":").split(",");
            this.jump = Integer.parseInt(jump[0]);
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

            String[] jump = FString.cutFrom(controls[controlId], ":").split(",");
            int dest = Integer.parseInt(jump[0]);

            merged.add(new Block(Block.Type.JUMP));
            if (jump.length == 1)
                Collections.addAll(merged, Block.toBlocks(new byte[]{
                        (byte)  (dest & 0xff)           , (byte) ((dest & 0xff00    ) >> 8),
                        (byte) ((dest & 0xff0000) >> 16), (byte) ((dest & 0x7f000000) >> 24)
                }));
            else
                Collections.addAll(merged, Block.toBlocks(new byte[]{
                        (byte)  (dest & 0xff)           , (byte) ((dest & 0xff00                 ) >> 8),
                        (byte) ((dest & 0xff0000) >> 16), (byte) ((dest & 0xff000000 | 0x80000000) >> 24),
                        //mark
                        (byte) (Integer.parseInt(jump[1]) & 0xff)
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

    public int getNextJump() {
        return jump;
    }

    public int getStart() {
        return start;
    }

    public Block[] getKey() {
        return key;
    }
}
