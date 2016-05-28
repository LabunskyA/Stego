package com.userspace;

import com.stego.Handler;
import com.userspace.task.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Executable {
    private Handler handler;

    public static void main(String[] args) throws IOException {
        Executable executable = new Executable(args);

        System.out.println("\n" + executable.getResult());
    }

    private Executable(String[] args) throws IOException, IllegalArgumentException {
        if (args.length < 3)
            throw new IllegalArgumentException();

        File imageFile;
        if (!(imageFile = new File(args[0])).exists())
            throw new FileNotFoundException();

        String pattern = new String(Files.readAllBytes(new File(args[2]).toPath()));
        byte[] data;
        try {
            data = Files.readAllBytes(new File(args[3]).toPath());
        } catch (ArrayIndexOutOfBoundsException e) {
            data = Files.readAllBytes(new File(args[2]).toPath());
        }

        handler = new Handler(new Task(args[1], imageFile, pattern, data));
    }

    private String getResult() {
        Object result = handler.process();

        if (result instanceof byte[])
            return new String((byte[]) result);

        if (result instanceof Boolean) {
            handler.writeResult();
            return "Everything is" + (!(Boolean) result ? "not":"") + " ok";
        }

        return result.toString();
    }
}
