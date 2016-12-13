package pw.stego;

import pw.stego.coders.Decoder;
import pw.stego.coders.Encoder;
import pw.stego.coders.WrongTaskException;
import pw.stego.task.EncodeTask;
import pw.stego.task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Handles tasks and provides simple interface to work with them
 */
public class TaskHandler {
    private Object result;

    private Encoder encoder;
    private Decoder decoder;

    private final Task task;

    /**
     * Creates suitable handler for task
     * @param task Task to handle
     */
    public TaskHandler(Task task) {
        if (task instanceof EncodeTask)
            encoder = new Encoder();
        else decoder = new Decoder();

        this.task = task;
    }

    /**
     * Processes associated task
     * @return Result of processing task: Boolean if task is to encode date and byte array if to decode
     */
    @SuppressWarnings("WeakerAccess")
    public Object process() throws WrongTaskException {
        if (decoder != null)
            return result = decoder.decode(task);

        if (encoder != null)
            return result = encoder.encode(task);

        return result = false;
    }

    /**
     * Finishes task with writing changes to filesystem
     * @return True if rewriting container is successful and false if not
     */
    @SuppressWarnings("WeakerAccess")
    public boolean writeResult() {
        if (task instanceof EncodeTask) try {
            task.finish();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        String path = task.getContainer().getAbsolutePath();
        path = path.substring(0, path.lastIndexOf('.')) + ".dec";

        try {
            Files.write(Paths.get(path), (byte[]) result);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}