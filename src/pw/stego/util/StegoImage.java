package pw.stego.util;

import java.awt.image.BufferedImage;

/**
 * Created by lina on 03.04.17.
 */
public class StegoImage {
    private final BufferedImage holder;

    public StegoImage(BufferedImage original) {
        holder = new BufferedImage(
                original.getWidth(), original.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        holder.getGraphics().drawImage(original, 0, 0, null);

        for (int y = 0; y < original.getHeight(); y++)
            for (int x = 0; x < original.getWidth(); x++)
                holder.setRGB(x, y, original.getRGB(x, y));
    }

    public void setRGB(int x, int y, int value) {
        holder.setRGB(x, y, value);
    }

    public int getRGB(int x, int y) {
        return holder.getRGB(x, y);
    }

    public int getWidth() {
        return holder.getWidth();
    }

    public int getHeight() {
        return holder.getHeight();
    }

    public BufferedImage toBufferedImage() {
        return holder;
    }
}
