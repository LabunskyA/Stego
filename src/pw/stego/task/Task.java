package pw.stego.task;

import pw.stego.Block;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class for whole data with associated container
 * Methods designed to work with data sections.
 */
public class Task {
    enum Type {ENCODE, DECODE}
    Type type;

    private final Block[] key;
    private int start;

    private final File container;
    private final BufferedImage image;

    Task(File container, BufferedImage image, Block[] key) {
        this.container = container;
        this.image = image;

        this.key = key;
    }

    static BufferedImage readBI(File file) {
        BufferedImage original;

        try {
            original = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
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

    public int mZ() {
        return image.getHeight() * image.getWidth();
    }

    public int shift() {
        return image.getWidth();
    }

    public BufferedImage getImage() {
        return image;
    }
    public File getContainer() {
        return container;
    }
    public Block[] getKey() {
        Block[] copy = new Block[key.length];
        System.arraycopy(key, 0, copy, 0, key.length);

        return copy;
    }
    public int getStart() {
        return start;
    }
}
