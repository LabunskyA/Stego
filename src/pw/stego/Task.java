package pw.stego;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Class for whole input data with associated container image.
 * Methods designed to work with data sections.
 */
public class Task {
    public enum Type {ENCODE, DECODE}
    public final Type type;

    private BufferedImage image;

    private Point from;
    private int inputIdx = 0;

    private Block[] data;
    private Block[] meta;

    private String pattern;
    private final String input;
    public final File container;

    public Task(File container, byte[] input, byte[] key, Patterns.Type patternType) {
        type = Type.ENCODE;

        this.image = readBI(container);
        this.container = container;
        this.input = new String(key, StandardCharsets.ISO_8859_1) + new String(input, StandardCharsets.ISO_8859_1);

        if (image != null) try {
            this.pattern = Patterns.createPattern(
                    patternType,
                    this.input.length(),
                    new Point(image.getWidth(), image.getHeight())
            );
        } catch (Exception e) {
            this.pattern = null;
        } else this.pattern = null;

        System.out.println(this.pattern);
    }

    public Task(File container, byte[] input, byte[] key, String pattern) {
        type = Type.ENCODE;

        this.image = readBI(container);
        this.container = container;
        this.input = new String(key, StandardCharsets.ISO_8859_1) + new String(input, StandardCharsets.ISO_8859_1);

        if (pattern == null && image != null) try {
            this.pattern = Patterns.createPattern(
                    Patterns.Type.SIMPLE,
                    this.input.length(),
                    new Point(image.getWidth(), image.getHeight())
            );
        } catch (Exception e) {
            this.pattern = null;
        } else this.pattern = pattern;

        System.out.println(this.pattern);
    }

    public Task(File container, byte[] key) {
        this.type = Type.DECODE;

        image = readBI(container);

        this.container = container;
        this.input = new String(key, StandardCharsets.ISO_8859_1);
    }

    private static BufferedImage readBI(File file) {
        BufferedImage original;

        try {
            original = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(original, 0, 0, null);

        for(int y = 0; y < original.getHeight(); y++)
            for(int x = 0; x < original.getWidth(); x++)
                image.setRGB(x,y, original.getRGB(x,y));

        return image;
    }

    /**
     * Writes all changes from RAM to original image file
     * @throws IOException if something wrong with writing into container
     */
    public void finish() throws IOException {
        ImageIO.write(image, "PNG", container);
    }

    /**
     * @param object Data to represent as blocks array
     * @return Blocks array from specifed input
     */
    private Block[] toBlocks(Object object) {
        byte[] data;
        Block[] result;

        if (object instanceof byte[]) {
            result = new Block[((byte[]) object).length * 4];
            data = (byte[]) object;
        } else if (object instanceof String) {
            String str = (String) object;
            String[] section = str.split("<(?:i|t)>");

            int dataLen = section.length - 1;
            for (String partLength : section)
                if (partLength.length() > 0)
                    dataLen += Integer.parseInt(partLength) * 4;

            result = new Block[dataLen];
            data = mergeWithInput(section, str).getBytes(StandardCharsets.ISO_8859_1);
        } else return null;

        for (int i = 0, j = 0; i < data.length; i++)
            for (int shift = 0; shift < 7; j++, shift += 2)
                if (Block.toControl(data, i) == Block.Type.NONE)
                    result[j] = new Block((byte) ((data[i] >> shift) & 3));
                else {
                    result[j++] = new Block(Block.toControl(data, i));

                    i += 2;
                    break;
                }

        return result;
    }

    /**
     * @param section String array with data lengths between control blocks
     * @param full Full section string
     * @return Pattern filled with data of specifed length
     */
    private String mergeWithInput(String[] section, String full) {
        int fullIdx = 0;

        String result = "";
        for (String part : section) {
            if (part.length() > 0) {
                int length = Integer.parseInt(part);

                fullIdx += part.length();
                result += input.substring(inputIdx, inputIdx += length);
            }

            try {
                result += full.substring(fullIdx, fullIdx += 3);
            } catch (StringIndexOutOfBoundsException ignored){}
        }

        return result;
    }

    /**
     * Process next section into byte array if any
     * @return return true if there is data left, false if none
     */
    public Boolean nextDataPart() {
        if (!pattern.contains("<p"))
            return false;

        pattern = cutFrom(pattern, "<p");
        data = toBlocks(getBetween(pattern, ">", "<p"));

        String[] temp = getBetween(pattern, ":", ">").split(",");
        int[] point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};
        from = new Point(point[0], point[1]);

        switch (getSectionEnd(pattern.indexOf("<p:"), pattern.indexOf("<pm:"))) {
            case JUMP:
                temp = getBetween(pattern, "<p:", ">").split(",");
                point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};

                meta = toBlocks(new byte[] {
                        (byte) (point[0] & 0xff), (byte) ((point[0] & 0xff00) >> 8),
                        (byte) (point[1] & 0xff), (byte) ((point[1] & 0x7f00) >> 8)
                });
                break;
            case JUMP_MARKED:
                temp = getBetween(pattern, "<pm:", ">").split(",");
                point = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2])};

                meta = toBlocks(new byte[] {
                        (byte) (point[0] & 0xff), (byte) ((point[0] & 0xff00) >> 8),
                        (byte) (point[1] & 0xff), (byte) ((point[1] & 0xff00 | 0x8000) >> 8),
                        (byte) (point[2]  & 0xff)
                });
                break;
            default:
                meta = null;
        }

        return true;
    }

    /**
     * Extracts type of control block in the end of the section
     * @param p Index of jump block
     * @param pm Index of marked jump block
     * @return Next nearest control block type
     */
    private Block.Type getSectionEnd(int p, int pm) {
        if (p == pm)
            return Block.Type.EOF;

        if (p == -1)
            return Block.Type.JUMP_MARKED;
        if (pm == -1)
            return Block.Type.JUMP;

        if (p < pm)
            return Block.Type.JUMP;

        return Block.Type.JUMP_MARKED;
    }

    public Boolean getKey() {
        if (input.length() == 0)
            return false;

        data = toBlocks(input.getBytes(StandardCharsets.ISO_8859_1));
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

    public BufferedImage getImage() {
        return image;
    }
    public Point getFrom() {
        return from;
    }
    public Block[] getData() {
        return data;
    }
    public Block[] getMeta() {
        return meta;
    }
}
