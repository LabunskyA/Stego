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
        if (decoder != null) {
            if (task.getKey())
                return decoder.decode(task.image, task.data);
        }

        if (encoder != null) {
            if (!task.nextDataPart())
                return false;

            int length = task.image.getWidth();
            int i = encoder.hideData(task.image, task.data, task.from.y * length + task.from.x);

            while (task.nextDataPart()) {
                i = encoder.hideData(task.image, task.data, task.from.y * length + task.from.x);
            }

            task.image.setRGB(i % length, i / length, (task.image.getRGB(i % length, i / length) & 0xfffffefe) |
                                                                                                            0x10000);

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
