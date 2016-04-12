package com.userspace;

import com.stego.Handler;
import com.userspace.task.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Executable {
    private Handler handler;

    public static void main(String[] args) throws IOException {
        Executable executable = new Executable(args);

        System.out.println(executable.getResult());
    }

    private Executable(String[] args) throws IOException, IllegalArgumentException {
        if (args.length < 2)
            throw new IllegalArgumentException();

        File imageFile;
        if (!(imageFile = new File(args[0])).exists())
            throw new FileNotFoundException();

        handler = new Handler(new Task(args[1], imageFile, args[2]));
    }

    private String getResult() {
        Object result = handler.process();

        if (result instanceof byte[])
            return new String((byte[]) result);

        if (result instanceof Boolean) {
            handler.writeResult();
            return result.toString();
        }

        return result.toString();
    }
}
