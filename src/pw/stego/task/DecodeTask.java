package pw.stego.task;

import pw.stego.Block;

import java.io.File;

/**
 * Task with decode constructor
 * Created by lina on 14.09.16.
 */
public class DecodeTask extends Task {
    public DecodeTask(File container, byte[] key) {
        super(container, readBI(container), Block.toBlocks(key));
        type = Type.DECODE;
    }
}
