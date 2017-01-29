package pw.stego.coders;

import pw.stego.Block;
import pw.stego.task.EncodeTask;
import pw.stego.task.Task;

import java.awt.image.BufferedImage;

/**
 * Class for encoding message to container
 */
public class Encoder extends Coder {
    /**
     * @param image as container
     * @param from point to start encoding
     * @param data to encode
     * @return index of first point after last encoded block
     */
    private int encode(BufferedImage image, int from, Block[] data) {
        int length = image.getWidth();

        int cursor = from;
        for (int i = 0; i < data.length; i++, cursor += delta) {
            image.setRGB(
                    cursor % length, cursor / length,
                    toEncoded(
                            image.getRGB(cursor % length, cursor / length),
                            data[i].value
                    )
            );

            switch (data[i].type) {
                case TRANS:
                    cursor = transpose(cursor, length);
                    break;

                case INV:
                    delta = -delta;
                    break;
            }
        }

        return cursor;
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
        BufferedImage image = task.getImage();

        int length = image.getWidth();

        try {
            Block[] data;
            while ((data = task.nextDataPart()).length > 0) {
                int jump[] = task.getNextJump();
                encode(image, jump[1] * length + jump[0], data);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
