package pw.stego.task;

import pw.stego.Block;
import pw.stego.util.StegoImage;

/**
 * Class for whole data with associated container
 * Methods designed to work with data sections.
 */
public class Task {
    enum Type {ENCODE, DECODE}

    final Block[] key;

    private final Type type;
    private final StegoImage image;

    Task(Type type, StegoImage image, Block[] key) {
        this.type = type;
        this.image = image;
        this.key = key;
    }

    public int mZ() {
        return image.getHeight() * image.getWidth();
    }

    public int shift() {
        return image.getWidth();
    }

    public Type getType() {
        return type;
    }

    public StegoImage getImage() {
        return image;
    }

    public Block[] getKey() {
        Block[] copy = new Block[key.length];
        System.arraycopy(key, 0, copy, 0, key.length);

        return copy;
    }
}
