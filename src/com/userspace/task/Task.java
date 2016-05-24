package com.userspace.task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Task {
    public boolean type;

    public BufferedImage image;

    public Block[] data;
    public Block[] meta;
    public Point from;

    private String input;
    private File imageFile;

    public Task(String type, File imageFile, String input) throws IOException {
        this.type = type.equals("--encode") || !type.equals("--decode");

        image = readBI(imageFile);
        this.imageFile = imageFile;

        this.input = input;
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
        ImageIO.write(image, "PNG", imageFile);
    }

    private Block[] toBlocks(Object object) {
        byte[] data;
        Block[] result;

        if (object instanceof byte[]) {
            result = new Block[((byte[]) object).length * 4];
            data = (byte[]) object;
        } else if (object instanceof String) {
            String str = (String) object;
            result = new Block[str.replaceAll("<(?:i|t)>", "").length() * 4 + str.split("<(?:i|t)>").length - 1];
            data = str.getBytes();
        } else return null;

        for (int i = 0, j = 0; i < data.length; i++)
            for (int shift = 0; shift < 7; j++, shift += 2)
                if (Block.toControl(data, i) == Block.ControlBlock.NONE)
                    result[j] = new Block((byte) ((data[i] >> shift) & 3));
                else {
                    result[j++] = new Block(Block.toControl(data, i));

                    i += 2;
                    break;
                }

        return result;
    }

    public Boolean nextDataPart() {
        if (!input.contains("<p"))
            return false;

        input = cutFrom(input, "<p");
        data = toBlocks(getBetween(input, ">", "<p"));

        String[] temp = getBetween(input, ":", ">").split(",");
        int[] point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};
        from = new Point(point[0], point[1]);

        if (input.contains("<p:")) {
            temp = getBetween(input, "<p:", ">").split(",");
            point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};

            meta = toBlocks(new byte[] {
                    (byte) (point[0] & 0xff), (byte) ((point[0] & 0xff00) >> 8),
                    (byte) (point[1] & 0xff), (byte) ((point[1] & 0xff00) >> 8)
            });
        } else meta = null;

        return true;
    }

    public Boolean getKey() {
        if (input.length() == 0)
            return false;

        data = toBlocks(input.getBytes());
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
        if (string.contains(to) && string.contains(from))
            return cutTo(cutFrom(string, from), to);

        if (string.contains(from))
            return cutFrom(string, from);

        return string;
    }
}
