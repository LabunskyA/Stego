package pw.stego;

import pw.stego.coders.Decoder;
import pw.stego.coders.Encoder;
import pw.stego.coders.KeyNotFoundException;
import pw.stego.coders.WrongTaskException;
import pw.stego.task.DecodeTask;
import pw.stego.task.EncodeTask;
import pw.stego.task.Task;

import java.awt.image.BufferedImage;

/**
 * Handles tasks and provides simple interface to work with them
 */
public class TaskHandler {
    private final Encoder encoder;
    private final Decoder decoder;

    private final Task task;

    /**
     * Creates suitable handler for task
     * @param task Task to handle
     */
    public TaskHandler(Task task) {
        encoder = new Encoder(task.shift(), task.mZ());
        decoder = new Decoder(task.shift(), task.mZ());

        this.task = task;
    }

    /**
     * Processes associated task
     */
    public void process() throws WrongTaskException, KeyNotFoundException {
        if (task instanceof DecodeTask)
            decoder.decode(task);

        else if (task instanceof EncodeTask)
            encoder.encode(task);
    }

    /**
     * Finishes task
     */
    public void finish() {
        if (task instanceof EncodeTask) {
            decoder.clearFalseKeys(task.getImage(), task.getKey(), ((EncodeTask) task).getStart());
        }
    }

    public BufferedImage getContainer() {
        return task.getImage().toBufferedImage();
    }
}
