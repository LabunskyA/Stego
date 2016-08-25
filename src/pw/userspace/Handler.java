package pw.userspace;

import pw.stego.Task;
import pw.stego.coders.Decoder;
import pw.stego.coders.Encoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Handles tasks and provides simple interface to work with them
 */
class Handler {
    private Object result;

    private Encoder encoder;
    private Decoder decoder;

    private final Task task;

    /**
     * Creates suitable handler for task
     * @param task Task to handle
     */
    Handler(Task task) {
        if (task.type == Task.Type.ENCODE)
            encoder = new Encoder();
        else decoder = new Decoder();

        this.task = task;
    }

    /**
     * Processes associated task
     * @return Result of processing task: Boolean if task is to encode date and byte array if to decode
     */
    Object process() {
        if (decoder != null)
            if (task.getKey())
                return result = decoder.decode(task.getImage(), task.getData());

        if (encoder != null)
            return result = encoder.encode(task);

        return result = false;
    }

    /**
     * Finishes task
     * @return True if rewriting container is successful and false if not
     */
    boolean writeResult() {
        if (task.type == Task.Type.ENCODE) try {
            task.finish();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            String path = task.container.getAbsolutePath();
            path = path.substring(0, path.length() - 2) + "_r";
            
            Files.write(Paths.get(path), (byte[]) result);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
