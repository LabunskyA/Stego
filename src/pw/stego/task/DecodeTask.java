package pw.stego.task;

import pw.stego.Block;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Task with decode constructor
 * Created by lina on 14.09.16.
 */
public class DecodeTask extends Task {
    private final String key;

    public DecodeTask(File container, byte[] key) {
        super(container, readBI(container));
        type = Type.DECODE;

        this.key = new String(key, StandardCharsets.ISO_8859_1);
    }

    public Block[] getKey() {
        if (key.length() == 0)
            return null;

        return Block.toBlocks(key.getBytes(StandardCharsets.ISO_8859_1));
    }
}
