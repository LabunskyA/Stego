package com.userspace.task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Task {
    public boolean type;

    public BufferedImage image;

    public byte[] key;
    public byte[] data;
    public Point from;

    private String input;
    private File imageFile;

    public Task(String type, File imageFile, Object key, String input) throws IOException {
        this.type = type.equals("--encode") || !type.equals("--decode");

        image = readBI(imageFile);

        this.imageFile = imageFile;

        this.key = toBytes(key);
        this.input = input;
    }

    public Task(String type, File imageFile, Object key) throws IOException {
        this.type = type.equals("--encode") || !type.equals("--decode");
        image = readBI(imageFile);

        this.key = toBytes(key);
    }

    private BufferedImage readBI(File file) throws IOException {
        BufferedImage original = ImageIO.read(file);

        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(original, 0, 0, null);

        for(int y = 0; y < original.getHeight(); y++)
            for(int x = 0; x < original.getWidth(); x++)
                image.setRGB(x,y, original.getRGB(x,y));

        return image;
    }

    public void finish() throws IOException {
        ImageIO.write(image, "PNG", new File("stego.png"));
    }

    private byte[] toBytes(Object object) {
        if (object instanceof String)
            return ((String) object).getBytes();

        if (object instanceof File)
            try {
                return Files.readAllBytes(((File) object).toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        return null;
    }

    public Boolean nextDataPart() {
        if (!input.contains("<p:"))
            return false;

        input = cutFrom(input, "<p:");
        data = toBytes(getBetween(input, ">", "<"));

        String[] temp = getBetween(input, "<p:", ">").split(",");
        from = new Point(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));

        return true;
    }

    private String cutFrom(String string, String trigger) {
        if (string.contains(trigger))
            return string.substring(string.indexOf(trigger) + trigger.length());

        return string;
    }
    private String cutTo(String string, String trigger) {
        if (string.contains(trigger))
            return string.substring(0, string.indexOf(trigger));

        return string;
    }
    private String getBetween(String string, String from, String to) {
        if (string.contains(to))
            return cutFrom(cutTo(string, to), from);

        if (string.contains(from))
            return cutFrom(string, from);

        return string;
    }
}
