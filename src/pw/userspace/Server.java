package pw.userspace;

import javafx.util.Pair;
import pw.stego.Patterns;
import pw.stego.Task;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

class Server {
    private static int id = 0;
    private ServerSocket soc;
    private final Set<File> containers;
    private final Queue<Pair<Task, Socket>> tasks;

    private Server() throws IOException {
        for (int i = 6000, err = 0; i < 7000; i++) try {
            this.soc = new ServerSocket(i);

            try {
                Files.write(Paths.get("port"), String.valueOf(i).getBytes());
            } catch (IOException e) {
                System.out.println("Cannot create file, check your system permissions");
                System.exit(-1);
            }

            break;
        } catch (IOException e) {
            if(err++ == 999)
                throw new IOException();
        }

        tasks = new ConcurrentLinkedQueue<>();
        containers = new ConcurrentSkipListSet<>();

        //Task processing thread
        new Thread(() -> {
            Pair<Task, Socket> toProcess;
            Handler handler;

            //noinspection InfiniteLoopStatement
            while(true) try {
                synchronized(tasks) {
                    while (tasks.size() == 0)
                        tasks.wait();

                    toProcess = tasks.poll();
                }

                System.out.println("Start of service #" + id);

                handler = new Handler(toProcess.getKey());
                byte out;

                try {
                    handler.process();
                    handler.writeResult();

                    out = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    out = 0;
                }

                toProcess.getValue().getOutputStream().write(out);
                toProcess.getValue().close();

                System.out.println("End of service #" + id++ + "\n");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        //Files deleter thread
        new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) for (File file : new LinkedList<>(containers))
                if (System.currentTimeMillis() > file.lastModified() + 1800)
                    if (file.delete())
                        containers.remove(file);
        }).start();
    }

    private void recieveTasks() throws IOException {
        //noinspection InfiniteLoopStatement
        while(true) {
            Socket client = soc.accept();
            DataInputStream inStream = new DataInputStream(client.getInputStream());

            Task toPush;
            String messagePath = getPath(inStream).toString();
            byte[] message = Files.readAllBytes(Paths.get(messagePath));

            byte taskType = inStream.readByte();
            if (taskType == 0)
                toPush = new Task(new File(messagePath + "_c"), message);
            else switch (taskType) {
                case 1:
                    toPush = new Task(
                            new File(messagePath + "_c"),
                            message, Files.readAllBytes(Paths.get(messagePath + "_k")),
                            new String(Files.readAllBytes(Paths.get(messagePath + "_p")), StandardCharsets.ISO_8859_1));
                    break;
                case 2:
                    toPush = new Task(
                            new File(messagePath + "_c"),
                            message, Files.readAllBytes(Paths.get(messagePath + "_k")),
                            Patterns.Type.SIMPLE);
                    break;
                case 3:
                    toPush = new Task(
                            new File(messagePath + "_c"),
                            message, Files.readAllBytes(Paths.get(messagePath + "_k")),
                            Patterns.Type.ILCD);
                    break;
                case 4:
                    toPush = new Task(
                            new File(messagePath + "_c"),
                            message, Files.readAllBytes(Paths.get(messagePath + "_k")),
                            Patterns.Type.ILED);
                    break;
                default:
                    toPush = null;
            }

            synchronized(tasks) {
                tasks.add(new Pair<>(toPush, client));
                tasks.notify();
            }
        }
    }

    private Path getPath(DataInputStream inStream) throws IOException {
        byte length = inStream.readByte();
        byte[] data = new byte[length];

        if(inStream.read(data) == -1)
            throw new IOException();

        return Paths.get(new String(data));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();

        //noinspection InfiniteLoopStatement
        while(true) try {
            server.recieveTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
