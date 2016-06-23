package pw.userspace;

import javafx.util.Pair;
import pw.stego.Task;
import pw.stego.Task.Type;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private static int id = 0;
    private ServerSocket soc;
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

                handler = new Handler(toProcess.getKey());

                handler.process();
                handler.writeResult();

                toProcess.getValue().getOutputStream().write(1);
                toProcess.getValue().close();

                System.out.println("End of service #" + id++);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void recieveTasks() throws IOException {
        //noinspection InfiniteLoopStatement
        while(true) {
            Socket client = soc.accept();
            DataInputStream inStream = new DataInputStream(client.getInputStream());

            System.out.println("Start of service #" + id + "\n");

            String message = getPath(inStream).toString();
            Type type = inStream.readByte() == 1 ? Type.ENCODE : Type.DECODE;

            String container = message + "_c";
            String pattern = message + (type == Type.ENCODE ? "_p" : "");

            synchronized(tasks) {
                tasks.add(new Pair<>(
                        new Task(type, new File(container),
                        new String(Files.readAllBytes(Paths.get(pattern))),
                        Files.readAllBytes(Paths.get(message))),
                        client
                ));

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
