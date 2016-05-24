package com.stego;

import com.stego.coders.Decoder;
import com.stego.coders.Encoder;
import com.userspace.task.Task;

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

    public Object process() {
        if (decoder != null)
            if (task.getKey())
                return decoder.decode(task.image, task.data);

        if (encoder != null) {
            int length = task.image.getWidth();
            int i = 0;

            while (task.nextDataPart()) {
                i = encoder.hideData(task.image, task.data, task.from.y * length + task.from.x);

                if (task.meta != null) {
                    task.image.setRGB(i % length, i / length, (task.image.getRGB(i % length, i / length) & 0xfffffefe) |
                            0x10101);

                    encoder.hideData(task.image, task.meta, i + encoder.delta);
                }
            }

            System.out.println("Encoding from x = " + i % length + " y = " + i / length);
            task.image.setRGB(i % length, i / length, (task.image.getRGB(i % length, i / length) & 0xfffffefe) |
                                                                                                            0x10000);
            System.out.println(4);

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
