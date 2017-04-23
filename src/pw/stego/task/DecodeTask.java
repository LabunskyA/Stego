package pw.stego.task;

import pw.stego.Block;
import pw.stego.util.StegoImage;

/**
 * Task with decode constructor
 * Created by lina on 14.09.16.
 */
public class DecodeTask extends Task {
    public DecodeTask(StegoImage container, byte[] key) {
        super(Type.DECODE, container, Block.toBlocks(key));
    }
}
