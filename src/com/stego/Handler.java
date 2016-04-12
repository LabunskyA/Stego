package com.stego;

import com.userspace.task.Task;

import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Handler {
    private Encoder encoder;
    private Decoder decoder;

    private Task task;

    public Handler(Task task){
        if (task.type)
            encoder = new Encoder();
        else decoder = new Decoder();

        this.task = task;
    }

    public Object process(){
        if (decoder != null)
            return decoder.decode(task.image, task.key);

        if (encoder != null) {
            while (task.nextDataPart())
                encoder.hideData(task.image, task.data, task.key, task.from.x, task.from.y);

            return true;
        }

        return false;
    }

    public boolean writeResult() {
        try {
            task.finish();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
