package pw.stego;

import java.awt.*;
import java.util.Random;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Patterns {
    public enum Type {SIMPLE, ILCD, ILED}

    static String createPattern(Type type, int messageLength, Point imageSize) {
        switch (type) {
            case SIMPLE:
                return newSimplePattern(messageLength, imageSize);
            case ILCD:
                return newCumulativeDistributedPattern(messageLength, imageSize);
            case ILED:
                return newEvenlyDistributedPatter(messageLength, imageSize);
            default:
                return null;
        }
    }

    private static String newSimplePattern(int messageLength, Point imageSize) {
        int start = new Random().nextInt(imageSize.x * imageSize.y - messageLength * 4);
        return getJump(new Point(start % imageSize.x, start / imageSize.x)) + messageLength;
    }

    private static String newCumulativeDistributedPattern(int messageLength, Point imageSize) {
        final StringBuilder pattern = new StringBuilder();
        final Random r = new Random();

        int empty = imageSize.x * imageSize.y - messageLength * 4;
        if (empty < 0)
            return null;

        int current = 0;
        int from = 0;

        while (current < messageLength && empty > 0) {
            int delta = r.nextInt(empty) + 1;
            from += delta;

            int section = r.nextInt(messageLength - current);
            if (current + section >= messageLength)
                section = messageLength - current;

            pattern.append(getJump(new Point(from % imageSize.x, from / imageSize.x)));
            if (section != 0)
                pattern.append(section);

            empty -= delta + 17;
            current += section;
            from += section + 17;
        }

        if (current < messageLength) try {
            int lastSection = Integer.parseInt(pattern.substring(pattern.lastIndexOf(">") + 1, pattern.length()));

            pattern.delete(pattern.lastIndexOf(">") + 1, pattern.length());
            pattern.append(lastSection + messageLength - current);
        } catch (NumberFormatException e) {
            pattern.append(messageLength - current);
        }
        
        return pattern.toString();
    }

    private static String newEvenlyDistributedPatter(int messageLength, Point imageSize) {
        Random r = new Random();

        int empty = imageSize.x * imageSize.y - messageLength * 4;
        if (empty < 0)
            return null;

        int start = r.nextInt(empty / 3);
        empty -= start;

        int space = r.nextInt(Math.max(empty / 1000, 3)) + 17;

        int sectCount = 1;
        int sectSize = messageLength;

        while ((empty -= space) > 0)
            sectSize = messageLength / ++sectCount;

        StringBuilder pattern = new StringBuilder();
        for (int i = 0, from = start; i < sectCount; i++, from += sectSize + space)
            pattern.append(getJump(new Point(from % imageSize.x, from / imageSize.x))).append(sectSize);

        return pattern.toString();
    }

    private static String getJump(Point to) {
        return "<p:" + to.x + "," + to.y + ">";
    }
}
