package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.EncodeTask;
import pw.stego.task.Task;
import pw.stego.util.StegoImage;

/**
 * Class for encoding message to container
 */
public class Encoder extends Coder {
    public Encoder(int shift, int mZ) {
        super(shift, mZ);
    }

    /**
     * @param image as container
     * @param from point to start encoding
     * @param data to encode
     * @return index of first point after last encoded block
     */
    private int encode(StegoImage image, int from, Block[] data) {
        jumpTo(from);
        for (int i = 0; i < data.length; i++, stepFwd()) {
            setDataOnCursor(image, data[i].value);

            switch (data[i].type) {
                case TRANS:
                    transpose();
                    break;

                case INV:
                    inverse();
                    break;
            }
        }

        return getCursor();
    }

    /**
     * @param encodeTask to process
     * @return true on successfull encoding, false on error
     * @throws WrongTaskException on wrong task argument type
     */
    public boolean encode(Task encodeTask) throws WrongTaskException {
        if (!(encodeTask instanceof EncodeTask))
            throw new WrongTaskException();

        EncodeTask task = (EncodeTask) encodeTask;
        StegoImage image = task.getImage();

        Block[] data;
        while ((data = task.nextDataPart()).length > 0)
            encode(image, task.getNextJump(), data);

        setDataOnCursor(image, new Block(Block.Type.EOF).value);

        return true;
    }
}
