package pw.userspace;

import pw.stego.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * StandAlone class with main method and arguments processing
 */
class StandAlone {
    private Handler handler;

    public static void main(String[] args) throws IOException {
        StandAlone standAlone = new StandAlone(args);

        System.out.println("\n" + standAlone.getResult());
    }

    private StandAlone(String[] args) throws IOException, IllegalArgumentException {
        if (args.length < 3) {
            System.out.println("Arguments for decoding: \"path_to_container\" --decode \"path_to_key\"");
            System.out.println("Arguments for encoding: \"path_to_container\" --encode \"path_to_key\"" +
                    " \"path_to_pattern\" \"path_to_message\"");
        }

        byte[] key = Files.readAllBytes(Paths.get(args[0]));

        File imageFile;
        if (!(imageFile = new File(args[1])).exists())
            throw new FileNotFoundException();

        String pattern = new String(Files.readAllBytes(new File(args[3]).toPath()));
        byte[] data;
        try {
            data = Files.readAllBytes(new File(args[4]).toPath());
        } catch (ArrayIndexOutOfBoundsException e) {
            data = Files.readAllBytes(new File(args[3]).toPath());
        }

        if (!args[1].equals("--encode") && !args[1].equals("--decode"))
            throw new IllegalArgumentException();

        Task.Type type = args[1].equals("--encode") ? Task.Type.ENCODE : Task.Type.DECODE;
        if (type == Task.Type.DECODE)
            handler = new Handler(new Task(imageFile, data));
        else handler = new Handler(new Task(imageFile, data, key, pattern));

        System.out.println(new String(data));
    }

    private String getResult() {
        Object result = handler.process();

        if (result instanceof byte[])
            return new String((byte[]) result);

        if (result instanceof Boolean) {
            Boolean finalResult = (Boolean) result | handler.writeResult();

            return "Everything is" + (!finalResult ? "not":"") + " ok";
        }

        return result.toString();
    }
}
