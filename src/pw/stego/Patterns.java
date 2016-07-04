package pw.stego;

import java.awt.*;
import java.util.Random;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
class Patterns {
    enum Type{SIMPLE, LINEARLY_DISTRIBUTED}

    static String createPattern(Type type, int messageLength, Point imageSize) {
        switch (type) {
            case SIMPLE:
                return newSimplePattern(messageLength, imageSize);
            case LINEARLY_DISTRIBUTED:
                return newLinearlyDistributedPattern(messageLength, imageSize);
            default:
                return null;
        }
    }

    private static String newSimplePattern(int messageLength, Point imageSize) {
        int start = new Random().nextInt(imageSize.x * imageSize.y - messageLength * 4);
        return getJump(new Point(start % imageSize.x, start / imageSize.x)) + messageLength;
    }

    private static String newLinearlyDistributedPattern(int messageLength, Point imageSize) {
        final StringBuilder pattern = new StringBuilder();
        final Random r = new Random();

        int empty = imageSize.x * imageSize.y - messageLength;
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

            empty -= delta + 9;
            current += section;
            from += section + 9;
        }

        if (current < messageLength) {
            int lastSection = Integer.parseInt(pattern.substring(pattern.lastIndexOf(">") + 1, pattern.length()));

            pattern.delete(pattern.lastIndexOf(">") + 1, pattern.length());
            pattern.append(lastSection + messageLength - current);
        }
        
        return pattern.toString();
    }

    private static String getJump(Point to) {
        return "<p:" + to.x + "," + to.y + ">";
    }
}
